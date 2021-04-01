# jni


## how to build

```bash
./build_jni.sh
```

## how to use

TestJNI.java is a demo for using libsilkjni.so

```bash
./test_jni.sh
```

## how to build jni 
1. write SilkJNI.java
2. `javac SilkJNI.java`  -> SilkJNI.class
3. `javah SilkJNI` -> SilkJNI.h
4. write SilkJNI.c
5. gcc -> libsilkjni.so
6. use libsilkjni.so