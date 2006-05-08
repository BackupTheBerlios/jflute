import  org.w3c.dom.*;
import  org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.parsers.DOMParser;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.lang.Long;

class FDT {

Document fdt;
Element root;


 /* Creates the FDT */
 void createFDT()
 {
        /* Create a new document with FDT as root element */

        fdt= new DocumentImpl();   
        root = fdt.createElement("FDT");    
        fdt.appendChild( root ); 

        if (fdt == null) {
            System.err.println("FluteFDT: DOMImpl.createElement: failed");
            System.exit(0);    
        }
        setExpires("3333333333");
 }


 void closeFDT()
 {
        /* I free the document structure and the DOMImplementation */
        fdt=null;
        root=null;
 }


 void setExpires(String ivalue)
 {
        /* Add the Expires attribute to the root element  */
        try {
        root.setAttribute ("Expires",ivalue);
        } catch (Exception ex) {
            System.err.println("FluteFDT: Element.setAttribute: failed");
            System.exit(0);  
        }
}

long getExpires()
 {      
        /*Get the expires value from root element*/
        
       if(!root.hasAttribute("Expires")){
            System.err.println("FluteFDT: Element.getAttribute: failed for Expires");
            System.exit(0);  
       }
       
       long result = Long.valueOf(root.getAttribute("Expires")).longValue();

       return result;
 }

boolean CheckWriteContext( String filepath, int mode)
{

   File f = new File (filepath);
   
   if((f!=null)&&(f.exists())){
      if(mode==flute.ALWAYS) return true;
      if(mode==flute.NEVER) return false;
      if(mode==flute.PROMPT){
     //  prompt the user to enter their name
      System.out.print("\nFile "+ filepath + " exists, overwrite? [y/n]  ");

      //  open up standard input
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

      String yn = null;

      //  read the username from the command-line; need to use try/catch with the
      //  readLine() method
      while(true){
      try {
         yn=null;
         yn = br.readLine();
      } catch (IOException ioe) {
         System.err.println("IO error trying to read answer!");
         System.exit(1);
      }
        if((yn!=null)&&(yn.charAt(0)=='y')) return true;
        if((yn!=null)&&(yn.charAt(0)=='Y')) return true;
        if((yn!=null)&&(yn.charAt(0)=='n')) return false;
        if((yn!=null)&&(yn.charAt(0)=='N')) return false;
        System.out.print("\nFile "+ filepath + " exists, overwrite? [y/n]  ");
      }
      }
   } // endif  ((f!=null)&&(f.exists())){
      else
        {
             /* create directory for file */
             
             // first find directory path if any             
             if(f.getParentFile()==null)return true;
             
             // then create directory for parent file
             System.out.println("Creating directory "+f.getParentFile().getPath());

             File pf = new File(f.getParentFile().getPath());
             
             boolean madedir=false;
             
             try{             
               madedir=pf.mkdirs();
             } catch (Exception ex) {
                  System.err.println("Unable to create directory "+f.getParentFile().getPath());
                  System.exit(1);
             }
             if(!madedir){
                  System.err.println("Unable to create directory "+f.getParentFile().getPath());
                  System.exit(1);
             }
           return true;                  
        }
         return true;       
}
 
boolean hasTOIinFDT(Element ele, long toi){

        // this one originally uses XPath to parse the document, but since that is DOM 3 and
        // Xerces is only DOM 2, we use a different implementation
        
        // has this element an attribute TOI and the same value?    
        
        if(ele.hasAttribute("TOI")&&(ele.getAttribute("TOI").length()>0)){        
          if(toi==Long.valueOf(ele.getAttribute("TOI")).longValue())return true;
        }
       
        // if not, go through all the child elements
        NodeList childs = ele.getChildNodes ();

        if(childs==null)return false;
        
        int nchilds = childs.getLength() ;
        
        if(nchilds==0)return false;
     
        /* Update File nodes */
        for (int i = 0; i < nchilds; i++) {
                Node nodeel = childs.item(i);

                if(nodeel.getNodeType()==Node.ELEMENT_NODE)
                {
                  boolean result= hasTOIinFDT((Element) nodeel,  toi);
                  // if one of the children (or their descendants) has this TOI, we return true
                  if(result)return true;
                }
         }
         // if none of the children (and their descendants) has this TOI, we don't have it in the document
         return false;
}

String locationForTOIinFDT(Element ele, long toi){
   String result=null;
   
        // has this element an attribute TOI and the same value?
        if(ele.hasAttribute("TOI")){
          if(toi==Long.valueOf(ele.getAttribute("TOI")).longValue()){
            if(ele.hasAttribute("Content-Location"))result=new String(ele.getAttribute("Content-Location"));
            return result;
          }
        }
       
        // if not, go through all the child elements
        NodeList childs = ele.getChildNodes ();

        if(childs==null)return result;
        
        int nchilds = childs.getLength() ;
        
        if(nchilds==0)return result;

        /* Update File nodes */
        for (int i = 0; i < nchilds; i++) {
                Node nodeel = childs.item(i);
                if(nodeel.getNodeType()==Node.ELEMENT_NODE)
                {
                if(locationForTOIinFDT((Element) nodeel,  toi)!=null){
                    result= new String(locationForTOIinFDT((Element) nodeel,  toi));
                    // if one of the children (or their descendants) has this TOI, we return that result
                    if(result!=null)return result;
                  }
                }
         }
         // if none of the children (and their descendants) has this TOI, we don't have it in the document
         return result;
}
 
 

 /* Adds elementsof the FDTinstance to the FDT */
void updateFDT(byte [] xmlfdtinstance, int len){
                        
        /* Load a new document from the pointer xmlfdtinstance */
        /* This doesn't work well from memory in Java, so we first write the file to disk */
        File temp;
        Document fdtinstance=null;
        Element rootel = null;
        String tempname = new String ("temp.xml");
       
        try {
          temp = File.createTempFile("flute",".xml");
          tempname = new String (temp.getName());
          temp.delete();
            } catch (Exception ex) {
            System.err.println("FluteFDT: Unable to create temp file name");
            System.exit(0);  
          }
        
        try {
               FileOutputStream fd = new FileOutputStream(tempname);
              fd.write(xmlfdtinstance,0,len);
              fd.close();
            } catch (Exception ex) {
            System.err.println("FluteFDT: Unable to write file for xmlfdtinstance");
            System.exit(0);  
          }
                
         DOMParser parser = new DOMParser();

      //  Parse the Document     
      //  and traverse the DOM
      try {
         parser.parse(tempname);
         fdtinstance = parser.getDocument();
            } catch (Exception ex) {
            System.err.println("FluteFDT: Unable to parse file for xmlfdtinstance "+tempname);
            System.exit(0);  
          }
                    
        /* Get reference to the root element of the document */
        rootel = fdtinstance.getDocumentElement ();

        if (rootel == null) {
            System.err.println("FluteFDT: Document.documentElement: null");
            System.exit(0);  
        }

        /* Get the reference to the childrens NodeList of the root element */
        NodeList childs = rootel.getChildNodes ();
        if (childs == null) {
            System.err.println("FluteFDT: Element.childNodes: null");
            System.exit(0);  
        }

        /* Go through the node of the instance and check if it is already included in the FDT */
        int nchilds = childs.getLength() ;


        /* Update Expires node */

       if(!rootel.hasAttribute("Expires")){
            System.err.println("FluteFDT: Element.getAttribute: failed for Expires on rootel");
            System.exit(0);  
       }
        setExpires(rootel.getAttribute("Expires"));
                                        
        /* Update File nodes */
        for (int i = 0; i < nchilds; i++) {
                Node nodeel = childs.item(i);
                if (nodeel == null) {
                    System.err.println("FluteFDT: NodeList.item("+i+": null");
                    System.exit(0);  
                }

                if(nodeel.getNodeType()==Node.ELEMENT_NODE)
                {
                        Attr attr = ((Element)nodeel).getAttributeNode("TOI");                  
                        if (attr == null) {
                          System.err.println("FluteFDT: Element.getAttributeNode: null");
                          System.exit(0);  
                        }
                        String toi = new String(attr.getValue());
                        if(! hasTOIinFDT(root,Long.valueOf(toi).longValue()))
                        {                                        
                                // add this element to the root element of the fdt document
                                Node importnode = fdt.importNode(nodeel, true);
                                // Append the imported Node to the childs list of the root element
                                try {                           
                                  root.appendChild(importnode);

                                 } catch (Exception ex) {
                                         System.err.println("FluteFDT: Element.appendChild: failed");
                                          System.exit(0);  
                                 }
                        }
                }
        }

  boolean success = (new File(tempname)).delete();         
 }      

 /* Adds elements of the FDT to the FileList */
 /* ONLY in !interactive mode*/
FFile updateFFile(FFile filelist)
{
         FFile          ffile=new FFile();
 
        /* Get the reference to the childrens NodeList of the root element */
        NodeList childs = root.getChildNodes ();
        if (childs == null) {
            System.err.println("FluteFDT: Element.childNodes: null");
            System.exit(0);  
        }

        
        /* Go through the node of the instance and check if it is already included in the FFile */
        int nchilds = childs.getLength() ;

                                        
        /* Update File nodes */
        for (int i = 0; i < nchilds; i++) {
                Node nodeel = childs.item(i);
                if (nodeel == null) {
                    System.err.println("FluteFDT: NodeList.item("+i+": null");
                    System.exit(0);  
                }

                if(nodeel.getNodeType()==Node.ELEMENT_NODE)
                {
                        Attr attr = ((Element)nodeel).getAttributeNode("TOI");                  
                        if (attr == null) {
                          System.err.println("FluteFDT: Element.getAttributeNode: null");
                          System.exit(0);  
                        }
                        String toi = new String(attr.getValue());
                        long inttoi = Long.valueOf(toi).longValue();
                                        
                        if(ffile.FFileFindTOI(inttoi, filelist)==null)
                        {                                           
                                filelist=addTOItoFFile(filelist, inttoi);
                                int mcl_option = (int) inttoi;
                                if (flute.mcl.mcl_ctl_FLUTE_DELIVER_THIS_ADU (flute.id, mcl_option)<0){
                                  System.err.println("FluteFDT: mcl_ctl: MCL_OPT_FLUTE_DELIVER_THIS_ADU failed");
                                  System.exit(0);  
                                }                                 
                        }
                }
        }

        return filelist;

} 

 /* Adds elements of the FDTinstance to the FileList */
FFile addTOItoFFile(FFile filelist, long toi)
{       
        
        FFile          ffile=new FFile();
         
        if(!hasTOIinFDT(root,toi)){    
           return filelist;
        }
                                             
        String filename = new String(locationForTOIinFDT(root,toi));
        
        if(filename==null){
           System.err.println("FluteFDT: No content location for TOI "+toi);
           System.exit(0);  
        }

        FFile NewFile = new FFile();
        
        NewFile.fullname=new String(filename);       
//        NewFile.writeIt = CheckWriteContext(NewFile.fullname, flute.overwrite);
        NewFile.writeIt = true;
        NewFile.received=0;
        NewFile.toi = toi; 
               


        if(NewFile.writeIt)
         {
            // need to create directory tree first
            File test = new File(NewFile.fullname) ;      
            try {          
            test.mkdirs() ;
            } catch (Exception ex) {
              System.err.println("FluteFDT: Error while creating  \""+NewFile.fullname+"\"");
              System.exit(0);
            }
            test.delete();
            // ok, now we can be sure the directory exists, so we can actually open the file
            try { 
               NewFile.fd=new FileOutputStream(NewFile.fullname);
            } catch (Exception ex) {
              System.err.println("FluteFDT: Error while opening file  \""+NewFile.fullname+"\"");
              System.exit(0);
            }
          }
         NewFile.next = null;
               
         if(filelist==null)filelist = new FFile();
         filelist.FFileInsert( NewFile);                 

         return filelist;        
}
 
Document createNewFDTinstance()
{      
        Document fdtinstance;                
        Element rootel;
        
        fdtinstance= new DocumentImpl();   

        if (fdtinstance == null) {
            System.err.println("FluteFDT: DOMImpl.createElement: failed");
            System.exit(0);    
        }
        
        rootel = fdtinstance.createElement("FDT-Payload");    
        fdtinstance.appendChild( rootel ); 
  
        /* Add the Expires attribute to the element  */
        try {
          Long lng = new Long(getExpires());
          rootel.setAttribute ("Expires",lng.toString());
        } catch (Exception ex) {
            System.err.println("FluteFDT: Element.setAttribute: failed");
            System.exit(0);  
        }        
        return fdtinstance;
}

int getAndSendFinalFDTInstance(Document fdtinstance){
        int i=0;
        File temp;        
        FileInputStream file_to_send;
        byte [] buffer = null;
        String tempname = new String ("temp.xml");

        // in Java we can't build an XML file in memory, so we have
        // to write it to some temp file first
       
        // creating temp file name
        try {
          temp = File.createTempFile("flute",".xml");
          tempname = new String (temp.getName());
          temp.delete();
            } catch (Exception ex) {
            System.err.println("FluteFDT: Unable to create temp file name");
            System.exit(0);  
          }
     //   System.err.println("Temporary name is :"+tempname);
            
        // write document to that temp file
        try {
            DOMWriter writer = new DOMWriter(tempname);
            writer.print(fdtinstance);
        } catch ( Exception e ) {
            e.printStackTrace(System.err);
        }
        
        // read temp file into buffer
        File file = new File(tempname); 
 
      try {
          file_to_send=new FileInputStream(tempname);
        } catch (Exception ex) {
            System.err.println("fluteFDT: Unable to open file \""+tempname+"\"");
            return i;
        }
       i=(int) file.length();
       buffer = new byte[i];

       if(buffer==null){
              System.err.println("fluteSend: Cannot alloc memory!");
              System.exit(0);    
       }

       try {       
          file_to_send.read(buffer,0,i);
          file_to_send.close();
       } catch (Exception ex) {
           System.err.println("fluteFDT: read failed");
           System.exit(0);    
       }
       file.delete();
       
       if (flute.mcl.mcl_send(flute.id, buffer , i) < 0){
             System.err.println("fluteSend: mcl_send failed");
             System.exit(0);    
       }
         
       return i;
       
}
 
 
Element createNewFile(int toi, String filepath)
{
        Element el;
        // create element       
        el = fdt.createElement("File");    
        if (el == null) {
              System.err.println("fluteFDT: Document.createElement: null");
              System.exit(0);    
        }
        // add file and toi attributes
        try {
           Integer ival = new Integer(toi);
           el.setAttribute ("TOI",ival.toString());
           el.setAttribute ("Content-Location",filepath);
        } catch (Exception ex) {
            System.err.println("FluteFDT: Element.setAttribute: failed");
            System.exit(0);  
        }

        // finally, append this to the root element
           
        root.appendChild( el ); 

        return el;
}
 
Document AddFileToFDTinstance(Document fdtinstance , long itoi)
{
       
        String stoi = null;
        Element rootel = null;

        Long ival = new Long(itoi);
        stoi=new String(ival.toString());
        
        /* Get the reference to the childrens NodeList of the root element */
        NodeList childs = root.getChildNodes ();
        if (childs == null) {
            System.err.println("FluteFDT: Element.childNodes: null");
            System.exit(0);  
        }

        /* Go through the node of the instance and search TOI in the FDT */
        int nchilds = childs.getLength() ;
                                
                        
        /* Update File nodes */
        for (int i = 0; i < nchilds; i++) {
                Node nodeel = childs.item(i);
                if (nodeel == null) {
                    System.err.println("FluteFDT: NodeList.item("+i+": null");
                    System.exit(0);  
                }
                if(nodeel.getNodeType()==Node.ELEMENT_NODE)
                {
                        Attr attr = ((Element)nodeel).getAttributeNode("TOI");                  
                        if (attr == null) {
                          System.err.println("FluteFDT: Element.getAttributeNode: null");
                          System.exit(0);  
                        }
                        String toi = new String(attr.getValue());                

                        if(stoi.equals(toi))
                        {

                                // Get reference to the root element of the fdtinstance document 
                                rootel = fdtinstance.getDocumentElement ();

                               if (rootel == null) {
                                        System.err.println("FluteFDT: Document.documentElement: null");
                                        System.exit(0);  
                                }

                                // add this element to the root element of the fdtinstance document
                                Node importnode = fdtinstance.importNode(nodeel, true);                        
                        
                                // Append the imported Node to the childs list of the root element
                                try { 
                                  rootel.appendChild(importnode);
                                 } catch (Exception ex) {
                                         System.err.println("FluteFDT: Element.appendChild: failed");
                                          System.exit(0);  
                                 }
                        }
                }
        }
        return fdtinstance;
 }


 
} // end of class

