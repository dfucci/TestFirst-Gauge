package sensorData;

import java.util.*;
import org.w3c.dom.Node;
import common.IO;


/**
 * DataController class is responsible for
 * 1. Loading Hackystat data; (NOT ANYMORE)
 * 2. Extracting the Hackystat data; (NOT ANYMORE)
 * 3. Saving the data into entity objects. 
 */

public class DataController extends IO{
	public final static String UNIT_TEST_DIR = "UnitTest\\";
	public final static String ACTIVITY_DIR = "Activity\\";
	public final static String FILE_METRIC_DIR = "FileMetric\\";
	public final static String UNIT_TEST_FILE = "UnitTest.xml";
	public final static String ACTIVITY_FILE = "Activity.xml";
	public final static String FILE_METRIC_FILE = "FileMetric.xml";
	
	private String projectName;
	private String testDataPath;
	private String activityDataPath;
	private String metricDataPath;
	private String actualTestLogs;
	private String prevFile =  null;
	private int unitTestAlignCounter;
	private int testActivityCounter = -1;

	public Vector<Activity> allActivities = new Vector<Activity>();
	public Vector<Activity> testActivities = new Vector<Activity>();
	public Vector<FileMetric> allFiles = new Vector<FileMetric>();
	public HashSet<String> testcaseSet =  new HashSet<String>();
	public HashSet<String> testNameSet =  new HashSet<String>();
	public UnitTestReader utr;
	public ActivityDataReader adr;
	public FileMetricReader fmr;
	
	public DataController(String projectName, String tfgDir, String actualTestLogs){
		
		super(0, null);
		this.projectName = projectName;
		this.testDataPath = tfgDir + UNIT_TEST_DIR;
		this.activityDataPath = tfgDir + ACTIVITY_DIR;
		this.metricDataPath = tfgDir + FILE_METRIC_DIR;
		this.actualTestLogs = actualTestLogs;
		this.unitTestAlignCounter = 0;
	}
	
	
	public void getActivityLog(){
		Vector<String> entry = new Vector<String>();
		entry.add("entry");
		adr = new ActivityDataReader();
		adr.projectSet.add(this.projectName);
		adr.processXMLDirectory(activityDataPath, entry);
		allActivities = adr.activityRecord;
		Collections.sort(allActivities);	
		Collections.sort(utr.unitTestRecord);
		alignLogFiles();
		Collections.sort(allActivities);
		Collections.sort(utr.unitTestRecord);
	}
	
	public void getTestLog(){
		Vector<String> entry = new Vector<String>();
		entry.add("entry");
		utr = new UnitTestReader();
		utr.processXMLDirectory(testDataPath, entry);
		testcaseSet =  utr.testCaseSet;
		testNameSet = utr.testNames;
		Collections.sort(utr.unitTestRecord);
		
	}

	public void getFileMetric(){
		Vector<String> entry = new Vector<String>();
		entry.add("entry");
		fmr = new FileMetricReader();
		fmr.processXMLDirectory(metricDataPath, entry);
		allFiles = fmr.fileMetricRecord;
		Collections.sort(allFiles);	
	}
	
	public void alignLogFiles(){
		int size = allActivities.size();		
		Vector<String> entry = new Vector<String>();
		entry.add("TestDefinitions"); entry.add("UnitTest");
		
		for(int i = 0; i < size; i++)
			if(allActivities.elementAt(i).type.equals("Test Run")){
				testActivities.add(allActivities.elementAt(i));
			}
		if(testActivities.size()== 0)
			System.out.print("\n\t(-)No logs regarding any test run, trying to interpolate...");
		try {
			this.processXMLDirectory(actualTestLogs, entry);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(testActivities.size()== 0)
			System.out.print("\n\t(+)Interpolated " + unitTestAlignCounter + " test runs based on test logs\n");
		
	}

	@Override
	protected void processXMLEntry(Node node) {

		UnitTest unitTest = null;
		
		if(!node.getOwnerDocument().getDocumentURI().equals(prevFile)){
			prevFile = node.getOwnerDocument().getDocumentURI();
			testActivityCounter++;
		}
		
		unitTest = utr.unitTestRecord.elementAt(unitTestAlignCounter);
		if(testActivities.size()!= 0){
			unitTest.tStamp = testActivities.elementAt(testActivityCounter).tStamp;
			utr.unitTestRecord.setElementAt(unitTest, unitTestAlignCounter);
		}else{
			Activity act = new Activity();
			
			act.tStamp 	= unitTest.tStamp;
			act.type 	= "Test Run";
			act.data 	= "SmokeTest";
			act.fName   = "SmokeTest";
			act.fileName = "SmokeTest";
				
			allActivities.add(act);
		}
		unitTestAlignCounter++;
		

	}

}

	
	