import java.io.*;
import java.util.Arrays;
import java.nio.*;

public class TestJNI {

   static {
      System.loadLibrary("silkjni"); // Load native library at runtime
   }

   public static void TestJNI(){
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
            //InputStream inputStream = new FileInputStream("8000_16bit_1channel.silk");
            InputStream inputStream = new FileInputStream("2021_04_07_16_23_43.silk");
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

   // silk buffer format: [len+data][len+data][....]
   // return pcm buffer decoded
   public static byte[] DecoderFormatedSilk(byte[] formatedSilkBuffer){
      SilkJNI silkJNI = new SilkJNI();

      // create decoder
      long decoder = silkJNI.createDecoder();
      System.out.println("decoder is:" + decoder);

      int len = formatedSilkBuffer.length;
      int cur = 0;
      int remain = len - cur;

      try(
         ByteArrayOutputStream output = new ByteArrayOutputStream();
      ){

         while(remain >= 2)
         {
            byte[] newSizeBytes = new byte[]{ formatedSilkBuffer[cur+1], formatedSilkBuffer[cur]}; // convert little-endian 2 big-endian
            ByteBuffer wrapped = ByteBuffer.wrap(newSizeBytes); // big-endian by default
            short sz = wrapped.getShort();
            System.out.println("sz:  " + sz);
            if(sz <= 0){
               System.out.println("Read Over, sz:  " + sz );
               break;
            }
            cur += 2;
            remain = len - cur;

            if(remain < sz){
               System.out.println("Read Over, data not enough, sz:  " + sz + ", remain:" + remain);
               break;
            }

            byte[] silkData = new byte[sz];
            for(int i=0;i<sz; i++){
               silkData[i] = formatedSilkBuffer[cur+i];
            }
            cur += sz;
            remain = len - cur;

            // decoder
            byte[] pcmData = silkJNI.decode(decoder, 8000, silkData);
            System.out.println("pcm length:" + pcmData.length);
            if(pcmData.length <= 0) {
               System.out.println("decode failed" );
               break;
            }

            output.write(pcmData);
         }

         byte[] out = output.toByteArray();
         return out;

      }catch (IOException ex) {
         ex.printStackTrace();
     }

     byte[] out = new byte[0];
     return out; 
   }

   public static void TestDecodeFormatedSILK(){
      // test decoder
      try (
            //InputStream inputStream = new FileInputStream("8000_16bit_1channel.silk");
            InputStream inputStream = new FileInputStream("2021_04_07_16_23_43.silk"); // 8000_16bit_1channel_20ms.silk
            //InputStream inputStream = new FileInputStream("8000_16bit_1channel_20ms.silk"); // 8000_16bit_1channel_20ms.silk
            OutputStream outputStream = new FileOutputStream("out1.pcm");
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

            // read whole silk data as buffer to decode
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] tmp = new byte[100];
            while(true){
               int byteRead = inputStream.read(tmp);
               if(byteRead > 0){
                  output.write(tmp, 0, byteRead);
               }
               if(byteRead < 100){
                  System.out.println("read over");
                  break;
               }
            }

            byte[] formatedSilkBuffer = output.toByteArray();
            byte[] pcmData = DecoderFormatedSilk(formatedSilkBuffer);

            // write to file
            if(pcmData.length > 0){
               outputStream.write(pcmData);
            }         
 
        } catch (IOException ex) {
            ex.printStackTrace();
        }
   }

   public static void main(String[] args) {
      TestJNI();
      //TestDecodeFormatedSILK();
   }   
}