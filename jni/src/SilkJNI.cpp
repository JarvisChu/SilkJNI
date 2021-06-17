#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "SilkJNI.h"
#include "SKP_Silk_SDK_API.h"
#include <string>
#include <vector>

typedef unsigned char BYTE;

#define MAX_BYTES_PER_FRAME     1024
#define MAX_INPUT_FRAMES        5
#define MAX_FRAME_LENGTH        480
#define FRAME_LENGTH_MS         20
#define MAX_API_FS_KHZ          48
#define MAX_LBRR_DELAY          2



/* SwapEndian: convert a little endian int16 to a big endian int16 or vica verca
 * vec: int16 array
 * len: length of vec
 */
void SwapEndian(int16_t vec[],int len)
{
    for(int i = 0; i < len; i++){
        int16_t tmp = vec[i];
        uint8_t *p1 = (uint8_t *) &vec[i]; 
        uint8_t *p2 = (uint8_t *) &tmp;
        p1[0] = p2[1]; 
        p1[1] = p2[0];
    }
}

/* IsBigEndian: check system endian
 */
bool IsBigEndian()
{
    uint16_t n = 1;
    if( ((uint8_t*) &n)[0] == 1 ){
        return false;
    }
    return true;
}

void Short2LittleEndianBytes(short st, BYTE bs[2])
{
    // change to little endian
    if( IsBigEndian()){
        SwapEndian(&st, 1);
    }

    bs[0] = ((BYTE*)&st)[0];
    bs[1] = ((BYTE*)&st)[1];
}

short LittleEndianBytes2Short(BYTE low, BYTE high)
{
    short st = 0;
    ((BYTE*)&st)[0] = low;
    ((BYTE*)&st)[1] = high;

    if(IsBigEndian()){
        SwapEndian(&st,1);
    }

    return st;
}

bool DecodeInternal(void* pDecoder, int sample_rate, const std::vector<BYTE>& silk_in, std::vector<BYTE>& pcm_out)
{
    SKP_SILK_SDK_DecControlStruct decControl;
    decControl.API_sampleRate = (int)sample_rate; // I: Output signal sampling rate in Hertz; 8000/12000/16000/24000
    
    int frames = 0;
    SKP_int16 out[ ( ( FRAME_LENGTH_MS * MAX_API_FS_KHZ ) << 1 ) * MAX_INPUT_FRAMES ] = {0};
    SKP_int16 *outPtr    = out;
    short     out_size = 0;
    short     len        = 0;
    do {
        /* Decode 20 ms */
        int ret = SKP_Silk_SDK_Decode(pDecoder, &decControl, 0, (unsigned char*) silk_in.data(), (int)silk_in.size(), outPtr, &len);
        if( ret ) {
            printf( "SKP_Silk_SDK_Decode returned %d\n", ret );
        }

        frames++;
        outPtr += len;
        out_size += len;
        if( frames > MAX_INPUT_FRAMES ) {
            /* Hack for corrupt stream that could generate too many frames */
            outPtr     = out;
            out_size = 0;
            frames     = 0;
            break;
        }
    /* Until last 20 ms frame of packet has been decoded */
    } while( decControl.moreInternalDecoderFrames);

    // using little endian
    if( IsBigEndian()){
        SwapEndian(out, out_size);
    }

    // append to pcm out
    BYTE* p = (BYTE*)out;
    pcm_out.insert(pcm_out.end(), p, p + out_size*2 );

    return true;
}

bool EncodeInternal(void* pEncoder, int sample_rate, const std::vector<BYTE>& pcm_in, std::vector<BYTE>& silk_out, int duration_ms/* = 20*/, int bit_rate/* = 10000*/)
{
	SKP_SILK_SDK_EncControlStruct encControl;
	encControl.API_sampleRate = sample_rate;
	encControl.maxInternalSampleRate = sample_rate;
	encControl.packetSize = (duration_ms * sample_rate) / 1000;
	encControl.complexity = 2;
	encControl.packetLossPercentage = 0;
	encControl.useInBandFEC = 0;
	encControl.useDTX = 0;
	encControl.bitRate = bit_rate;

	// encode
	short nBytes = 2048;
	SKP_uint8 payload[2048];
	int ret = SKP_Silk_SDK_Encode(pEncoder, &encControl, (const short*)&pcm_in[0], encControl.packetSize, payload, &nBytes);
	if (ret) {
		printf("SKP_Silk_Encode failed, ret-%d\n", ret);
		return false;
	}

    BYTE bytes[2];
	Short2LittleEndianBytes(nBytes, bytes);
	silk_out.insert(silk_out.end(), &bytes[0], &bytes[2]);
	silk_out.insert(silk_out.end(), &payload[0], &payload[nBytes]);
    return true;
}

JNIEXPORT jstring JNICALL Java_SilkJNI_getVersion
  (JNIEnv *env, jclass jc)
{
    const char * version = SKP_Silk_SDK_get_version();
    jclass strClass = env->FindClass("Ljava/lang/String;");
    jmethodID ctorID = env->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
    jbyteArray bytes = env->NewByteArray(strlen(version));
    env->SetByteArrayRegion(bytes, 0, strlen(version), (jbyte*)version);
    jstring encoding = env->NewStringUTF("utf-8");
    return (jstring)env->NewObject(strClass, ctorID, bytes, encoding);
}


JNIEXPORT jlong JNICALL Java_SilkJNI_createDecoder
  (JNIEnv *env, jclass jc)
{
    int decSizeBytes = 0;
    int ret = SKP_Silk_SDK_Get_Decoder_Size( &decSizeBytes );
    if( ret ) {
        printf( "SKP_Silk_SDK_Get_Decoder_Size returned %d\n", ret );
        return -1;
    }

    void* pDecoder = malloc(decSizeBytes);

    /* Reset decoder */
    ret = SKP_Silk_SDK_InitDecoder( pDecoder );
    if( ret ) {
        printf( "SKP_Silk_InitDecoder returned %d\n", ret );
        return -2;
    }

    return (jlong)pDecoder;
}

JNIEXPORT jlong JNICALL Java_SilkJNI_createEncoder
  (JNIEnv *env, jclass jc)
{
    int encSizeBytes = 0;
    int ret = SKP_Silk_SDK_Get_Encoder_Size( &encSizeBytes );
    if( ret ) {
        printf( "SKP_Silk_SDK_Get_Encoder_Size returned %d\n", ret );
        return -1;
    }

    void* pEncoder = malloc(encSizeBytes);

    SKP_SILK_SDK_EncControlStruct encStatus;
    ret = SKP_Silk_SDK_InitEncoder(pEncoder, &encStatus);
    if( ret ) {
        printf( "SKP_Silk_SDK_InitEncoder returned %d\n", ret );
        return -2;
    }

    return (jlong)pEncoder;
}

JNIEXPORT void JNICALL Java_SilkJNI_destroyDecoder
  (JNIEnv *env, jclass jc, jlong decoderPtr)
{
    if(decoderPtr > 0){
        free((void *) decoderPtr);
    }
}

JNIEXPORT void JNICALL Java_SilkJNI_destroyEncoder
  (JNIEnv *env, jclass jc, jlong encoderPtr)
{
    if(encoderPtr > 0){
        free((void *) encoderPtr);
    }
}

JNIEXPORT jbyteArray JNICALL Java_SilkJNI_decodeRaw
  (JNIEnv *env, jclass jc, jlong decoderPtr, jint sampleRate, jbyteArray silkBuffer)
{
    //printf( "Java_SilkJNI_decode: decoderPtr:%ld, sampleRate:%d\n", decoderPtr, sampleRate);

    if(decoderPtr <= 0) {
        printf("Java_SilkJNI_decode: invalid decoderPtr, %ld", decoderPtr);    
        jbyteArray pcmBuffer = env->NewByteArray(0);
        return pcmBuffer;
    }

    jbyte* silkBytes = env->GetByteArrayElements(silkBuffer, 0);
	jsize  silkBytesSize = env->GetArrayLength(silkBuffer);

    std::vector<BYTE> silk_in((BYTE*)silkBytes, (BYTE*)silkBytes + (int)silkBytesSize );
    std::vector<BYTE> pcm_out;
    DecodeInternal((void *)decoderPtr, (int)sampleRate, silk_in, pcm_out);

    env->ReleaseByteArrayElements(silkBuffer, silkBytes, 0);
    jbyteArray pcmBuffer = env->NewByteArray(pcm_out.size());
    env->SetByteArrayRegion(pcmBuffer, 0, pcm_out.size(), (const signed char*) pcm_out.data());
    return pcmBuffer;
}

JNIEXPORT jbyteArray JNICALL Java_SilkJNI_decode
  (JNIEnv *env, jclass jc, jlong decoderPtr, jint sampleRate, jbyteArray silkBuffer)
{
    //printf( "Java_SilkJNI_decode: decoderPtr:%ld, sampleRate:%d\n", decoderPtr, sampleRate);

    if(decoderPtr <= 0) {
        printf("Java_SilkJNI_decode: invalid decoderPtr, %ld", decoderPtr);    
        jbyteArray pcmBuffer = env->NewByteArray(0);
        return pcmBuffer;
    }

    jbyte* silkBytes = env->GetByteArrayElements(silkBuffer, 0);
	jsize  silkBytesSize = env->GetArrayLength(silkBuffer);

    std::vector<BYTE> silk_in((BYTE*)silkBytes, (BYTE*)silkBytes + (int)silkBytesSize );
    std::vector<BYTE> pcm_out;

    int pkg_cnt = 0;
    while(silk_in.size() > 2){
        short pkg_size = LittleEndianBytes2Short(silk_in[0], silk_in[1]);
        //printf("pkg_size: %d, silk_in[0]=%x, silk_in[1]=%x\n", pkg_size, silk_in[0], silk_in[1]);

        if(silk_in.size() < size_t(pkg_size + 2)){
            printf("Java_SilkJNI_decode: remain not enough, remain:%ld, pkg_size:%d\n", silk_in.size() - 2, pkg_size);
            break;
        }

        pkg_cnt ++;
        std::vector<BYTE> silk_package(silk_in.begin() + 2, silk_in.begin() + 2 + pkg_size);
        silk_in.erase(silk_in.begin(), silk_in.begin() + 2 + pkg_size);
        DecodeInternal((void *)decoderPtr, (int)sampleRate, silk_package, pcm_out);
    }

    printf("Java_SilkJNI_decode: input silk size:%d, output pcm size:%ld, package count:%d\n", (int)silkBytesSize, pcm_out.size(), pkg_cnt); 

    env->ReleaseByteArrayElements(silkBuffer, silkBytes, 0);
    jbyteArray pcmBuffer = env->NewByteArray(pcm_out.size());
    env->SetByteArrayRegion(pcmBuffer, 0, pcm_out.size(), (const signed char*) pcm_out.data());
    return pcmBuffer;
}

JNIEXPORT jbyteArray JNICALL Java_SilkJNI_encode
  (JNIEnv *env, jclass jc, jlong encoderPtr, jint sampleRate, jint sampleBits,jint channel, jbyteArray pcmBuffer)
{
    printf( "Java_SilkJNI_encode: encoderPtr:%ld, sampleRate:%d, sampleBits:%d, channel:%d\n", encoderPtr, sampleRate, sampleBits, channel);

    if(encoderPtr <= 0) {
        printf("Java_SilkJNI_encode: invalid encoderPtr, %ld", encoderPtr);    
        jbyteArray silkBuffer = env->NewByteArray(0);
        return silkBuffer;
    }

    jbyte* pcmBytes = env->GetByteArrayElements(pcmBuffer, 0);
	jsize  pcmBytesSize = env->GetArrayLength(pcmBuffer);

    std::vector<BYTE> pcm_in((BYTE*)pcmBytes, (BYTE*)pcmBytes + (int)pcmBytesSize );
    std::vector<BYTE> silk_out;

    // encode 20ms audio data each package
	int nBytesPer20ms = sampleRate * channel * sampleBits / 8 / 50;
    int pkg_cnt = 0;
    int bit_rate = 10000; // YOU CAN CHANGE bit_rate to CHANGE SILK QUALITY !!!!
    while(pcm_in.size() >= size_t(nBytesPer20ms)) {
        pkg_cnt ++;
        EncodeInternal((void*) encoderPtr, sampleRate, pcm_in, silk_out, 20, bit_rate); // will append the encoded silk data into silk_buf
        pcm_in.erase(pcm_in.begin(), pcm_in.begin() + nBytesPer20ms);
    }

    printf("Java_SilkJNI_encode: input pcm size:%d, output silk size:%ld, package count:%d\n", (int)pcmBytesSize, silk_out.size(), pkg_cnt); 

    env->ReleaseByteArrayElements(pcmBuffer, pcmBytes, 0);
    jbyteArray silkBuffer = env->NewByteArray(silk_out.size());
    env->SetByteArrayRegion(silkBuffer, 0, silk_out.size(), (const signed char*) silk_out.data());
    return silkBuffer;
}


