
import java.io.File;
import java.io.FileInputStream;
import  org.w3c.dom.*;
import mcl.MCL_JNI;

class fsend {

int fec_code = flute.mcl.MCL_FEC_CODE_NULL; /* FEC code that will be used by the sender */
        /* The current version uses a single FEC code for the whole */
        /* session even if FEC can be specified on a per-object basis. */
int max_fragment_size;  /* Big files are fragmented into fragments of this
                           size */
int toiindex=0;
Document fdtinstance;



void FluteSend ()
{
        int mcl_max_block_size=0; /* maximum size of a source block in bytes; */
                                /* depends on the FEC codec used and its */
                                /* limitations on the "k" parameter */
        int Bytes_sent = 0;

        int mcl_option;
        
//cf        pthread_mutex_lock(&flutemutex);

        fdtinstance = flute.fdt.createNewFDTinstance();
        
//cf        pthread_mutex_unlock(&flutemutex);

        if (flute.fec_ratio > 1.0) {

                /*
                 * The user asked for FEC, so choose the codec.
                 * Try LDGM first, and if not applicable, revert to RSE                 
                 */

                fec_code = flute.mcl.MCL_FEC_CODE_LDGM;
                if (flute.mcl.mcl_ctl_SET_FEC_CODE(flute.id, fec_code)<0)
                {
                        fec_code = flute.mcl.MCL_FEC_CODE_RSE;
                        System.err.println("fluteSend: LDGM cpde not available, reverting to RSE");                        
                        
                        if (flute.mcl.mcl_ctl_SET_FEC_CODE(flute.id, fec_code)<0) {
                                System.err.println("fluteSend: no FEC codec available");
                                System.exit(0);    
                        }
                }
                if (flute.mcl.mcl_ctl_FEC_RATIO(flute.id, flute.fec_ratio)<0){
                                System.err.println("fluteSend: mcl_ctl failed for FEC_RATIO");
                                System.exit(0);    
                 }
        } else {
                /*
                 * No FEC packet, so set the FEC code to NULL.
                 */
                fec_code = flute.mcl.MCL_FEC_CODE_NULL;
                 if (flute.mcl.mcl_ctl_SET_FEC_CODE(flute.id, fec_code)<0) {
                        System.err.println("fluteSend: FEC codec NULL not available");
                        System.exit(0);    
                }       
        }
        /*
         * Determine the maximum block size.
         * This maximum is defined by the MCL library, depending on
         * the FEC code in use, but it must not exceed the value
         * specified in the * XXX_MAX_FRAGMENT_SIZE constant.
         */
        if (flute.mcl.mcl_ctl_GET_MAX_BLOCK_SIZE_FOR_CURRENT_FEC(flute.id, mcl_max_block_size)<0){
                System.err.println("fluteSend: mcl_ctl failed for MCL_OPT_GET_MAX_BLOCK_SIZE_FOR_CURRENT_FEC");
                System.exit(0);    
        }
        
        max_fragment_size = mcl_max_block_size - flute.MAX_TRAILER_SIZE; 
       
        if(fec_code == flute.mcl.MCL_FEC_CODE_RSE){
                if(flute.RSE_MAX_FRAGMENT_SIZE<max_fragment_size)
                      max_fragment_size=flute.RSE_MAX_FRAGMENT_SIZE;                
        }
        else if(fec_code == flute.mcl.MCL_FEC_CODE_LDGM){
                if(flute.LDPC_MAX_FRAGMENT_SIZE<max_fragment_size)
                      max_fragment_size=flute.LDPC_MAX_FRAGMENT_SIZE;
        }
        else {
                if(flute.NO_FEC_MAX_FRAGMENT_SIZE<max_fragment_size)
                      max_fragment_size=flute.NO_FEC_MAX_FRAGMENT_SIZE;
        }
        /*
         * NB: always use LCT1 now, in all cases...
         */
        mcl_option = flute.mcl.MCL_SCHED_LCT1;
        if (flute.mcl.mcl_ctl_SCHED(flute.id, mcl_option)<0) {
                 System.err.println("fluteSend: mcl_ctl MCL_OPT_SCHED failed for LCT1");
                 System.exit(0);    
        }
        if (flute.optimode == flute.OPTIMIZE_SPACE)
        {
                mcl_option = flute.mcl.MCL_SCHED_PARTIALLY_MIXED_ORDER;
                if (flute.mcl.mcl_ctl_OBJ_SCHED(flute.id, mcl_option)<0){
                        System.err.println("fluteSend: mcl_ctl MCL_OPT_SCHED failed");
                        System.exit(0);    
                }
        }
        else if (flute.optimode == flute.OPTIMIZE_SPEED)
        {
                mcl_option = flute.mcl.MCL_SCHED_MIXED_ORDER;
                if (flute.mcl.mcl_ctl_OBJ_SCHED(flute.id, mcl_option)<0){
                        System.err.println("fluteSend: mcl_ctl MCL_OPT_SCHED failed");
                        System.exit(0);    
                }
        }
        else if (flute.optimode == flute.OPTIMIZE_CPU)
        {
                mcl_option = flute.mcl.MCL_SCHED_MIXED_ORDER;
                if (flute.mcl.mcl_ctl_OBJ_SCHED(flute.id, mcl_option)<0){
                        System.err.println("fluteSend: mcl_ctl MCL_OPT_SCHED failed");
                        System.exit(0);    
                }
        }
        else
        {
                System.err.println("fluteSend: invalid optimization mode!");
                System.exit(0);    
        }

        if (flute.mcl.mcl_ctl_NO_NONEWADU(flute.id)<0){
                System.err.println("fluteSend: mcl_ctl MCL_OPT_NO_NONEWADU failed");
                System.exit(0);    
        }

/*
        mcl_option = 1;
        flute.mcl.mcl_ctl_REUSE_APPLI_TX_BUFFER(flute.id, mcl_option);
*/
        
        if (flute.recursive==1) {

                int fdtsize=0;
                
                max_fragment_size = flute.RSE_MAX_FRAGMENT_SIZE;
                
                if(flute.LDPC_MAX_FRAGMENT_SIZE>max_fragment_size)max_fragment_size=flute.LDPC_MAX_FRAGMENT_SIZE;
                
                if(flute.NO_FEC_MAX_FRAGMENT_SIZE>max_fragment_size)max_fragment_size=flute.NO_FEC_MAX_FRAGMENT_SIZE;        
              
                
                if(flute.mcl.mcl_ctl_KEEP_DATA(flute.id)<0){
                    System.err.println("fluteSend: mcl_ctl KEEP_DATA failed");
                    System.exit(0);    
                }

                Bytes_sent = RecursiveSend(flute.fileparam);


               int mcl_option_temp=0;
                if (flute.mcl.mcl_ctl_SET_NEXT_TOI(flute.id, mcl_option_temp)<0){
                    System.err.println("fluteSend: mcl_ctl MCL_OPT_SET_NEXT_TOI failed");
                    System.exit(0);    
                }
                
//cf                pthread_mutex_lock(&flutemutex);
                
                fdtsize=flute.fdt.getAndSendFinalFDTInstance(fdtinstance);
                
//cf                pthread_mutex_unlock(&flutemutex);

                if(flute.mcl.mcl_ctl_PUSH_DATA(flute.id)<0){
                    System.err.println("fluteSend: mcl_ctl PUSH_DATA failed");
                    System.exit(0);    
                }

        }
        else
        {
        
                int fdtsize=0;
                
                max_fragment_size = flute.RSE_MAX_FRAGMENT_SIZE;
                
                if(flute.LDPC_MAX_FRAGMENT_SIZE>max_fragment_size)max_fragment_size=flute.LDPC_MAX_FRAGMENT_SIZE;
                
                if(flute.NO_FEC_MAX_FRAGMENT_SIZE>max_fragment_size)max_fragment_size=flute.NO_FEC_MAX_FRAGMENT_SIZE;        
                        
                if(flute.mcl.mcl_ctl_KEEP_DATA(flute.id)<0){
                    System.err.println("fluteSend: mcl_ctl KEEP_DATA returned an error");
                    System.exit(0);    
                }

                Bytes_sent = SendThisFile(flute.fileparam, max_fragment_size);


                int mcl_option_temp=0;
                if (flute.mcl.mcl_ctl_SET_NEXT_TOI(flute.id, mcl_option_temp)<0){
                    System.err.println("fluteSend: mcl_ctl MCL_OPT_SET_NEXT_TOI failed");
                    System.exit(0);    
                }

//cf                pthread_mutex_lock(&flutemutex);

                fdtsize=flute.fdt.getAndSendFinalFDTInstance(fdtinstance);

//cf                pthread_mutex_unlock(&flutemutex);


                if(flute.mcl.mcl_ctl_PUSH_DATA(flute.id)<0){
                    System.err.println("fluteSend: mcl_ctl PUSH_DATA failed");
                    System.exit(0);    
                }

        }

         flute.mcl.mcl_close(flute.id);

        System.out.println("FluteSend complete. "+Bytes_sent+" bytes sent");
}



int SendThisFile (String file_path, int fragment_len)
{
        FileInputStream file_to_send=null; 

        File    file                   = null;
        byte [] buf_file               = null;
        int     sent                   = 0;
        int     ObjectLength           = 0;
        String toistring               = null;
        Element el;

        toiindex++;
                if (flute.mcl.mcl_ctl_SET_NEXT_TOI(flute.id, toiindex)<0){
                    System.err.println("fluteSend: mcl_ctl MCL_OPT_SET_NEXT_TOI failed");
                    System.exit(0);    
                 }

      file = new File (file_path);
       
      if(!file.exists()){
            System.err.println("fluteSend: "+file_path+", no such file!");
            return sent;
      }
      
      if(!file.isFile()){
            System.err.println("fluteSend: "+file_path+" is not a regular file!");
            return sent;
      }

      try {
          file_to_send=new FileInputStream(file_path);
        } catch (Exception ex) {
            System.err.println("fluteSend: Unable to open file \""+file_path+"\"");
            return sent;
        }
        
       buf_file = new byte[(int)file.length()];
       
       if(buf_file==null){
              System.err.println("fluteSend: Cannot alloc memory!");
              System.exit(0);    
       }

       try {       
       ObjectLength=file_to_send.read(buf_file,0,(int)file.length());
       } catch (Exception ex) {
           System.err.println("fluteSend: read failed, returned "+ObjectLength);
           System.exit(0);    
       }

       if(ObjectLength < 0){
                    System.err.println("fluteSend: read failed, returned "+ObjectLength);
                    System.exit(0);    
       }

                
       if (flute.mcl.mcl_send(flute.id, buf_file, ObjectLength) < 0){       
                    System.err.println("fluteSend: mcl_send failed");
                    System.exit(0);    
       }                        

       sent+= ObjectLength;

       try {   file_to_send.close();    } catch (Exception ex) { }        
       
       
//cf        pthread_mutex_lock(&flutemutex);
        
        /* Updating FDT*/       
        el=flute.fdt.createNewFile(toiindex,file_path);
       
        /* Adding to fdt-instance*/
        flute.fdt.AddFileToFDTinstance(fdtinstance, toiindex);
        
//cf        pthread_mutex_unlock(&flutemutex);

        System.err.println("fluteSend: Sent file "+file_path);

        return sent;
}

int RecursiveSend (String Path)
{

        File    file                   = null;
        File    subfile                   = null;
        int total_sent = 0;
        String FullName;
        String FindString;
        
        FindString = new String(Path+"\\*");


      file = new File (Path);
       
      if(!file.exists()){
            System.err.println("fluteSend: "+Path+", no such directory!");
            return total_sent;
      }
      
      if(!file.isDirectory()){
            System.err.println("fluteSend: in recursive mode, the given parameter MUST BE a valid directory name \nAborting...");
              System.exit(0);    
      }

      String [] list = file.list();

     for(int i =0; i<list.length;i++){
          subfile = new File (Path+"\\"+list[i]);
          if(subfile.isDirectory()){
                total_sent+= RecursiveSend (Path+"\\"+list[i]);
          }
          if(subfile.isFile()){
                 total_sent += SendThisFile (Path+"\\"+list[i], max_fragment_size);
          }
     }

        return total_sent;
}

} // end of class

