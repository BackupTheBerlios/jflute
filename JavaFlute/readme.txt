
JavaFlute 
(Homepage: http://jflute.berlios.de/)

This is the Java implementation of the FLUTE transfer protocol.
It is based on the C-implementation of FLUTE available from
the MCLv3 OpenSource project (though the code we used is 
actually based on the mcl_v2.99 version).
The homepage of that project is:
http://www.inrialpes.fr/planete/people/roca/mcl/mcl.html 

The FLUTE software uses the the ALC (Asynchronous Layered 
Coding) / LCT (Layered Coding Transport) multicast protocol.
The implementation of this protocol is available as a library
from the MCLv3 project. To be able to access this library from
Java, an JNI layer for the library was implemented. This is
available as part of thew JFlute project (for the homepage,
see the top of this file).

Compiling the files is staightforward:
javac DOMwriter.java   -classpath .;xerces.jar;../mcl_jni/
javac FDT.java   -classpath .;xerces.jar;../mcl_jni/
javac flute.java -classpath .;xerces.jar;../mcl_jni/
javac frecv.java -classpath .;xerces.jar;../mcl_jni/
javac fsend.java -classpath .;xerces.jar;../mcl_jni/
javac ffile.java -classpath .;xerces.jar;../mcl_jni/
The software needs Xerces to parse the XML files. (FLUTE file transfer
sends information about the transmitted files as XML data.)

The same FLUTE class is used for sending and receiving.

To start in receiving mode, a typical call would look like:
java  -classpath .;xerces.jar;../mcl_jni/ flute  -recv -a<machine>/<port>

To send a file tree, the call would look like:
java  -classpath .;xerces.jar;../mcl_jni/ flute -a<machine>/<port> -R -send testtree

In more detail, the options for FLUTE are:

 nFLUTE Multicast File Transfert Tool 

Usage: java  -classpath .;xerces.jar;../mcl_jni/ flute [options] file|directory (in recursive mode) 
  COMMON OPTIONS 
     -h[elp]         this help 
     -send or -recv  choose Flute mode:  sender or receiver. 
     -an[/p]         set uni/multicast address or name to n and 
                     port number to p (default 127.0.0.1/"+port);
     -ifn            the network interface to use is the one attached to the 
                     local address n (only used on multi-homed hosts/routers) 
     -demuxn         set the LCT Transport Session Id (TSI) to n (default 0) 
                     - at a sender TSI is included in each packet sent 
                     - at a receiver {src_addr; TSI} is used for packet filtering 
     -ln             set number of layers to n 
     -singlelayer    optimize transmissions for single layer. 
                     must be set by both sender/receiver! 
     -vn             set (MCL) verbosity level to n (add statistics too) 
     -statn          set (MCL) statistic level to n (0: none, 1: final, 2: all) 
     -silent         silent mode 
     -tmpdir         the temporary directory is dir (string) 
                     (unix default: \"/tmp\") 
     -plow           tx profile for Low  Speed Internet 
     -pmed           tx profile for Med  Speed Internet (default) 
     -phigh          tx profile for High Speed Internet 
     -plan           tx profile for High Speed LAN 
     -psize[/rate]   manual tx profile specification, 
                     size is the datagram size (bytes) (used by sender/recv) 
                     rate is the base layer tx rate (bits/s) (used by sender) 
     -ospeed         use it to optimize speed (default) 
     -ospace         use it to reduce the required memory at receivers 
     -ocpu           use it if receiver is CPU limited 
     -P              Request user input (pause) before exiting (for win console) 
  SENDER SPECIFIC OPTIONS 
     -tn             set the ttl (time to live) to n (default 1) 
     -R              use recursive mode 
     -cont           continuous delivery mode (same as -repeat) 
                     also known as ``on-demand'' mode (default is ``push'') 
     -repeatn        repeat n times on each layer then stop 
                     ignored in ``on-demand mode'' 
     -fecn           set FEC ratio to n (float, must be >= 1.0) (default 2.0) 
                     (NB: 1.0 means no FEC as this is the n/k ratio) 
  RECEIVER SPECIFIC OPTIONS 
     -srcn          set the source address or name to n (default 0) 
                    {src_addr; TSI} is used for incoming packet filtering. 
     -int           interactive mode (select file to be received) 
                    default is to receive all files 

