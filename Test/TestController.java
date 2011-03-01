package Test;

import junit.framework.TestCase;


public class TestController extends TestCase {

	public TestController(String arg0) {
		super(arg0);
	}
/*
	public void testActivityExist() throws Exception {
		Controller control = new Controller();
		control.getActivityLog();
		control.getTestLog();
		
		
		String s1 = new String ("1069433487008");
		String s2 = new String ("1069433968009");
		long time1 = (new Long(s1)).longValue();
		long time2 = (new Long(s2)).longValue();
		
		boolean result = control.activityExist(time1, time2);
		assertEquals(false, result);
		
		control.buildCycleElement();
		assertEquals(4, control.allElements.size());
	}
*/	
/*
	public void testSetFileSize() throws Exception{
		Controller control = new Controller();
		control.getFileMetric();
		int i;
		FileMetric fm;
		for (i = 0; i < control.allFiles.size(); i++)
			fm = (FileMetric)control.allFiles.get(i);
		
		
	}
*/

		/*
	public void testBuildCycleElement() throws Exception{
		Controller control = new Controller();
		control.getActivityLog();
		control.getTestLog();
		control.getFileMetric();
		
		control.buildCycleElement();

		CycleElement element;
		ActiveFile file;
		
		for (int i = 0; i < control.allElements.size(); i++){
			element = (CycleElement)control.allElements.get(i);
			System.out.println("element " + i);
			
			for (int j = 0; j < element.activeFile.size(); j++){
				file = (ActiveFile)element.activeFile.get(j);
				System.out.println("file " + j + " " + file.fileName + " " + file.size );
			}
			
		}
		
	}
	*/
	
	/*
	public void testComputeCycle() throws Exception{
		Controller control = new Controller();
		
		control.getActivityLog();
		control.getTestLog();
		control.getFileMetric();
		
		Cycle cycle = new Cycle();
		
		control.buildCycleElement();
		control.computeCycle(0, cycle);
		
		Vector files;
		
		//System.out.println("cycle size = " + control.allCycles.size());
		for (int i = 0; i < control.allCycles.size(); i++){
			System.out.println();
			System.out.println("--------------------------------------------------");
			System.out.println("compute cycle " + i);
			System.out.println("--------------------------------------------------");
			cycle = (Cycle)control.allCycles.get(i);
			System.out.println("cycle files number: " + cycle.cycleFiles.size());
		   
			System.out.println("cycle elapsed time: " + cycle.getTime());
			System.out.println("cycle TP Ratio: " + cycle.getTPRatio());
			for (int j = 0; j < cycle.elementList.size(); j++){
				System.out.println();
				System.out.println("element index: " + j);
				
				files = ((CycleElement)cycle.elementList.get(j)).activeFile;
				for (int k = 0; k < files.size(); k++){
					System.out.println("file number " + k + ": file name = " + ((ActiveFile)files.get(k)).fileName + " loc = " +  ((ActiveFile)files.get(k)).size );
					System.out.println("file time = " + ((ActiveFile)files.get(k)).elapsedTime );
				}
					
			}
			
		}
	}
	
	*/
/*	
	public void testWriteToExcel() throws Exception {
		Controller control = new Controller();

		control.getActivityLog();
		control.getTestLog();
		control.getFileMetric();
		
		Cycle cycle = new Cycle();
		
		control.buildCycleElement();
		control.computeCycle(0, cycle);
		
		//control.writeToExcel();
		control.updateExcel("C:\\TEMP\\TEST\\Template.xls", "C:\\TEMP\\TEST\\Report.xls");
	}
*/	
/*	
	public void testLoadHackyStatData() throws Exception {
		Controller control = new Controller();
		control.loadHackyStatData( "fVCxKHecYgA8");
	}
*/
	
/*	
	public void testGetFilePath() throws Exception {
		Controller c = new Controller();
		c.getFilePath("E:\\NRC\\TEST\\");
	}
*/

	
/*
	public void testIntegration() throws Exception{
		Controller control = new Controller();
		control.loadHackyStatData( "fVCxKHecYgA8");
		
		control.getFilePath("E:\\NRC\\TEST\\");
		control.getActivityLog();
		control.getTestLog();
		control.getFileMetric();
		
		Cycle cycle = new Cycle();
	
		control.buildCycleElement();
		control.computeCycle(0, cycle);
		
		//control.writeToExcel();
		control.updateExcel("E:\\NRC\\TEST\\Template.xls", "E:\\NRC\\TEST\\Report.xls");
		
	}
*/
}