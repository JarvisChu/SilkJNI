import java.io.*;
import java.util.Arrays;
import java.nio.*;

public class TestJNI {

   static {
      System.loadLibrary("silkjni"); // Load native library at runtime
   }

   // Test JNI
   public static void main(String[] args) {

      SilkJNI silkJNI = new SilkJNI();

      // hello world
      silkJNI.sayHello();

      // get version
      String v = silkJNI.getVersion();
      System.out.println("version is:" + v);

      // create decoder
      long decoder = silkJNI.createDecoder();
      System.out.println("decoder is:" + decoder);

      // test decoder
      try (
            InputStream inputStream = new FileInputStream("8000_16bit_1channel.silk");
            OutputStream outputStream = new FileOutputStream("out.pcm");
        ) {

            // check silk file header
            String header = "#!SILK_V3";
            byte[] headerBytes = new byte[]{0,0,0,0,0,0,0,0,0};
            int bytesRead = inputStream.read(headerBytes);
            if( ! Arrays.equals(headerBytes, header.getBytes()) ){
               System.out.println("Invalid Silk File, Error: Wrong Header " + new String(headerBytes) );
               return ;
            }else{
               System.out.println("Valid Silk File Header" + new String(headerBytes) );
            }

            // read silk data and decoder
            while(true)
            {
               byte[] sizeBytes = new byte[]{0,0};
               bytesRead = inputStream.read(sizeBytes);
               if(bytesRead < 2){
                  System.out.println("Read Over, bytesRead:  " + bytesRead );
                  break;
               }

               byte[] newSizeBytes = new byte[]{ sizeBytes[1], sizeBytes[0]}; // convert little-endian 2 big-endian
               ByteBuffer wrapped = ByteBuffer.wrap(newSizeBytes); // big-endian by default
               short sz = wrapped.getShort();
               System.out.println("sz:  " + sz);
               if(sz <= 0){
                  System.out.println("Read Over, sz:  " + sz );
                  break;
               }

               byte[] silkData = new byte[sz];
               bytesRead = inputStream.read(silkData);
               if(bytesRead < sz){
                  System.out.println("Read Over, bytesRead:  " + bytesRead );
                  break;
               }

               // decoder
               byte[] pcmData = silkJNI.decode(decoder, 8000, silkData);
                System.out.println("pcm length:" + pcmData.length);
               if(pcmData.length <= 0) {
                  System.out.println("decode failed" );
                  break;
               }

               // write pcm to file
               outputStream.write(pcmData);
            }
 
        } catch (IOException ex) {
            ex.printStackTrace();
        }

      silkJNI.destroyDecoder(decoder);
   }
}