
import java.io.FileInputStream;
import java.net.InetAddress;
import mcl.MCL_JNI;

class flute {

    final static int OPTIMIZE_SPEED         = 1;
    final static int OPTIMIZE_SPACE         = 2;
    final static int OPTIMIZE_CPU           = 3;
    final static int OPTIMIZE_SINGLE_LAYER  = 4;
    final static int PROMPT  =1;
    final static int NEVER   =2;
    final static int ALWAYS  =3;
    final static int SEND                    =1;
    final static int RECV                    =2;

    final static int RSE_MAX_FRAGMENT_SIZE   = (64*1024);       // 65535 bytes
    final static int LDPC_MAX_FRAGMENT_SIZE  = (20*1024*1024);  // 20 MB                  
    final static int NO_FEC_MAX_FRAGMENT_SIZE= (1*1024*1024);  // 1 MB                   
    final static int MAX_TRAILER_SIZE        = 2050;
    
   static int id=0;
   static int verbose=0;
   static int stats=0;
   static int port=2324;
   static int demux_label=0;
   static int ttl=1;
   static float   fec_ratio = (float) 2.0;

   static int     mode = 0;
   static int     reuse_tx_buff = 1;

   static int     overwrite = ALWAYS;
   static int     interactive=0;
   static String  fileparam=null;
   static int     recursive = 0;
   static int     pause = 0;
   static int     silent = 0;
   static int     tx_huge_file = 0;
   static String  tmp_dir=null;
   static int     tmp_dir_set = 0;
   static int     txprof_set = 0;         /* set tx profile only once */
   static int     txprof_mode = MCL_JNI.MCL_TX_PROFILE_MID_RATE_INTERNET;
   static int     txprof_dt_size = 0;     /* default datagram size in bytes */
   static int     txprof_rate = 0;        /* default rate in pkts/s (converted from bps)*/

   static int     levels = 0;
   static int     nb_tx = 1;
   static int     delivery_mode = MCL_JNI.DEL_MODE_PUSH;
   static int     optimode = OPTIMIZE_SPEED;
   static int     single_layer = 1;
   
   static MCL_JNI mcl;
   static FDT fdt;
   static FileInputStream fd=null; 
   static InetAddress addr=null;
   static InetAddress src_addr=null;
   static InetAddress mcast_if=null;

   /** globals specific to flute */   
   static FFile myFiles=null;

                         
public static void main (String args[]){

   mcl = new MCL_JNI();
   fdt = new FDT();
   

   
        /* variables for display thread*/
//cf      pthread_t thread_display;

        int     err = 0;        /* return value of mcl_ctl functions */

//cf      signal(SIGINT, (sighandler_t)interrupted);

           parse_commandline(args); /* Parameters parsing... */
           

        if (mode == SEND) /*sender */
        {
                if ((id = mcl.mcl_open("w")) < 0){
                   System.err.println("FluteSend: ERROR, mcl_open failed");
                   System.exit(0);    
                }   
        }
        else if (mode == RECV) /* or receiver */
        {
                if ((id = mcl.mcl_open("r")) < 0){
                   System.err.println("FluteReceive: ERROR, mcl_open failed");
                   System.exit(0);    
                }   
        }
        else /* what? */
        {
                System.err.println("FluteReceive: ERROR, mcl_open failed");
                usage(args);
                return;
        }

        /* Initialize mutex & fdt and start display thread */
        fdt.createFDT();
//cf      pthread_mutex_init(&flutemutex,NULL);
//cf      if (verbose==0) pthread_create(&thread_display, NULL, display, (void *)NULL);   

        /* specify few important parameters... */
        if (verbose > 0)
        {
                err += mcl.mcl_ctl_VERBOSITY(id, verbose);
                if(err!=0){                   
                    System.err.println("Flute: ERROR, mcl_ctl failed for VERBOSITY");
                    System.exit(0);    
                }
        }

        if (stats > 0)
        {
                err += mcl.mcl_ctl_STATS(id, stats);
                if(err!=0){                   
                    System.err.println("Flute: ERROR, mcl_ctl failed for STATS");
                    System.exit(0);    
                }
        }


        err += mcl.mcl_ctl_TX_PROFILE(id, txprof_mode);
        if(err!=0){                   
            System.err.println("Flute: ERROR, mcl_ctl failed for TX_PROFILE");
            System.exit(0);    
        }
        
        if (single_layer > 0) {         /* must be before TX_PROFILE */ /* ??? */
                err += mcl.mcl_ctl_SINGLE_LAYER(id, single_layer);
                if(err!=0){                   
                    System.err.println("Flute: ERROR, mcl_ctl failed for SINGLE_LAYER");
                    System.exit(0);    
                }
        }

        if (txprof_dt_size == 0) {
                err += mcl.mcl_ctl_TX_PROFILE(id, txprof_mode);
                if(err!=0){                   
                    System.err.println("Flute: ERROR, mcl_ctl failed for TX_PROFILE");
                    System.exit(0);    
                }
        }

        if (txprof_dt_size > 0) {       /* must be after TX_PROFILE */
                err += mcl.mcl_ctl_DATAGRAM_SIZE(id, txprof_dt_size);
                if(err!=0){                   
                    System.err.println("Flute: ERROR, mcl_ctl failed for DATAGRAM_SIZE "+txprof_dt_size);
                    System.exit(0);    
                }
        }

        if (txprof_rate > 0) {          /* must be after TX_PROFILE */
                err += mcl.mcl_ctl_TX_RATE(id, txprof_rate);
                if(err!=0){                   
                    System.err.println("Flute: ERROR, mcl_ctl failed for TX_RATE "+txprof_rate);
                    System.exit(0);    
                }
        }

        if (levels > 0) {
                err += mcl.mcl_ctl_LAYER_NB(id, levels);
                if(err!=0){                   
                    System.err.println("Flute: ERROR, mcl_ctl failed for LAYER_NB");
                    System.exit(0);    
                }
        }

        if (port > 0) {         /* in host format! */
                err += mcl.mcl_ctl_PORT(id, port);
                if(err!=0){                   
                    System.err.println("Flute: ERROR, mcl_ctl failed for PORT");
                    System.exit(0);    
                }
        }
        
        
        if (addr != null) {         /* in host format! */
                err += mcl.mcl_ctl_ADDR(id,addr.getHostName());
                if(err!=0){                   
                    System.err.println("Flute: ERROR, mcl_ctl failed for ADDR");
                    System.exit(0);    
                }
        }
        
        if (mcast_if !=null) {             /* in host format! */
                err += mcl.mcl_ctl_ADDR(id,mcast_if.getHostName());
                if(err!=0){                   
                    System.err.println("Flute: ERROR, mcl_ctl failed for NETIF");
                    System.exit(0);    
                }
        }

        if (demux_label > 0) {
                err += mcl.mcl_ctl_DEMUX_LABEL(id, demux_label);
                if(err!=0){                   
                    System.err.println("Flute: ERROR, mcl_ctl failed for DEMUX_LABEL");
                    System.exit(0);    
                }
        }

        if (ttl > 0) {
                err += mcl.mcl_ctl_TTL(id, ttl);
                if(err!=0){                   
                    System.err.println("Flute: ERROR, mcl_ctl failed for TTL");
                    System.exit(0);    
                }
        }

        if (nb_tx > 1) {
                err += mcl.mcl_ctl_NB_OF_TX(id, nb_tx);
                if(err!=0){                   
                    System.err.println("Flute: ERROR, mcl_ctl failed for NB_OF_TX");
                    System.exit(0);    
                }
        }

        if (tmp_dir_set == 1) {
                err += mcl.mcl_ctl_TMP_DIR(id, tmp_dir);
                if(err!=0){                   
                    System.err.println("Flute: ERROR, mcl_ctl failed for TMP_DIR \""+tmp_dir+"\"");
                    System.exit(0);    
                }
        }

        err += mcl.mcl_ctl_DELIVERY_MODE(id, delivery_mode);
        if(err!=0){                   
            System.err.println("Flute: ERROR, mcl_ctl failed for DELIVERY_MODE");
            System.exit(0);    
        }

        if (mode == SEND) /*sender */
        {
            fsend fluteSender = new fsend();
            fluteSender.FluteSend();    
         }
        else if (mode == RECV) /* or receiver */
        {
            frecv fluteReceiver = new frecv();
            fluteReceiver.FluteRecv();    
        }
                
//cf      if (verbose==0) pthread_kill(thread_display,0); 
        fdt.closeFDT();

        if (pause==1) {
//cf              system("pause");
        }
  }



static void parse_commandline (String args[]){

  int argc=0;
  
  if(args.length<1)usage(args);

   while (argc< args.length){

         if(args[argc].startsWith("-a")){
           String rname=new String(args[argc].substring(2));
           // check for port extension
           if(rname.indexOf("/")!=-1){
                 port= Integer.valueOf(rname.substring(rname.indexOf("/")+1)).intValue();
                 rname= rname.substring(0,rname.indexOf("/"));
           }  // if(rname.indexOf("//")!=-1)
           try{
                addr=InetAddress.getByName(rname);
           } catch (Exception ex){
                        System.err.println("Bad argument \""+args[argc]+"\"");
                        System.exit(0);       
           }
         } // if(args[argc].startsWith("-a"))

         if(args[argc].startsWith("-demux")){
           demux_label=Integer.valueOf(args[argc].substring(6)).intValue();              
         }

         if(args[argc].startsWith("-v")){ /* verbosity level */
           verbose = Integer.valueOf(args[argc].substring(2)).intValue();    
           if(verbose>0) stats=2;          
         }

         if(args[argc].startsWith("-h")){
           usage(args);
         }

         if(args[argc].startsWith("-l")){ /* verbosity level */
           single_layer = Integer.valueOf(args[argc].substring(2)).intValue();           
         }

         if(args[argc].startsWith("-tmp")){ /* temp dir */
           tmp_dir = new String(args[argc].substring(4));    
           if(tmp_dir.charAt(tmp_dir.length()-1)!='/')tmp_dir=tmp_dir+'/';       
           tmp_dir_set = 1;
         }
         else if(args[argc].startsWith("-t")){  /* ttl value */
           ttl = Integer.valueOf(args[argc].substring(2)).intValue();           
         }

         if(args[argc].startsWith("-send")){        
                                mode = SEND;
                                }
         else if(args[argc].startsWith("-src")){  
           String rname=new String(args[argc].substring(4));         
           try{
                src_addr=InetAddress.getByName(rname);
           } catch (Exception ex){
                        System.err.println("Flute: ERROR, bad argument "+args[argc]+"\n(NB: dont use space between -stat and value)");
                        System.exit(0);       
           }
          }
         else if(args[argc].startsWith("-silent")){  
            silent=1;
            }
         else if(args[argc].startsWith("-stat")){  
           stats = Integer.valueOf(args[argc].substring(5)).intValue();   
           }        
         else if(args[argc].startsWith("-singlelayer")){  
           single_layer = 1;
           levels=1;
           }    

         if(args[argc].startsWith("-recv")){        
                               mode = RECV;
                                }             
         else if(args[argc].startsWith("-repeat")){                                  
           /* nb of tx is original_tx + repeat_nb*/
           nb_tx = Integer.valueOf(args[argc].substring(7)).intValue() + 1;   
           }

         if(args[argc].startsWith("-ospace")){        
                                optimode = OPTIMIZE_SPACE;
                                }
         if(args[argc].startsWith("-ospeed")){        
                                optimode = OPTIMIZE_SPEED;
                                }
         if(args[argc].startsWith("-ocpu")){        
                                optimode = OPTIMIZE_CPU;
                                }
         if(args[argc].startsWith("-p")){        
                        if (txprof_set==1) {
                           System.err.println("Flute: ERROR, tx profile can be set only once");
                           System.exit(0);       
                        } else {
                                txprof_set = 1;
                        }
                        if(args[argc].startsWith("-plow"))
                                txprof_mode = MCL_JNI.MCL_TX_PROFILE_LOW_RATE_INTERNET;
                        else if(args[argc].startsWith("-pmed"))
                                txprof_mode = MCL_JNI.MCL_TX_PROFILE_MID_RATE_INTERNET;
                        else if(args[argc].startsWith("-phigh"))
                                txprof_mode = MCL_JNI.MCL_TX_PROFILE_HIGH_SPEED_INTERNET;
                        else if(args[argc].startsWith("-plan")){
                                txprof_mode = MCL_JNI.MCL_TX_PROFILE_HIGH_SPEED_LAN;
                                single_layer = 1;       /* true in a LAN! */
                        }                        
                        else { /* process size/rate argument */
                            String sr_arg=new String(args[argc].substring(2));
                            String s_arg=null;
                            String r_arg=null;
                            if(sr_arg.indexOf("/")!=-1){
                                s_arg= sr_arg.substring(0,sr_arg.indexOf("/"));
                                r_arg= sr_arg.substring(sr_arg.indexOf("/")+1);
                            }
                            else s_arg=sr_arg;
                            
                            int rate=0;
                            if(r_arg!=null)rate=Integer.valueOf(r_arg).intValue();  
                            if(rate<0){
                               System.err.println("Flute: ERROR, invalid rate for argument "+args[argc]);
                               System.exit(0);       
                            }
                            txprof_dt_size = Integer.valueOf(s_arg).intValue();  
                            if (txprof_dt_size <= 0) {
                               System.err.println("Flute: ERROR, invalid rate for argument "+args[argc]);
                               System.exit(0);       
                            }
                            if (rate > 0) {
                                        txprof_rate = (int)((float)rate /
                                                ((float)txprof_dt_size * 8.0));
                                        /* no less than 1 pkt/s */
                                        if(txprof_rate<1)txprof_rate = 1;
                                }
                         }
                      }

         if(args[argc].startsWith("-R")){
             recursive = 1;
         }

         if(args[argc].startsWith("-P")){
             pause = 1;
         }

         if(args[argc].startsWith("-fec")){
             fec_ratio = Integer.valueOf(args[argc].substring(4)).floatValue();           
         }

         if(args[argc].startsWith("-cont")){
             delivery_mode = MCL_JNI.DEL_MODE_ON_DEMAND;     
         }

         if(args[argc].startsWith("-int")){
              interactive=1;
              overwrite = PROMPT;
         }

         if(args[argc].startsWith("-if")){
           String rname=new String(args[argc].substring(3));         
           try{
                mcast_if=InetAddress.getByName(rname);
           } catch (Exception ex){
                        System.err.println("Flute: ERROR, bad argument "+args[argc]+"\n(NB: dont use space between -stat and value)");
                        System.exit(0);       
           }
         }
      argc++;
    } //  while (argc< args.length)

        /* if single_layer mode is not set, the nb of fec layers
         * must be integer. Enforce it...
         */
        if (single_layer!=0) {
                int     tmp_fec_ratio;
                tmp_fec_ratio = (int)fec_ratio;
                fec_ratio = (float)tmp_fec_ratio;
        }


        /* last arg is file name with SEND*/
        if( mode == SEND )
        {
          fileparam= new String (args[args.length-1]);
        }
     
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

        System.out.println("\nFLUTE Multicast File Transfert Tool");

        System.out.println("\n\nUsage: flute [options] file|directory (in recursive mode)");
        System.out.println(" COMMON OPTIONS");
        System.out.println("    -h[elp]         this help");
        System.out.println("    -send or -recv  choose Flute mode:  sender or receiver.");
        System.out.println("    -an[/p]         set uni/multicast address or name to n and");
        System.out.println("                    port number to p (default 127.0.0.1/"+port);
        System.out.println("    -ifn            the network interface to use is the one attached to the");
        System.out.println("                    local address n (only used on multi-homed hosts/routers)");
        System.out.println("    -demuxn         set the LCT Transport Session Id (TSI) to n (default 0)");
        System.out.println("                    - at a sender TSI is included in each packet sent");
        System.out.println("                    - at a receiver {src_addr; TSI} is used for packet filtering");
        System.out.println("    -ln             set number of layers to n");
        System.out.println("    -singlelayer    optimize transmissions for single layer.");
        System.out.println("                    must be set by both sender/receiver!");
        System.out.println("    -vn             set (MCL) verbosity level to n (add statistics too)");
        System.out.println("    -statn          set (MCL) statistic level to n (0: none, 1: final, 2: all)");
        System.out.println("    -silent         silent mode");
        System.out.println("    -tmpdir         the temporary directory is dir (string)");
        System.out.println("                    (unix default: \"/tmp\")");
        System.out.println("    -plow           tx profile for Low  Speed Internet");
        System.out.println("    -pmed           tx profile for Med  Speed Internet (default)");
        System.out.println("    -phigh          tx profile for High Speed Internet");
        System.out.println("    -plan           tx profile for High Speed LAN");
        System.out.println("    -psize[/rate]   manual tx profile specification,");
        System.out.println("                    size is the datagram size (bytes) (used by sender/recv)");
        System.out.println("                    rate is the base layer tx rate (bits/s) (used by sender)");
        System.out.println("    -ospeed         use it to optimize speed (default)");
        System.out.println("    -ospace         use it to reduce the required memory at receivers");
        System.out.println("    -ocpu           use it if receiver is CPU limited");
        System.out.println("    -P              Request user input (pause) before exiting (for win console)");
        System.out.println(" SENDER SPECIFIC OPTIONS");
        System.out.println("    -tn             set the ttl (time to live) to n (default 1)");
        System.out.println("    -R              use recursive mode");
        System.out.println("    -cont           continuous delivery mode (same as -repeat)");
        System.out.println("                    also known as ``on-demand'' mode (default is ``push'')");
        System.out.println("    -repeatn        repeat n times on each layer then stop");
        System.out.println("                    ignored in ``on-demand mode''");
        System.out.println("    -fecn           set FEC ratio to n (float, must be >= 1.0) (default 2.0)");
        System.out.println("                    (NB: 1.0 means no FEC as this is the n/k ratio)");
        System.out.println(" RECEIVER SPECIFIC OPTIONS");
        System.out.println("    -srcn          set the source address or name to n (default 0)");
        System.out.println("                   {src_addr; TSI} is used for incoming packet filtering.");
        System.out.println("    -int           interactive mode (select file to be received)");
        System.out.println("                   default is to receive all files");

        System.exit(0);
}

}

