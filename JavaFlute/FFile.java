
import java.io.FileOutputStream;
import mcl.MCL_JNI;

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

