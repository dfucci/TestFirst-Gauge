package sensorData;

import java.util.Vector;
import org.w3c.dom.*;
import common.IO;

public class FileMetricReader extends IO{

	
	 public Vector<FileMetric> fileMetricRecord = new Vector<FileMetric>();
	
	 public FileMetricReader(){ 
		 super(0, (Vector<String>) null);
	 }
	
	 @Override
	protected void processXMLEntry(Node node) {
		FileMetric fm = new FileMetric();
			
		fm.tStamp 		= node.getAttributes().getNamedItem("tstamp").getNodeValue();
		fm.tool 		= node.getAttributes().getNamedItem("tool").getNodeValue();
		fm.fileName 	= node.getAttributes().getNamedItem("fileName").getNodeValue();
		fm.className	= node.getAttributes().getNamedItem("className").getNodeValue();
		fm.data		= node.getAttributes().getNamedItem("data").getNodeValue();
		fm.lmod		= node.getAttributes().getNamedItem("lmod").getNodeValue();
		fm.loc			= node.getAttributes().getNamedItem("loc").getNodeValue();
			
		fileMetricRecord.add(fm);
	 }
}
