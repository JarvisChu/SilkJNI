public class SilkJNI {
   public static native void sayHello();
   public static native String getVersion();
   public static native long createDecoder();
   public static native void destroyDecoder(long decoderPtr);

   // decode silk buffer
   // return pcm buffer
   public static native byte[] decode(long decoderPtr, int sampleRate, byte[] silkBufferIn);
}