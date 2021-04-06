# SILK JNI

## 目录结构

```
.
|-- jni      : libsilkjni.so库和源码，以及demo
|-- libsilk  : libsilk.so库及demo
`-- silksrc  : silk c 源码，从 https://github.com/ploverlake/silk 拷贝而来
```

## 使用说明

1. 进入silksrc，编译silk的动态库

```bash
cd silksrc/SILK_SDK_SRC_FLP_v1.0.9/
./make_and_cp.sh
```

编译完成之后，会将生成的动态库libsilk.so 以及相关的头文件拷贝到libsilk中

> 库中已包含了一份编译结果，【环境：centos7 64bit  && jdk 1.8】
> **如果满足的话，直接使用即可，不用重复编译**

2. 进入libsilk，验证libsilk.so的可用性

```bash
cd libsilk/
./test_decoder.sh
```

运行完成，会使用libsilk.so 生成一个`decoder`程序，并且使用`decoder`程序将 `8000_16bit_1channel.silk` 解码成 `out.pcm`

> 这一个步骤的主要目的是验证编译出来的libsilk.so是正确和可用的。
> **对于JNI的使用者，直接忽略这一步**

3. 进入jni，编译libsilkjni.so 和相关demo

> 注意：因为libsilkjni.so 依赖libsilk.so，所以请将上一步的libsilk.so放到可以找到的目录中，如/usr/lib64.

```bash
cd jni/
./build_jni.sh # 编译得到一个libsilkjni.so文件
./test_jni.sh # 运行demo
```

> 库中已经包含了一个编译结果，即libsilkjni.so, 【环境：centos7 64bit  && jdk 1.8】
> **如果满足的话，直接使用即可，不用重复编译**
> 使用方法参考 `TestJNI.java`
