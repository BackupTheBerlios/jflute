
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

import  java.io.FileInputStream;
import  java.io.InputStreamReader;
import  java.net.URL;
import  java.net.InetAddress;

class mclsend {

   static int id=0;
   static int port=2324;
   static int layer=8;
   static int ttl=5;
   static int verbose=-1;
   static MCL_JNI mcl;
   static FileInputStream fd=null; 
   static InetAddress addr=null;
   
      
public static void main (String args[]){

   mcl = new MCL_JNI();
   
   parse_commandline(args);
   
   send();
  
  }

static void send(){
 int len;
 int sent=0;

 id = mcl.mcl_open("w");
    if(id<0){
       System.err.println("send: mcl_open failed");
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
  try {
   len=fd.read(buf,0,31744);
  } catch (Exception ex) {}

   if(len>=0){
   
    result = mcl.mcl_send(id,buf,len);

    sent=sent+len;
    
    if(result<0){
       System.err.println("send: read failed, returned "+len);
    }
   }
  }

   System.out.println("send: "+sent+" bytes sent");
   
  mcl.mcl_close(id);
  try { fd.close(); } catch (Exception ex) {}
}



static void parse_commandline (String args[]){

  int argc=args.length;
  
  if(args.length<1)usage(args);
  
  /* last arg is filename, unless it is a call for help */
  if(!args[argc-1].equals("-h")&&!args[argc-1].equals("-help")&&
     !args[argc-1].equals("/h")&&!args[argc-1].equals("/?")){
        try {
          //  URL theUrl = new URL(args[argc-1]);
          //  fd = new BufferedReader(new InputStreamReader(theUrl.openStream()));
          fd=new FileInputStream(args[argc-1]);
        } catch (Exception ex) {
            System.err.println("Error while parsing command line: ");
            System.err.println("Unable to open file \""+args[argc-1]+"\"");
            System.exit(0);
        }
        argc--;
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
         System.err.println(" Verbosity "+verbose);   
     }
     
     argc--;
   } // while (argc>0)

     
}


static void usage (String args[]){
  if(id == 0 ){ // need an MCL endpoint first 
    id=mcl.mcl_open("w");
    if(id<0){
      System.err.println("usage: mcl_open failed");
      System.exit(0);
     }
    }
    mcl.mcl_ctl_MOREABOUT(id);
    System.out.println("\n\nUsage: java mclsend [options] file_to_tx");
    System.out.println("     -h[elp]   this help");
    System.out.println("     -an[/p]   set uni/multicast address or name to n and");
    System.out.println("               port number to p (default 127.0.0.1/"+port);
    System.out.println("     -vn       set verbosity level to n");
    System.exit(0);
}

}
