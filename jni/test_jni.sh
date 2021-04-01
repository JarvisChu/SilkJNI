rm -rf out.pcm
javac TestJNI.java
java -Djava.library.path=. TestJNI
