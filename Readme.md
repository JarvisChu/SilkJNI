# Silk codec for JNI

>[跳转到中文版本](./Readme_zh.md)

Silk encoder and decoder for JNI

## Files

```
tree -L 2
.
├── Readme.md
├── Readme_zh.md
├── audio                              # audio resources
│   ├── 8000_16bit_1channel.pcm
│   └── 8000_16bit_1channel_20ms.silk
├── jni                                # silk jni
│   ├── Readme.md
│   ├── build_jni.sh
│   ├── release                        # contains jni libs already built for Mac/Centos64
│   ├── src
│   └── test
└── libsilk                            # libsilk.so
    ├── build_libsilk.sh
    ├── release                        #
    ├── src                            # silk codec source code, copy from https://github.com/ploverlake/silk
    └── test                           # test for libsilk.so


```

## How to use

### 1. build silk shared library: `libsilk.so`

```bash
cd libsilk/
./build_libsilk.sh
```

This will build libsilk.so into the `libsilk/release` directory along with header files (i.e. `release/interface`).

You can test libsilk.so using `test/test_libsilk.sh`

```bash
cd libsilk/test
./test_libsilk.sh
```

`test_libsilk.sh` will generate a silk decoder `decoder` and a silk encoder `encoder` based on `libsilk.so`. Then using the `encoder` to encode `audio/8000_16bit_1channel.pcm` into silk audio, and the `decoder` to decode `audio/8000_16bit_1channel_20ms.silk` into pcm audio

### 2. build silk jni library: `libsilkjni.so`

```bash
cd jni/
./build_jni.sh 
./test_jni.sh
```

`build_jni.sh` will build libsilkjni.so (libsilkjni.jnilib on Mac)
`test_jni.sh` will run TestJNI.java, and decode silk to pcm using libsilkjni.so

> There are jni libs already built for Mac/Linux, you can use directly if satisfied your enviroments
> - **jni/release/mac_jdk1.8**: Mac Big Sur, jdk 1.8
> - **jni/release/linux_64_jdk1.8**: centos7 64bit, jdk 1.8

## Example

Please refer to `jni/test/TestJNI.java.`

```java
public class TestJNI {

   static {
      System.loadLibrary("silkjni"); // Load native library at runtime
   }

   public static void TestGetVersion()
   {
      SilkJNI silkJNI = new SilkJNI();
      String v = silkJNI.getVersion();
      System.out.println("version is:" + v);
   }

   // decode raw silk data
   public static void TestDecodeRawSilk()
   {
      SilkJNI silkJNI = new SilkJNI();
      long decoder = silkJNI.createDecoder();
      byte[] pcmData = silkJNI.decodeRaw(decoder, sampleRate, silkData);
      silkJNI.destroyDecoder(decoder);
   }

   // decode silk with standard package format, [len+data][len+data][...]
   public static void TestDecodeFormatedSilk()
   {
      SilkJNI silkJNI = new SilkJNI();
      long decoder = silkJNI.createDecoder();
      byte[] pcmData = silkJNI.decode(decoder, sampleRate, silkData);
      silkJNI.destroyDecoder(decoder);
   }

   // encode silk audio (with standard package format) to pcm audio.
   public static void TestEncode()
   {
      SilkJNI silkJNI = new SilkJNI();
      long encoder = silkJNI.createEncoder();
      byte[] silkData = silkJNI.encode(encoder, sampleRate, sampleBits, channel, pcmData);
      silkJNI.destroyEncoder(encoder);
   }

   public static void main(String[] args) {
      TestGetVersion();
      TestDecodeRawSilk();
      TestDecodeFormatedSilk();
      TestEncode();
   }
}
```