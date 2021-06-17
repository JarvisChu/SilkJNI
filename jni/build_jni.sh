#!/bin/bash

cd src

rm SilkJNI.class SilkJNI.h

javac SilkJNI.java # -> SilkJNI.class 
javah SilkJNI      # -> SilkJNI.h

os=`uname`
if [[ $os == 'Linux' ]]; then
    g++ -I/usr/java/jdk1.8.0_281-amd64/include/ \
        -I /usr/java/jdk1.8.0_281-amd64/include/linux \
        -I../../libsilk/release/interface \
        -L../../libsilk/release -lsilk \
        -fPIC --shared SilkJNI.cpp -o libsilkjni.so
    mv  libsilkjni.so ../release    
elif [[ $os == 'Darwin' ]]; then
    g++ -I/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/include/ \
        -I/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/include/darwin \
        -I../../libsilk/release/interface \
        -L../../libsilk/release -lsilk \
        -fPIC --shared SilkJNI.cpp -o libsilkjni.jnilib
    mv  libsilkjni.jnilib ../release
fi

