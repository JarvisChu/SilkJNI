import java.io.*;
import java.util.Arrays;
import java.nio.*;

public class TestJNI {

   static {
      System.loadLibrary("silkjni"); // Load native library at runtime
   }

   public static void TestGetVersion()
   {
      SilkJNI silkJNI = new SilkJNI();
      String v = silkJNI.getVersion();
      System.out.println("\n\nSilkJNI version:" + v);
   }

   // decode raw silk data
   public static void TestDecodeRawSilk()
   {
      String silkFileName = "8000_16bit_1channel_20ms.silk";
      String pcmFileName = "out_raw.pcm";
      System.out.println("\n\nTestDecodeRaw: decoder silk file: " + silkFileName + " to pcm file: " + pcmFileName);

      SilkJNI silkJNI = new SilkJNI();

      // create decoder
      long decoder = silkJNI.createDecoder();
      System.out.println("decoder is:" + decoder);

      // test decoder
      try (
         InputStream inputStream = new FileInputStream(silkFileName);
         OutputStream outputStream = new FileOutputStream(pcmFileName);
      ) {

         // check silk file header
         String header = "#!SILK_V3";
         byte[] headerBytes = new byte[]{0,0,0,0,0,0,0,0,0};
         int bytesRead = inputStream.read(headerBytes);
         if( ! Arrays.equals(headerBytes, header.getBytes()) ){
            System.out.println("Invalid Silk File, Error: Wrong Header " + new String(headerBytes) );
            return ;
         }else{
            System.out.println("Valid Silk File Header: " + new String(headerBytes) );
         }

         // read silk data and decoder
         while(true){
            // read each silk package (len+data) and decode

            // read `len` (2 bytes)
            byte[] sizeBytes = new byte[]{0,0};
            bytesRead = inputStream.read(sizeBytes);
            if(bytesRead < 2){
               System.out.println("Read Over, bytesRead:  " + bytesRead );
               break;
            }

            byte[] newSizeBytes = new byte[]{ sizeBytes[1], sizeBytes[0]}; // convert little-endian 2 big-endian
            ByteBuffer wrapped = ByteBuffer.wrap(newSizeBytes); // big-endian by default
            short sz = wrapped.getShort();
            //System.out.println("sz:  " + sz);
            if(sz <= 0){
               //System.out.println("Read Over, sz:  " + sz );
               break;
            }

            // read `data` with length of `len` (sz)
            byte[] silkData = new byte[sz];
            bytesRead = inputStream.read(silkData);
            if(bytesRead < sz){
               System.out.println("Read Over, bytesRead:  " + bytesRead );
               break;
            }
               
            // decoder
            byte[] pcmData = silkJNI.decodeRaw(decoder, 8000, silkData);
            //System.out.println("pcm length:" + pcmData.length);
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

   // decode silk with standard package format, [len+data][len+data][...]
   public static void TestDecodeFormatedSilk()
   {
      String silkFileName = "8000_16bit_1channel_20ms.silk";
      //String silkFileName = "out.silk";
      String pcmFileName = "out_formated.pcm";
      System.out.println("\n\nTestDecodeFormatedSilk: decoder silk file: " + silkFileName + " to pcm file: " + pcmFileName);

      SilkJNI silkJNI = new SilkJNI();

      // create decoder
      long decoder = silkJNI.createDecoder();
      System.out.println("decoder is:" + decoder);

      File inputFile = new File(silkFileName);

      // test decoder
      try (
         InputStream inputStream = new FileInputStream(inputFile);
         OutputStream outputStream = new FileOutputStream(pcmFileName);
      ) {
         // check silk file header
         String header = "#!SILK_V3";
         byte[] headerBytes = new byte[]{0,0,0,0,0,0,0,0,0};
         int bytesRead = inputStream.read(headerBytes);
         if( ! Arrays.equals(headerBytes, header.getBytes()) ){
            System.out.println("Invalid Silk File, Error: Wrong Header " + new String(headerBytes) );
            return ;
         }else{
            System.out.println("Valid Silk File Header: " + new String(headerBytes) );
         }

         // read silk data and decoder
         // read whole file
         long len = inputFile.length() - 9;
         byte[] silkData = new byte[(int)len];
         bytesRead = inputStream.read(silkData);
         if(bytesRead <= 0){
            System.out.println("Read Silk File failed");
            return;
         }
            
         // decoder formated silk data
         byte[] pcmData = silkJNI.decode(decoder, 8000, silkData);
         //System.out.println("pcm length:" + pcmData.length);
         if(pcmData.length <= 0) {
            System.out.println("decode failed" );
            return;
         }

         // write pcm to file
         outputStream.write(pcmData);

      } catch (IOException ex) {
         ex.printStackTrace();
      }

      silkJNI.destroyDecoder(decoder);
   }

   // encode silk audio (with standard package format) to pcm audio.
   public static void TestEncode()
   {
      String silkFileName = "out.silk";
      String pcmFileName = "8000_16bit_1channel.pcm";
      System.out.println("\n\nTestEncode: encode pcm file: " + pcmFileName + " to silk file: " + silkFileName);

      SilkJNI silkJNI = new SilkJNI();

      // create encoder
      long encoder = silkJNI.createEncoder();
      System.out.println("encoder is:" + encoder);

      File inputFile = new File(pcmFileName);

      // test decoder
      try (
         InputStream inputStream = new FileInputStream(inputFile);
         OutputStream outputStream = new FileOutputStream(silkFileName);
      ) {

         // read pcm data and encode
         long len = inputFile.length();

         byte[] pcmData = new byte[(int)len];
         int bytesRead = inputStream.read(pcmData);
         if(bytesRead <= 0) {
            System.out.println("Read PCM File failed");
            return;
         }

         byte[] silkData = silkJNI.encode(encoder, 8000, 16, 1, pcmData);
         System.out.println("silk length:" + silkData.length);
         if(silkData.length <= 0) {
            System.out.println("encode failed" );
            return;
         }

         // write silk file header
         String header = "#!SILK_V3";
         outputStream.write(header.getBytes());

         // write silk data
         outputStream.write(silkData);
 
      } catch (IOException ex) {
         ex.printStackTrace();
      }

      silkJNI.destroyEncoder(encoder);
   }

   public static void main(String[] args) {
      TestGetVersion();
      TestDecodeRawSilk();
      TestDecodeFormatedSilk();
      TestEncode();
   }   
}