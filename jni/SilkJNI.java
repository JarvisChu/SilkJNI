public class SilkJNI {
   public static native void sayHello();
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
    * silkBufferIn: the raw silk audio data
    * return: pcm buffer
   */ 
   public static native byte[] decode(long decoderPtr, int sampleRate, byte[] silkBufferIn);
}