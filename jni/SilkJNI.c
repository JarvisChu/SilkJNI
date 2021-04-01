#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "SilkJNI.h"
#include "SKP_Silk_SDK_API.h"

#define MAX_BYTES_PER_FRAME     1024
#define MAX_INPUT_FRAMES        5
#define MAX_FRAME_LENGTH        480
#define FRAME_LENGTH_MS         20
#define MAX_API_FS_KHZ          48
#define MAX_LBRR_DELAY          2

JNIEXPORT void JNICALL Java_SilkJNI_sayHello
  (JNIEnv * env, jclass jc)
{
    printf("Hello World\n");
}

JNIEXPORT jstring JNICALL Java_SilkJNI_getVersion
  (JNIEnv *env, jclass jc)
{
    const char * version = SKP_Silk_SDK_get_version();
    jclass strClass = (*env)->FindClass(env, "Ljava/lang/String;");
    jmethodID ctorID = (*env)->GetMethodID(env, strClass, "<init>", "([BLjava/lang/String;)V");
    jbyteArray bytes = (*env)->NewByteArray(env, strlen(version));
    (*env)->SetByteArrayRegion(env, bytes, 0, strlen(version), (jbyte*)version);
    jstring encoding = (*env)->NewStringUTF(env, "utf-8");
    return (jstring)(*env)->NewObject(env, strClass, ctorID, bytes, encoding);
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

JNIEXPORT void JNICALL Java_SilkJNI_destroyDecoder
  (JNIEnv *env, jclass jc, jlong decoderPtr)
{
    if(decoderPtr > 0){
        free((void *) decoderPtr);
    }
} 


JNIEXPORT jbyteArray JNICALL Java_SilkJNI_decode
  (JNIEnv *env, jclass jc, jlong decoderPtr, jint sampleRate, jbyteArray silkBuffer)
{
    printf( "Java_SilkJNI_decode decoderPtr:%ld, sampleRate:%d\n", decoderPtr, sampleRate);

    if(decoderPtr <= 0) {
        printf("Java_SilkJNI_decode invalid decoderPtr, %ld", decoderPtr);    
        jbyteArray pcmBuffer = (*env)->NewByteArray(env,0);
        return pcmBuffer;
    }

    jbyte* silkBytes = (*env)->GetByteArrayElements(env,silkBuffer, 0);
	jsize  silkBytesSize = (*env)->GetArrayLength(env,silkBuffer);

    SKP_SILK_SDK_DecControlStruct decControl;
    decControl.API_sampleRate = (int)sampleRate; // I: Output signal sampling rate in Hertz; 8000/12000/16000/24000
    
    int frames = 0;
    SKP_int16 pcmOut[ ( ( FRAME_LENGTH_MS * MAX_API_FS_KHZ ) << 1 ) * MAX_INPUT_FRAMES ] = {0};
    SKP_int16 *outPtr = pcmOut;
    short pcmOutSize = 0;
    short len = 0;
    do {
        /* Decode 20 ms */
        int ret = SKP_Silk_SDK_Decode((void*)decoderPtr, &decControl, 0, (unsigned char*) silkBytes, (int)silkBytesSize, outPtr, &len);
        if( ret ) {
            printf( "SKP_Silk_SDK_Decode returned %d\n", ret );
        }

        frames++;
        outPtr  += len;
        pcmOutSize += len;
        if( frames > MAX_INPUT_FRAMES ) {
            /* Hack for corrupt stream that could generate too many frames */
            outPtr  = pcmOut;
            pcmOutSize = 0;
            frames  = 0;
            break;
        }
    /* Until last 20 ms frame of packet has been decoded */
    } while( decControl.moreInternalDecoderFrames ); 

    (*env)->ReleaseByteArrayElements(env, silkBuffer, silkBytes, 0);

    jbyteArray pcmBuffer = (*env)->NewByteArray(env,pcmOutSize * 2);
    (*env)->SetByteArrayRegion(env,pcmBuffer, 0, pcmOutSize * 2, (char*) pcmOut);
    return pcmBuffer;
} 