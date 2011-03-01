package sensorData;

public class FileMetric implements Comparable<FileMetric>{

	public String tStamp;
	public String tool;
	public String fileName;
	public String className;
	public String data;
	public String lmod;
	public String loc;
	
	
	public int getFileSize(){
		Integer l = new Integer(loc);
		return l.intValue();
	}
	
	public long getLModTime(){
		Long l = new Long(lmod);
		return l.longValue();
	}
	
	public String getFileName(){
		return fileName;
	}
	
	public String getClassName(){
		return className;
	}
	
	public int compareTo(FileMetric a) {
		if(this.getLModTime() > a.getLModTime())
			return 1;
		else
			return -1;
	}
}
