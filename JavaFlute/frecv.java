

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

/* Note that this code is based on the C-code distributed
 * as part of the MCLv3 OpenSource project. */
 
class frecv {


void FluteRecv()
{
        int mcl_option;
        
        // the following if...else...else statement seems to be partly redundant,
        // but it's a direct translation of the original Flute C code...

        if (flute.optimode == flute.OPTIMIZE_SPACE)
        {
                mcl_option = 1;
                if (flute.mcl.mcl_ctl_FLUTE_DELIVERY(flute.id)<0){
                   System.err.println("fluteReceive: MCL_OPT_FLUTE_DELIVERY failed");
                   System.exit(0);    
                }
                mcl_option = 0;
                if (flute.mcl.mcl_ctl_POSTPONE_FEC_DECODING(flute.id, mcl_option)<0){
                        /* non critical... ignore! */
                   //System.err.println("fluteReceive: MCL_OPT_POSTPONE_FEC_DECODING failed");
                   //System.exit(0);    
                  }

        }
        else if (flute.optimode == flute.OPTIMIZE_SPEED)
        {
                mcl_option = 1;
                if (flute.mcl.mcl_ctl_FLUTE_DELIVERY(flute.id)<0){
                   System.err.println("fluteReceive: MCL_OPT_FLUTE_DELIVERY failed");
                   System.exit(0);    
                }
                mcl_option = 0;
                if (flute.mcl.mcl_ctl_POSTPONE_FEC_DECODING(flute.id, mcl_option)<0){
                        /* non critical... ignore! */
                   //System.err.println("fluteReceive: MCL_OPT_POSTPONE_FEC_DECODING failed");
                   //System.exit(0);    
                  }
        }
        else if (flute.optimode == flute.OPTIMIZE_CPU)
        {
                mcl_option = 0;
                if (flute.mcl.mcl_ctl_FLUTE_DELIVERY(flute.id)<0){
                   System.err.println("fluteReceive: MCL_OPT_FLUTE_DELIVERY failed");
                   System.exit(0);    
                }
                mcl_option = 1;
                if (flute.mcl.mcl_ctl_POSTPONE_FEC_DECODING(flute.id, mcl_option)<0){
                        /* non critical... ignore! */
                   //System.err.println("fluteReceive: MCL_OPT_POSTPONE_FEC_DECODING failed");
                   //System.exit(0);    
                  }
        }
        else
        {
                   System.err.println("fluteReceive: FATAL ERROR: invalid optimization mode!");
                   System.exit(0);    
        }

        if (flute.src_addr !=null) {             /* in host format! */
                int errorcode = flute.mcl.mcl_ctl_ADDR(flute.id,flute.src_addr.getHostName());
                if(errorcode!=0){                   
                    System.err.println("fluteReceive: MCL_OPT_SRC_ADDR failed");
                    System.exit(0);    
                }
        }

        if (flute.interactive==0) 
        {
                if (flute.mcl.mcl_ctl_FLUTE_DELIVER_ALL_ADU(flute.id)<0){
                    System.err.println("fluteReceive: MCL_OPT_FLUTE_DELIVER_ALL_ADU failed");
                    System.exit(0);    
                }
        }


        System.out.println("Waiting for data...");
        RecvFiles();

        flute.mcl.mcl_close(flute.id);
        System.out.println("fluteRecv completed");
}




void RecvFiles()
{
        byte [] buf = null;       /* buffer for recv'd fragment */
        int             len             = 0;
        FFile          ThisFile=null;
        FFile          ffile=new FFile();
        int    toi = 0;
        int             max_fragment_size; /* Big files are fragmented into
                                              fragments of this size */                                     


        /* determine the maximum fragment size (pessimistic evaluation that
         * does not take FEC used into account) */
        max_fragment_size = flute.RSE_MAX_FRAGMENT_SIZE;
        if(flute.LDPC_MAX_FRAGMENT_SIZE>max_fragment_size)max_fragment_size=flute.LDPC_MAX_FRAGMENT_SIZE;
        if(flute.NO_FEC_MAX_FRAGMENT_SIZE>max_fragment_size)max_fragment_size=flute.NO_FEC_MAX_FRAGMENT_SIZE;

        /* Objects contain a file of fdt*/
        
        
       buf = new byte[max_fragment_size+flute.MAX_TRAILER_SIZE];
       
       if(buf==null){
                    System.err.println("fluteReceive: Cannot alloc memory!");
                    System.exit(0);    
        }

        /* Receiving ALL Objects... */      
        while( len != -1)
        {
          int toia[] = new int [1];
          len = flute.mcl.mcl_recv_flute(flute.id, buf, max_fragment_size, toia);
          toi = toia[0];
          
          if(len != -1) {
               
                System.out.println("New Object Received ("+len+" Bytes), TOI: "+toi);

//cf                pthread_mutex_lock(&flutemutex);
                if(toi==0) /*we have an fdt instance */
                {                                                       
                        flute.fdt.updateFDT(buf, len);
                        if (flute.interactive==0) flute.myFiles=flute.fdt.updateFFile(flute.myFiles);
                        
                }
                else if((ThisFile=ffile.FFileFindTOI(toi, flute.myFiles))!=null) /*reception of a known & selected file */
                {
                        System.out.println("Received file "+ThisFile.fullname);
                        
                        if((ThisFile.writeIt) && (ThisFile.fd != null))
                        {       
                              try {
                                ThisFile.fd.write(buf,0,len);
                              } catch (Exception ex) {
                                       System.err.println("fluteReceive: Unable to write file \""+ThisFile.fullname+"\"");
                              }
        
                                System.out.println("Writing file \""+ThisFile.fullname+"\" ("+ThisFile.filesize+" Bytes).");

                              try {
                                 ThisFile.fd.close();
                              } catch (Exception ex) {
                                       System.err.println("fluteReceive: Unable to close file \""+ThisFile.fullname+"\"");
                              }
                               
                                ThisFile.received=1;
                                // next line already commented out in original Flute
                                // ffile.FFileRemove(ThisFile.fullname,flute.myFiles);
                        }
                        else
                        {
                               System.out.println("Skipped file "+ThisFile.fullname);
                        }
                }
                else /*received unknown or unwanted file*/
                {
                         System.out.println("WARNING: Received unknown file");
                }

//cf                pthread_mutex_unlock(&flutemutex);


          } // endif (len != -1) 
        } // end while  while( len != -1)

        if(buf!=null) buf = null;

        return;
}

}
