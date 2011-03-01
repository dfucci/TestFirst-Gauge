package Test;

import junit.framework.TestCase;

public class TestActivity extends TestCase {

	public TestActivity(String arg0) {
		super(arg0);
	}
/*
	public void testProcessFile() throws Exception {
		ActivityDataReader adr = new ActivityDataReader();
		adr.processFileDirectory("C:\\TEMP\\Michelle\\Activity"); 
		Activity act;
		
		assertEquals(2, adr.activityRecord.size());
		
		act = (Activity)adr.activityRecord.get(0);
		assertEquals("1069422966449", act.tStamp);
		assertEquals("Eclipse" , act.tool);
		assertEquals("Open File", act.type);
		assertEquals("C:/Program Files/eclipse/workspace/Money/src/Money.java", act.data);
		
		act = (Activity)adr.activityRecord.get(1);
		assertEquals("1069423176178", act.tStamp);
		assertEquals("Eclipse" , act.tool);
		assertEquals("Close File", act.type);
		assertEquals("C:/Program Files/eclipse/workspace/Money/src/TestMoney.java", act.data );
		
	}
	
	public void testGetFileName() throws Exception{
		ActivityDataReader adr = new ActivityDataReader();
		adr.processFileDirectory("C:\\TEMP\\Michelle\\Activity"); 
		Activity act1;
		Activity act2;
		
		act1 = (Activity)adr.activityRecord.get(0);
		act2 = (Activity)adr.activityRecord.get(1);
		
		assertEquals(false, act1.getData().startsWith("Test"));
		assertEquals(true, act2.getData().startsWith("Test"));
	}
*/
}
