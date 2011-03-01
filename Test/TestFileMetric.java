package Test;

import junit.framework.TestCase;

public class TestFileMetric extends TestCase {

	public TestFileMetric(String arg0) {
		super(arg0);
	}

	/*public void testProcessFile() throws Exception {
		FileMetricReader fmr = new FileMetricReader();
		fmr.processFileDirectory("C:\\TEMP\\Michelle\\FileMetric"); 
		FileMetric fm;
		
		assertEquals(2, fmr.fileMetricRecord.size());
		
		fm = (FileMetric)fmr.fileMetricRecord.get(0);
		assertEquals("1069422996000", fm.tStamp);
		assertEquals("Sensor Shell" , fm.tool);
		assertEquals("C:/Program Files/eclipse/workspace/Money/src/Money.java", fm.fileName);
		assertEquals("Money", fm.className);
		assertEquals("cbo=2,dit=1,loc=19,noc=0,rfc=3,size=913,wmc=4", fm.data);
		assertEquals("1069362207269", fm.lmod);
		
		fm = (FileMetric)fmr.fileMetricRecord.get(1);
		assertEquals("1069423226000", fm.tStamp);
		assertEquals("Sensor Shell" , fm.tool);
		assertEquals("C:/Program Files/eclipse/workspace/Money/src/Money.java", fm.fileName);
		assertEquals("Money", fm.className);
		assertEquals("cbo=2,dit=1,loc=19,noc=0,rfc=3,size=913,wmc=4", fm.data);
		assertEquals("1069362207269", fm.lmod);
		
	}*/

}
