package sensorData;

import java.util.HashSet;
import java.util.Vector;
import org.w3c.dom.*;
import common.IO;

public class UnitTestReader extends IO{
		
	public Vector<UnitTest> unitTestRecord = new Vector<UnitTest>();
	public HashSet<String> testCaseSet = new HashSet<String>();
	public HashSet<String> testNames = new HashSet<String>();

	public UnitTestReader(){ 
		super(0, (Vector<String>)null);
		
		//this.testCaseSet.add("AcceptanceTest");
		//this.testCaseSet.add("UnitTest");
		//this.testCaseSet.add("RecommenderTests");
	}
		
	@Override
	protected void processXMLEntry(Node node) {
					
		UnitTest ut = new UnitTest();
		
		ut.tStamp 	= node.getAttributes().getNamedItem("tstamp").getNodeValue();
		ut.tool 	= node.getAttributes().getNamedItem("tool").getNodeValue();
		ut.testCaseName = node.getAttributes().getNamedItem("testCaseName").getNodeValue();
		ut.testName	 = node.getAttributes().getNamedItem("testName").getNodeValue();
		ut.elapsedTime	 = node.getAttributes().getNamedItem("elapsedTime").getNodeValue();
		ut.error		 = node.getAttributes().getNamedItem("errorString").getNodeValue();
		ut.failure		 = node.getAttributes().getNamedItem("failureString").getNodeValue();
			
		this.unitTestRecord.add(ut); 
		this.testCaseSet.add(ut.testCaseName);
		this.testNames.add(ut.testName);
	}
}
