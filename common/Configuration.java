package common;

import java.util.Vector;


public class Configuration {

	private Vector<Property> properties;
	
	public Configuration(){
		this.properties = new Vector<Property>();
	}
	public void add(Property property){
		this.properties.add(property);
	}
	
	public String getValue(String name){
		int i, size = properties.size();
		
		for(i = 0; i < size; i++)
			if(properties.elementAt(i).getName().equals(name))
				return properties.elementAt(i).getValue();
		return "";
	}
	
	public String getDescription(String name){
		int i, size = properties.size();
		
		for(i = 0; i < size; i++)
			if(properties.elementAt(i).getName().equals(name))
				return properties.elementAt(i).getDescription();
		return null;
	}
}
