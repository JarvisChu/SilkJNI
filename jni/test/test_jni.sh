#!/bin/bash

rm *.pcm *.silk *.class

cp ../../audio/* .
cp ../release/libsilk* .
cp ../../libsilk/release/libsilk.so .
cp ../src/* .

javac TestJNI.java
java -Djava.library.path=. TestJNI
