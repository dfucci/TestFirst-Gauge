package sensorData;

import java.util.HashSet;
import java.util.Vector;
import org.w3c.dom.*;
import common.IO;

public class ActivityDataReader extends IO{

	public Vector<Activity> activityRecord = new Vector<Activity>(); //all Activity data
	public HashSet<String> projectSet =  new HashSet<String>(); //projectSet contains the project name appeared in UnitTest data
	
	public ActivityDataReader(){
		super(0, (Vector<String>)null);
	}
	
	@Override
	protected void processXMLEntry(Node node) {		
		Activity act = new Activity();
		
		act.tStamp 	= node.getAttributes().getNamedItem("tstamp").getNodeValue();
		act.type 	= node.getAttributes().getNamedItem("type").getNodeValue();
		act.data 	= node.getAttributes().getNamedItem("data").getNodeValue();
		act.fName   = node.getAttributes().getNamedItem("fName").getNodeValue();
		act.fileName = act.data.substring(act.data.lastIndexOf('\\')+1);
			
		activityRecord.add(act);

	}
}