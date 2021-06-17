# Silk codec for JNI

> [Goto English](./Readme.md)

Silk 编解码的JNI实现

## 目录结构

```
tree -L 2
.
├── Readme.md
├── Readme_zh.md
├── audio                              # 音频资源
│   ├── 8000_16bit_1channel.pcm
│   └── 8000_16bit_1channel_20ms.silk
├── jni                                # silk jni的源码
│   ├── Readme.md
│   ├── build_jni.sh
│   ├── release                        # 包含了mac和linux两个版本已经编好的jni库
│   ├── src
│   └── test
└── libsilk                            # libsilk.so 的源码
    ├── build_libsilk.sh
    ├── release                        #
    ├── src                            # silk编解码器的源码，来自 https://github.com/ploverlake/silk
    └── test                           # test for libsilk.so
```

## 使用说明

### 1. 编译silk的动态库 `libsilk.so`

```bash
cd libsilk/
./build_libsilk.sh
```

编译完成之后，生成的动态库libsilk.so及相关的头文件存放在`libsilk/release`目录

接下来，可以使用`test/test_libsilk.sh`来验证libsilk.so的可用性。

```bash
cd libsilk/test
./test_libsilk.sh
```

运行完成，会使用libsilk.so 生成一个`decoder`程序和`encoder`程序，并且使用他们完成了音频的编解码。
如果一切顺利，那么说明编译出来的libsilk.so是正确的、可用的。

### 2. 编译silk的jni库 `libsilkjni.so`

```bash
cd jni/
./build_jni.sh
./test_jni.sh
```

`build_jni.sh` 会生成 libsilkjni.so (Mac上是：libsilkjni.jnilib)
`test_jni.sh` 会运行 TestJNI.java, 使用libsilkjni.so完成silk的编解码

> 库中已包含了两份编译结果，如果满足的话，直接使用即可，不用重复编译
> - **jni/release/mac_jdk1.8**: Mac Big Sur, jdk 1.8
> - **jni/release/linux_64_jdk1.8**: centos7 64bit, jdk 1.8

## 示例
详见 `jni/test/TestJNI.java`

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