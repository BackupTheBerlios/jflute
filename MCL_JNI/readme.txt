
The main purpose of the project was to provide the capability of using
the FLUTE transfer protocol from Java.

FLUTE is based on the ALC (Asynchronous Layered Coding) / LCT (Layered 
Coding Transport) multicast protocol. For timing and cost reasons, it
was decided not to attempt an implementation of the underlying layers,
but to use an existing Open Source implementation.

We decided to use the libraries created in the MCLv3 project (though
the code we used is actually based on the mcl_v2.99 version).
The homepage is http://www.inrialpes.fr/planete/people/roca/mcl/mcl.html 

The code is written in C/C++. We decided not to attempt to make a port
to Java, but to build create an JNI (Java Native Interface) to be able
to use the libraries from the MCLv3 directly.

Compilation is straightforward:
javac mcl\MCL_JNI.java
javah -jni mcl.MCL_JNI
javac mclsend.java -classpath .
javac mclrecv.java -classpath .
(The last two lines create two small test programs.)

Testing: 
The original MCLv3 distribution contains two small programs,
mclsend and mclrecv, which can be used for testing purposes.

These programms were ported to Java to be able to test the
JNI interface to the library and also to check interworking
with the original C-based versions of the same programs.

The receiver needs to be started with the adress and port of
the machine it wants to receive a file from:
java mclrecv -v3 -a<machine>/<port>

The sender needs to be started with the adress of the machine to
send to and the port on which to send. It also needs the name 
of the file to send.
java mclsend -v3 -a<machine>/<port> testtext.txt
The file will then be transfered to the receiver and stored 
there as 'mclftp.1'.

Due to internal buffer size, the file needs to be smaller than
31744 bytes. (This is obviously a test tool to check whether
the basic communication layers work, not an a practical file
transfer routine. Extending the software to allow transfers of
files that are larger than one buffer would have been easy, but
sacrificed compatibility with the test tools that come with 
the MCLv3 software. Since the main purpose of porting the test
tools to Java was to check interoperability with the existing
tools, the buffer handling was kept unchanged.)


