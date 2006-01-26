
/*
 *  Copyright (c) 2005-2006 Fraunhofer FOKUS
 *  (main author: Christian Fuhrhop - fuhrhop@fokus.fhg.de)
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307,
 *  USA.
 */

/* Note that this testing program is based on the C-program distributed
 * as part of the 'mclftp' tools from the MCLv3 OpenSource project. */

import  java.io.FileOutputStream;
import  java.io.InputStreamReader;
import  java.net.URL;
import  java.net.InetAddress;

class mclrecv {

   static int id=0;
   static int port=2324;
   static int layer=0;
   static int ttl=5;
   static int verbose=-1;
   static MCL_JNI mcl;
   static FileOutputStream fd=null; 
   static InetAddress addr=null;
   
      
public static void main (String args[]){

   mcl = new MCL_JNI();
   
   parse_commandline(args);
   
   recv();
  
  }

static void recv(){
 int len;
 int received=0;

 id = mcl.mcl_open("r");
    if(id<0){
       System.err.println("recv: mcl_open failed");
       System.exit(0);
    }
 /* specify a few important parameters */
 if(verbose>=0){
   int stats = 1; /* 1=> all intermediate stats */
   mcl.mcl_ctl_VERBOSITY(id, verbose);
   mcl.mcl_ctl_STATS(id, stats);
 }
 
 if(port>0)
    mcl.mcl_ctl_PORT(id, port);
    
 if(addr!=null){
   mcl.mcl_ctl_ADDR(id,addr.getHostName());
 }

 if(ttl>0)
    mcl.mcl_ctl_TTL(id, ttl);

 if(layer>0)
    mcl.mcl_ctl_LAYER(id, layer);
 
 len=1;
 byte [] buf = new byte[31744];
 int result; 
 
 while(len>=0){
 
   len = mcl.mcl_recv(id,buf,31744);
   
   if(len>0){
     received = received+len;
 
     try {
       fd.write(buf,0,len);
     } catch (Exception ex) {}
    
     System.err.println("recv: "+len+" bytes read, total="+received);
  }
 }

   System.out.println("recv: "+received+" bytes received");
   
  mcl.mcl_close(id);
  try { fd.close(); } catch (Exception ex) {}
}



static void parse_commandline (String args[]){

  int argc=args.length;
  
  String outname = new String (".\\mclftp.1");
        try { 
          fd=new FileOutputStream(outname);
        } catch (Exception ex) {
            System.err.println("Unable to open file \""+outname+"\"");
            System.exit(0);
        }
     
     
   while (argc>0){
     if(args[argc-1].startsWith("-a")){
       String rname=new String(args[argc-1].substring(2));
//       System.err.println(rname);     
       // check for port extension
       if(rname.indexOf("/")!=-1){
         port= Integer.valueOf(rname.substring(rname.indexOf("/")+1)).intValue();
         rname= rname.substring(0,rname.indexOf("/"));
       }  // if(rname.indexOf("//")!=-1)
       try{
        addr=InetAddress.getByName(rname);
         System.err.println(" Host name "+addr.getHostName());   
         System.err.println(" Host address "+addr.getHostAddress());   
       } catch (Exception ex){
            System.err.println("Bad argument \""+args[argc-1]+"\"");
            System.exit(0);       
       }
     } // if(args[argc-1].startsWith("-a"))

     if(args[argc-1].startsWith("-h")){
       usage(args);
     }

     if(args[argc-1].startsWith("-v")){  // verbosity level
       verbose=Integer.valueOf(args[argc-1].substring(2)).intValue();
     }

     if(args[argc-1].startsWith("-l")){  // number of layers level
       layer=Integer.valueOf(args[argc-1].substring(2)).intValue();
     }
     
     argc--;
   } // while (argc>0)

     
}


static void usage (String args[]){
  if(id == 0 ){ // need an MCL endpoint first 
    id=mcl.mcl_open("r");
    if(id<0){
      System.err.println("mcl_recv usage: mcl_open failed");
      System.exit(0);
     }
    }
    mcl.mcl_ctl_MOREABOUT(id);
    System.out.println("\n\nUsage: java mclrecv [options]");
    System.out.println("     Receive and save a file in .\\mclftp.1 is an unique suffix");
    System.out.println("     -h[elp]   this help");
    System.out.println("     -an[/p]   set uni/multicast address or name to n and");
    System.out.println("               port number to p (default 127.0.0.1/"+port);
    System.out.println("     -ln       set number of layer to n");
    System.out.println("     -vn       set verbosity level to n");
    System.exit(0);
}

}
