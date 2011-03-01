package common;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public abstract class IO {
	
	public DocumentBuilder builder;
	public DocumentBuilderFactory  factory;
	public Vector<Document> document;
	public Vector<Element> rootElements;
	
	protected abstract void processXMLEntry(Node node);
	
	public IO(int numOutput, Vector<String> rootElementNames){
		
		this.factory = DocumentBuilderFactory.newInstance();
		this.builder = null;
		try {
			this.builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		this.document = new Vector<Document>();
		this.rootElements = new Vector<Element>();
		for(int i=0; i < numOutput; i++){
			this.document.add(builder.newDocument());
			this.rootElements.add(document.get(i).createElement(rootElementNames.get(i)));
			this.document.get(i).appendChild(this.rootElements.get(i));
		}
	}
	
	public void processXMLDirectory(String directory, Vector<String> entryText){
		
		File fileDirectory = new File(directory);
		File[] filesInDir = fileDirectory.listFiles(); 

		if (filesInDir != null) { 
			int length = filesInDir.length; 

			for (int i = 0; i < length; ++i) { 
				File f = filesInDir[i]; 
				if (f.isFile()){
					String path = f.getAbsolutePath();
					processXMLFile(path, entryText);
				}  
				else if (f.isDirectory())  
					processXMLDirectory(f.getName(), entryText); 
			} 
		}
	}


 /**
  * Read one XML file
  */
	 public void processXMLFile(String fileName, Vector<String> entryText){
		
		 Element element = null;
		 Document myDocument = null;
		 try {
			  myDocument = this.builder.parse( new File(fileName));		 
		 } catch (SAXException sxe){
			 Exception x = sxe;
			 if (sxe.getException() != null)
				 x = sxe.getException();
			 x.printStackTrace(); 
		 } catch (IOException ioe){
			 ioe.printStackTrace(); 
		 }
		 element = (Element)myDocument.getChildNodes().item(0);
		 int size = element.getChildNodes().getLength();
		 for (int i=0; i<size; i++){
			 Node node = element.getChildNodes().item(i);
			 if ((node.getNodeType() == Node.ELEMENT_NODE) && (node.getNodeName().equals(entryText.firstElement())))
				 processNestedEntry(entryText, node, 0);
		 }
	 }
	 
	private void processNestedEntry(Vector<String> entryText, Node node, int depth) {
		
		if(depth==entryText.size()-1){
			if ((node.getNodeType() == Node.ELEMENT_NODE) && (node.getNodeName().equals(entryText.lastElement())));
					processXMLEntry(node);	
		}else {
			int size = node.getChildNodes().getLength();
			for (int i=0; i<size; i++){
				Node childNode = node.getChildNodes().item(i);
				if ((childNode.getNodeType() == Node.ELEMENT_NODE) && (childNode.getNodeName().equals(entryText.elementAt(depth+1))))
					processNestedEntry(entryText, childNode, depth+1);
			}
		}
	}

	 public void writeXMLFile(Document document, String fullFileName){
		 
		XMLSerializer serializer = new XMLSerializer();
		
	    try {
			serializer.setOutputCharStream(new java.io.FileWriter(fullFileName));
			serializer.serialize(document);
		} catch (IOException e) {
			e.printStackTrace();
		}
	 
	 }
	 
	//TODO If something funny happens about timeStamps, it is here! 
	 public String convertTime(String timeStr, char mode, String timeZone) throws ParseException{
			
		Date d = null; 
		SimpleDateFormat df = new SimpleDateFormat("yyyy.mm.dd HH:mm:ss.SSS");
		String time = timeStr;
		Long timeDiff = new Long(Long.valueOf(timeZone)*Long.valueOf("3600000"));
			
		if (mode == 'f'){
			time = timeStr.substring(0, 23);
			time = time.replace('T', ' ');
			time = time.replace('-', '.');
			
		}else{
			time = timeStr.substring(0, 12);
			time = "1970.01.01 " + time;			
		}
		d = df.parse(time);
		return Long.toString(d.getTime()-timeDiff);
	}

}