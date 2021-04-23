rm -rf out.pcm TestJNI.class
javac TestJNI.java
java -Djava.library.path=. TestJNI
