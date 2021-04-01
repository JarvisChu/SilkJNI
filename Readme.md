# silk coder

- silksrc: silk clang source code, copy from https://github.com/ploverlake/silk
- libsilk: libsilk.so and header files build from silksrc. and demo code for use libsilk.so
- jni: silk jni

## how to use

1. build silk shared library: libsilk.so

```bash
cd silksrc/SILK_SDK_SRC_FLP_v1.0.9/
./make_and_cp.sh
```

`make_and_cp.sh` will build libsilk.so, then copy libsilk.so and header files (interface directory) to libsilk directory

2. test libsilk.so

```bash
cd libsilk/
./test_decoder.sh
```

`test_decoder.sh` will generate a silk decoder named `decoder`, and then use the `decoder` to decode `8000_16bit_1channel.silk` to `out.pcm`

3. build silk jni library: libsilkjni.so

```bash
cd jni/
./build_jni.sh 
./test_jni.sh
```

`build_jni.sh` will build libsilkjni.so
`test_jni.sh` will run TestJNI.java, and decode silk to pcm using libsilkjni.so
