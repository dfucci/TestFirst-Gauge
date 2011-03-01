package sensorData;

public class Activity implements Comparable<Activity>{
	
	public String tStamp;
	public String type;
	public String data;
	public String fName;
	
	public String fileName = "";


	public String getData(){
		return data;
	}
		
	public long getTimeStamp(){
		Long l = new Long(tStamp);
		return l.longValue();
	}
	
	public String getFileName(){
		return fileName;
	}

	public int compareTo(Activity a) {
		if(this.getTimeStamp() > a.getTimeStamp())
			return 1;
		else
			return -1;
	}

		
}

