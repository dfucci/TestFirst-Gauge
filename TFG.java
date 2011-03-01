import java.io.File;
import java.util.Vector;
import common.*;
import gauge.*;
import sensorData.*;

/**
 * Class TFG is responsible for:
 * 1. Loading the TFGConfig.xml configuration file;
 * 2. Executing the interfaces for converting VS logs(event logger and test results) to internal format 
 * 3. Executing sensorData.dataController and gauge.gaugeController
 */
public class TFG{
	
	private final static String confFile = "TFGConfig.xml";
	private final static String vsLogEventConf = "TrigerringEvents.xml";
	
	private Configuration configuration;
	private ConfigurationReader confReader;
	private GaugeController gaugeControl;
	private DataController dataControl;
	private MSTestReader msTestReader;
	private VSLogReader vsLogReader;
	private String activityFilePath, fileMetricFilePath, TestFilePath;
	
	public TFG(){	
		Vector<String> entry = new Vector<String>();
		entry.add("Property");
		this.confReader = new ConfigurationReader();
		this.configuration = this.confReader.readConf(confFile, entry);
		this.fileMetricFilePath = this.configuration.getValue("TFGDirectory") + DataController.FILE_METRIC_DIR + DataController.FILE_METRIC_FILE;
		this.activityFilePath = this.configuration.getValue("TFGDirectory") + DataController.ACTIVITY_DIR + DataController.ACTIVITY_FILE;
		this.TestFilePath = this.configuration.getValue("TFGDirectory") + DataController.UNIT_TEST_DIR + DataController.UNIT_TEST_FILE;
	}
	
	public static void main (String argv[]) throws Exception {
		
		Vector<String> rootElement = new Vector<String>();
		Vector<String> entry = new Vector<String>();
		TFG tfg = new TFG();
		
		if(!tfg.checkDirsAndFiles()){
			System.out.println("\n(!)Cannot continue without above file(s)...");
			return;
		}		
		if(!tfg.exists(tfg.activityFilePath, false) || !tfg.exists(tfg.fileMetricFilePath, false))
			tfg.generateActivitiesAndFileMetrics(rootElement, entry, tfg);
		if(!tfg.exists(tfg.TestFilePath, false))
			tfg.generateTestLogs(rootElement, entry, tfg);
		
		// Data controller loads logs to memory
		System.out.print("\n(+)Reading data...");
		tfg.dataControl =  new DataController(tfg.configuration.getValue("ProjectName"), tfg.configuration.getValue("TFGDirectory"), tfg.configuration.getValue("TestDataIn"));
		tfg.dataControl.getTestLog();
		tfg.dataControl.getActivityLog();
		tfg.dataControl.getFileMetric();
		System.out.println("done!");

		// Gauge controller computes all the cycles and exports the result to report file
		System.out.print("\n(+)Processing data...");
		tfg.gaugeControl = new GaugeController(tfg.configuration.getValue("RemoveNoisyLogs"), tfg.dataControl.testNameSet);
		tfg.gaugeControl.buildCycleElement(tfg.dataControl, Long.parseLong(tfg.configuration.getValue("IdleTimeThreshold")));
		tfg.gaugeControl.computeCycle();
		System.out.println("done!");
		System.out.print("\n(+)Generating report...");
		tfg.gaugeControl.updateExcel(tfg.configuration.getValue("ReportTemplateFile"), tfg.configuration.getValue("TFGDirectory") + tfg.configuration.getValue("ReportFileName"));
		System.out.println("done!");
		System.out.println("\n(!)Check report @ " + tfg.configuration.getValue("TFGDirectory") + tfg.configuration.getValue("ReportFileName"));
	}

	private void generateTestLogs(Vector<String> rootElement, Vector<String> entry, TFG tfg) {
		System.out.print("\n\t(+)Generating test logs...");
		rootElement.clear();rootElement.add("sensor");
		tfg.msTestReader = new MSTestReader(1, rootElement);
		entry.clear(); entry.add("Results"); entry.add("UnitTestResult");
		tfg.msTestReader.processXMLDirectory(tfg.configuration.getValue("TestDataIn"), entry);
		tfg.msTestReader.writeXMLFile(tfg.msTestReader.document.elementAt(0), tfg.TestFilePath);
		System.out.println("done!");
	}

	private void generateActivitiesAndFileMetrics(Vector<String> rootElement, Vector<String> entry, TFG tfg) {
		System.out.print("\n\t(+)Generating activities and file metrics...");
		rootElement.add("sensor"); rootElement.add("sensor");
		tfg.vsLogReader = new VSLogReader(2, rootElement, vsLogEventConf);
		entry.clear(); entry.add("Item");
		tfg.vsLogReader.processXMLFile(tfg.configuration.getValue("VSLogDataIn"), entry);
		tfg.vsLogReader.writeXMLFile(tfg.vsLogReader.document.elementAt(0), tfg.fileMetricFilePath);
		tfg.vsLogReader.writeXMLFile(tfg.vsLogReader.document.elementAt(1), tfg.activityFilePath);
		System.out.println("done!");
	}
	
	private boolean checkDirsAndFiles() {
		boolean success = false;
		
		if(exists(this.configuration.getValue("VSLogDataIn"), false))
			if(exists(this.configuration.getValue("TestDataIn"), false))
				if(exists(this.configuration.getValue("TFGDirectory"), true))
					if(exists(this.configuration.getValue("TFGDirectory") + DataController.ACTIVITY_DIR , true))
						if(exists(this.configuration.getValue("TFGDirectory") + DataController.UNIT_TEST_DIR, true))
							if(exists(this.configuration.getValue("TFGDirectory") + DataController.FILE_METRIC_DIR, true))
								success = true;
		return success;
		
	}

	private boolean exists(String str, boolean force) {
		boolean success = false;
		File control = new File(str);
		
		if(control.exists())
			success = true;
		else if(force){
			if(new File(str).mkdir())
				success = true;
			if (!success) {
				System.out.println("\n(!)Can not create directory: " + str);
			}else{
				System.out.println("\n(+)Successfully created directory: " + str);
			}
		}else{
			System.out.println("\n(-)" + str + " does not exist...");
		}
		return success;
	}
}	
	
