package common;

import java.util.Vector;

import org.w3c.dom.Node;

public class ConfigurationReader extends IO {
	Configuration conf;

	public ConfigurationReader(){
		super((int)0, (Vector<String>)null);
		conf = new Configuration();
	}
	public Configuration readConf(String file, Vector<String> entry){
		try {
			this.processXMLFile(file, entry);
		} catch (Exception e) {
			System.out.println("\n(-) Configuration file " + file + " not found...");
			e.printStackTrace();
		}
		return conf;
	}
	@Override
	protected void processXMLEntry(Node node) {
		Property property = new Property(node.getAttributes().getNamedItem("name").getNodeValue(), node.getAttributes().getNamedItem("value").getNodeValue(), node.getAttributes().getNamedItem("description").getNodeValue());
		this.conf.add(property);		
	}
}
