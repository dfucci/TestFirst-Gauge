package gauge;

import java.util.Vector;
import java.util.HashSet;

import sensorData.*;

public class Cycle {

	public boolean testCodeTimeFilled = false;
	public boolean prodCodeTimeFilled = false;
	
	private long cycleTime;
	private long cycleIdleTime;
	
	private long testCodeTime;
	private long prodCodeTime;
	
	private double testCodeSize;
	private double prodCodeSize;
	
	private double tpRatio = 0.001;
	
	boolean isRefactoring = false;  // Indicating if it is a refactoring cycle
	public String isTddCompliant = "U";
	public String type = "U";

	Vector<ActiveFile> cycleFiles = new Vector<ActiveFile>();  // Historical files the current and the previous cycles access
  	Vector<CycleElement> elementList = new Vector<CycleElement>(); // The cycle elements in this cycle
  	Vector<UnitTest> lastTestMethods = new Vector<UnitTest>(); // The test methods in the last (successful) execution of the test
	
	public long getTime(){
		return cycleTime;//!=0 ? cycleTime: (long)1;
	}
	
	public long getIdleTime(){
		return cycleIdleTime;
	}
	
	public long getTestCodeTime(){
		return testCodeTime;//>1000 ? testCodeTime: (long)0;
	}
	
	public long getProdCodeTime(){
		return prodCodeTime;//>1000 ? prodCodeTime: (long)0;
	}
	
	public double getTestCodeSize(){
		return testCodeSize;
	}
	
	public double getProdCodeSize(){
		return prodCodeSize;
	}
	
	public double getTPRatio(){
		return tpRatio;
	}
	
	/**
	 * Updates the cycle time, including:
	 * 1. cycle active time and idle time;
	 * 2. cycle test code time and production code time.
	 */
	private void setCycleTime(){
		int i;
		int j;
		
		long fileTime = 0;
		long testFileTime = 0;
	
		long testTime = 0;
		long idleTime = 0;
		
		Vector<ActiveFile> files;
		Vector<UnitTest> tests;
				
		for ( i = 0; i < elementList.size(); i++){
			files = ((CycleElement)elementList.get(i)).activeFile;
			tests = ((CycleElement)elementList.get(i)).testBlock;
			
			for ( j = 0; j < files.size(); j++){
				fileTime = fileTime + ((ActiveFile)files.get(j)).activeTime;
				idleTime = idleTime + ((ActiveFile)files.get(j)).idleTime;
				// Calculate the time spent on writing test code
				if (((ActiveFile)files.get(j)).isTestCode)
					testFileTime = testFileTime + ((ActiveFile)files.get(j)).activeTime;
			}
			
			for ( j = 0; j < tests.size(); j++)
				testTime = testTime + ((UnitTest)tests.get(j)).getTestTime();
				
			cycleTime = fileTime + testTime;
			cycleIdleTime = idleTime;
			
			testCodeTime = testFileTime;
			prodCodeTime = fileTime - testCodeTime;
		}
		
	}

	/**
	 * Updates the cycle T/P ratio
	 */
	private void setCycleTPRatio() {
		testCodeSize = 0.001;
		prodCodeSize = 0;
		for (int i = 0; i < cycleFiles.size(); i++){
			ActiveFile actFile = (ActiveFile)cycleFiles.get(i);
			if (actFile.isTestCode)
				testCodeSize = testCodeSize + actFile.size;
			else 
				prodCodeSize = prodCodeSize + actFile.size;
		}
		
		if (prodCodeSize != 0){
			tpRatio = testCodeSize/prodCodeSize;
		}	
	}
	
	/**
	 *  Updates cycle filelist when a new cycle element is added into the cycle
	 */
	public void updateCycle(CycleElement element){
		for ( int i = 0; i < element.activeFile.size(); i++){
			boolean updated = false;
			ActiveFile aFile = (ActiveFile)element.activeFile.get(i);
			// Check if the element file has been recorded in the cycle file list
			for (int j = 0; j < cycleFiles.size(); j++){
				ActiveFile actFile = (ActiveFile)cycleFiles.get(j);
				if (actFile.fileName.equals(aFile.fileName)){
					cycleFiles.setElementAt(aFile, j);
					updated = true;
					break;
				}
			}
			if (updated == false) 
				cycleFiles.add(aFile);
		}
		setCycleTPRatio();
		setCycleTime();
	}

	
	/**
	 * 	Check if a cycle is refactoring cycle. If a cycle contains:
	 *  1. no production code activity or
	 * 	2. no test code activity or
	 * 	3. no change in the test methods (in terms of test method name and size)
	 * 	then this cycle is considered to be a potential refactoring cycle
	 */
	public void checkRefac() {
		CycleElement currentElement;
		Activity act;
		Vector<UnitTest> testMethods;		
		/*boolean noTestCode = true;
		boolean noProdCode = true;
		boolean refactoring = false;
		
		for (int i = 0; i < elementList.size(); i++){
			currentElement = (CycleElement)elementList.get(i);
			
			for (int j = 0; j < currentElement.prodAct.size(); j++){
				act = currentElement.prodAct.get(j);
				if(act.type.contains("Refactor"))
					refactoring = true;
			}
			if ( currentElement.testAct.size() > 0)  // has test code
				noTestCode = false;
			if ( currentElement.prodAct.size() > 0)  // has production code		
				noProdCode = false;		
			if ((noTestCode == false) && (noProdCode == false))
				break;
		}
		// testMethods in the last cycleElement
		testMethods =((CycleElement)elementList.lastElement()).testBlock;
		if ((noTestCode == false) && (noProdCode == false) && differentTestMethods(testMethods) && !refactoring)
			isRefactoring = false;*/
		
		for (int i = 0; i < elementList.size(); i++){
			currentElement = (CycleElement)elementList.get(i);
			
			for (int j = 0; j < currentElement.prodAct.size(); j++){
				act = currentElement.prodAct.get(j);
				if(act.type.contains("Refactor"))
					this.isRefactoring = true;
			}
			for (int j = 0; j < currentElement.testAct.size(); j++){
				act = currentElement.testAct.get(j);
				if(act.type.contains("Refactor"))
					this.isRefactoring = true;
			}
			
		}
		
	}

	private boolean differentTestMethods(Vector<UnitTest> testMethods) {
		HashSet<String> lastTestSet = new HashSet<String>();
		HashSet<String> testSet = new HashSet<String>();	
		
		if (!lastTestMethods.isEmpty())
			for (int i = 0; i < lastTestMethods.size(); i++)
				lastTestSet.add(((UnitTest)lastTestMethods.get(i)).testCaseName + ((UnitTest)lastTestMethods.get(i)).testName);
	
		for (int i = 0; i < testMethods.size(); i++)
			testSet.add(((UnitTest)testMethods.get(i)).testCaseName + ((UnitTest)testMethods.get(i)).testName);
	
		if ((lastTestSet.size() == testSet.size()) && (testSet.containsAll(lastTestSet))) 		
			return false;
		else
			return true;
	}

	public void SetCycleTypes() {
		CycleElement currentElement;
		Vector<UnitTest> testMethods;
		long firstTestAct = 0, firstProdAct= 0;
		boolean noTestCode = true;
		boolean noProdCode = true;
		boolean refactoring = false;
		
		
		
		for (int i = 0; i < elementList.size(); i++){
			currentElement = (CycleElement)elementList.get(i);
			
			if ( currentElement.testAct.size() > 0 && currentElement.isTestEditing){  // has test code
				noTestCode = false;
				if(firstTestAct == (long) 0 )
					firstTestAct = currentElement.testAct.firstElement().getTimeStamp();
			}
			if ( currentElement.prodAct.size() > 0 && currentElement.isProdEditing){  // has production code		
				noProdCode = false;		
				if(firstProdAct == (long) 0 )
					firstProdAct = currentElement.prodAct.firstElement().getTimeStamp();			
			}	
		}
		
		if (!noTestCode && !noProdCode){	
			if (firstTestAct < firstProdAct){
				this.type = "TF";
				this.isTddCompliant = "Y";
			}else{
				this.type = "TL";
				this.isTddCompliant = "N";
			}
			return;
		}
		
		if(noProdCode && !noTestCode){
			this.type = "TA";
			return;
		}
		
		if(!noProdCode && noTestCode){
			this.type = "PA";
			return;
		}
		if(this.isRefactoring)
			this.type = "R";
		
		
	}
	
}
