package Test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for default package");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(TestActivity.class));
		suite.addTest(new TestSuite(TestFileMetric.class));
		suite.addTest(new TestSuite(TestUnitTest.class));
		//$JUnit-END$
		return suite;
	}
}
