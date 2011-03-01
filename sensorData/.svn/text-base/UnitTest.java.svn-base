package sensorData;

public class UnitTest implements Comparable<UnitTest>{

	public String tStamp;
	public String tool;
	public String testCaseName;
	public String testName;
	public String elapsedTime;
	public String error;
	public String failure;

	public boolean counted = false;
	
	public long getTimeStamp() {
		
		Long l = new Long(tStamp);
		return l.longValue();
	}
	
	public long getTestTime(){

		Long l = new Long(elapsedTime);
		return l.longValue();
	}


	public int compareTo(UnitTest a) {
		
		if(this.getTimeStamp() > a.getTimeStamp())
			return 1;
		else 
			return -1;
	}
	
	public String toString(){
		
		return "@" + tStamp + ": " + testCaseName + "/" + testName + "run for "  + elapsedTime + " milisec. and may have returned the following message: " + error + failure;
	
		
	}
}
