
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
 
import java.io.FileOutputStream;

class FFile {

        String          fullname;
        long            toi;                    //toi of the file
        boolean         writeIt;                // Shall we write it or not?
        FileOutputStream fd;                    // File descriptor
        long            filesize;               // File size in Bytes
        int             received;               // got the file??
        FFile           next;                   // pointer to the next File (if any).

   FFile () {
    fullname=null;
    next=null;
    fd=null;
  }

void FFileInsert(FFile p_filelist, FFile newfile )
{
        FFile List = p_filelist;

        if (List == null) {
                List = new FFile();
        }       
        else
        {
                while (List.next != null) {
                        List = List.next;
                }
                List.next = new FFile();
                List = List.next;
        }

        List.fullname = new String(newfile.fullname);
        List.writeIt = newfile.writeIt;
        List.received = newfile.received;
        List.fd = newfile.fd;
        List.toi = newfile.toi;
        List.filesize = newfile.filesize;
        List.next = null;
}

FFile FFileFind( String fullname, FFile filelist )
{
        FFile found = null;
        FFile listloop = filelist;
        
        while(listloop != null)
        {
                if( listloop.fullname.equals(fullname) ) {
                        found = listloop;
                        break;
                }
                listloop = listloop.next;
        }
        return found;
}

FFile FFileFindTOI( long toi, FFile filelist )
{
        FFile found = null;
        FFile listloop = filelist;
        
        while(listloop != null)
        {
                if(listloop.toi==toi) {
                        found = listloop;
                        break;
                }
                listloop = listloop.next;
        }
        return found;
}

void FFileRemoveTOI( long toi, FFile filelist )
{
        FFile previous = null;
        FFile listloop = filelist;
        
        if(filelist.toi==toi){
           // first one matches
           listloop=listloop.next;
           return;
        }
        
        previous=listloop;
        listloop=listloop.next;
        
        while(listloop != null)
        {
                if(listloop.toi==toi) {
                        previous.next=listloop.next;
                        return;
                }
                previous=listloop;
                listloop = listloop.next;
        }
}

void FFileRemove(  String fullname, FFile filelist )
{
        FFile previous = null;
        FFile listloop = filelist;
        
        if(filelist.fullname.equals(fullname)){
           // first one matches
           listloop=listloop.next;
           return;
        }
        
        previous=listloop;
        listloop=listloop.next;
        
        while(listloop != null)
        {
                if(listloop.fullname.equals(fullname)) {
                        previous.next=listloop.next;
                        return;
                }
                previous=listloop;
                listloop = listloop.next;
        }
}


void FFilePrintList(FFile FFList)
{
        FFile listloop = FFList;
        
        System.out.println("Files Partially received:");
                  
        while(listloop != null)
        {
                System.out.println("\t"+listloop.fullname+" \t "+listloop.toi);
                listloop = listloop.next;
        }
        System.out.println("");
}


} // end of class

