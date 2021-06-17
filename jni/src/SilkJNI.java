public class SilkJNI {
   
   /* get version
    */
   public static native String getVersion();

   /* create decoder
    * return: the decoder instance decoderPtr
    */
   public static native long createDecoder();

   /* destroy decoder
    * decoderPtr: the decoder instance, returned by createDecoder 
    */
   public static native void destroyDecoder(long decoderPtr);

   /* decode raw silk buffer
    * decoderPtr: the decoder instance, returned by createDecoder
    * sampleRate: sample rate
    * silkBufferIn: the raw silk audio data, without format.
    * return: pcm buffer
   */ 
   public static native byte[] decodeRaw(long decoderPtr, int sampleRate, byte[] silkBufferIn);

   /* decode formated silk buffer
    * decoderPtr: the decoder instance, returned by createDecoder
    * sampleRate: sample rate
    * silkBufferIn: the format silk audio data, standard package format: [len+data][len+data]
    * return: pcm buffer
   */
   public static native byte[] decode(long decoderPtr, int sampleRate, byte[] silkBufferIn);

   /* create encoder
    * return: the encoder instance encoderPtr
    */
    public static native long createEncoder();

    /* destroy encoder
     * encoderPtr: the encoder instance, returned by createEncoder 
     */
    public static native void destroyEncoder(long encoderPtr);
 
    /* encode pcm into formated silk buffer
     * encoderPtr: the encoder instance, returned by createEncoder
     * sampleRate: sample rate
     * pcmBufferIn: the pcm audio data
     * return: silk buffer with standard package format: [len+data][len+data]
    */
    public static native byte[] encode(long encoderPtr, int sampleRate, int sampleBits, int channel, byte[] pcmBufferIn);
}