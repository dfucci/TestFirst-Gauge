package gauge;

import java.util.HashSet;
import java.util.Vector;

import sensorData.Activity;
import sensorData.UnitTest;


public class CycleElement {
	Vector<Activity> testAct = new Vector<Activity>();
	Vector<Activity> prodAct = new Vector<Activity>();
	Vector<UnitTest> testBlock = new Vector<UnitTest>();
	Vector<ActiveFile> activeFile = new Vector<ActiveFile>();
	Vector<Activity> elementAct = new Vector<Activity>();	
	HashSet<String> elementTestSet = new HashSet<String>();
	
	int attemptNumber;
	int passNumber;
	int testCounter;
	public boolean isProdEditing = false;
	public boolean isTestEditing = false;
	
	public long getTestTime(){
		long testTime = 0;
		int size = testBlock.size();
		
		for (int i = 0; i < size; i++)
			testTime = testTime + ((UnitTest)testBlock.get(i)).getTestTime();
		return testTime;
	}
	
		
}
