package Test;

import junit.framework.TestCase;

public class TestUnitTest extends TestCase {

	public TestUnitTest(String arg0) {
		super(arg0);
	}

	/*public void testProcessFile() throws Exception {
		UnitTestReader utr = new UnitTestReader();
		utr.processFileDirectory("C:\\TEMP\\Michelle\\UnitTest"); 
		UnitTest ut;
		
		assertEquals(29, utr.unitTestRecord.size());
		
		ut = (UnitTest)utr.unitTestRecord.get(0);
		assertEquals("1069422986000", ut.tStamp);
		assertEquals("Sensor Shell" , ut.tool);
		assertEquals("TestMoney", ut.testCaseName);
		assertEquals("testMultiplyDollar", ut.testName);
		assertEquals("62", ut.elapsedTime);
		assertEquals(" ", ut.error);
		assertEquals(" ", ut.failure);
		
		ut = (UnitTest)utr.unitTestRecord.get(1);
		assertEquals("1069422986001", ut.tStamp);
		assertEquals("Sensor Shell" , ut.tool);
		assertEquals("TestMoney", ut.testCaseName);
		assertEquals("testAddEuroToDollar", ut.testName);
		assertEquals("0", ut.elapsedTime);
		assertEquals(" ", ut.error);
		assertEquals(" ", ut.failure);
	}*/

}
