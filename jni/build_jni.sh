#!/bin/bash

rm SilkJNI.class SilkJNI.h

javac SilkJNI.java # -> SilkJNI.class 
javah SilkJNI      # -> SilkJNI.h
gcc -I/usr/java/jdk1.8.0_281-amd64/include/ \
    -I /usr/java/jdk1.8.0_281-amd64/include/linux \
    -I../libsilk/interface \
    -L../libsilk -lsilk \
    -fPIC --shared SilkJNI.c -o libsilkjni.so