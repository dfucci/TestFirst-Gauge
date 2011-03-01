package sensorData;

import common.Configuration;
import common.ConfigurationReader;
import common.IO;
import java.text.ParseException;
import java.util.Vector;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class VSLogReader extends IO{
	
	Vector<String> entry = new Vector<String>();
	ConfigurationReader confReader = new ConfigurationReader();
	Configuration conf;
	
	
	public VSLogReader(int numOutput, Vector<String> rootElementNames, String eventTypesFile) {
		super(numOutput, rootElementNames);
		Vector<String> entry = new Vector<String>();
		entry.add("Property");
		try {
			conf = confReader.readConf(eventTypesFile, entry);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void processXMLEntry(Node node) {
		
		Document activityDoc = this.document.elementAt(1);
		//Document fileMetricDoc = this.document.elementAt(0);
		Element rootElement = this.rootElements.elementAt(1);
		//Element rootElementFile = this.rootElements.elementAt(0);
		String nodeEntry, str;
		boolean calculateMetric = false;
		
		nodeEntry = node.getAttributes().getNamedItem("xsi:type").getNodeValue();
		str = conf.getValue(nodeEntry);
		if(str.startsWith("*")){
			str = str.replace("*", "");
			calculateMetric = true;
		}
		
		if(!str.isEmpty()){
			if(nodeEntry.equals("AfterCommandExecuted")){
				if(node.getAttributes().getNamedItem("Command").getNodeValue().contains("Test") && node.getAttributes().getNamedItem("Command").getNodeValue().contains("Run")){
					writeEntry(node, activityDoc, rootElement, str, "TimeStamp", "Test Run");
					System.out.println(node.getAttributes().getNamedItem("TimeStamp").toString());
				}else{
					writeEntry(node, activityDoc, rootElement, str, "TimeStamp", node.getAttributes().getNamedItem("Command").getNodeValue());
				}
			}else if(nodeEntry.equals("AfterCommandExecuted")){
				writeEntry(node, activityDoc, rootElement, str, "TimeStamp", node.getAttributes().getNamedItem("Command").getNodeValue());
			
//			}else if((nodeEntry.equals("BeforeCommandExecuted") || nodeEntry.equals("AfterCommandExecuted") || nodeEntry.equals("OutputPaneCleared") || nodeEntry.equals("OutputPaneAdded")) && node.getAttributes().getNamedItem("Command")!=null){
//				if(node.getAttributes().getNamedItem("Command").getNodeValue().contains("Refactor")){
//					writeEntry(node, activityDoc, rootElement, str, "TimeStamp", "Refactor");
//				}else{
//					writeEntry(node, activityDoc, rootElement, str, "TimeStamp", nodeEntry);
//				}
			}else{
				writeEntry(node, activityDoc, rootElement, str, "TimeStamp", nodeEntry);
			}
			if(calculateMetric)
				;//writeFileMetric(node, fileMetricDoc, rootElementFile);
		}else{
			System.out.println("there is an unknown type in the VSLogger file: " + nodeEntry);
		}
	}

	private void writeEntry(Node node, Document activityDoc, Element rootElement, String dataStr, String timeStr, String typeStr) {
		
		if(node.getAttributes().getNamedItem(dataStr).getNodeValue().equalsIgnoreCase("UNKNOWN"))
			return;
		
		Element em = activityDoc.createElement("entry");
		em.setAttribute("type", typeStr);
		em.setAttribute("data", node.getAttributes().getNamedItem(dataStr).getNodeValue());
		em.setAttribute("fName", "UNKNOWN");
		if(!typeStr.equalsIgnoreCase("Test Run") && node.getAttributes().getNamedItem("DefinitionKind") != null){
			if(!node.getAttributes().getNamedItem("DefinitionKind").getNodeValue().isEmpty() && !node.getAttributes().getNamedItem("DefinitionKind").getNodeValue().equalsIgnoreCase("Variable") && !node.getAttributes().getNamedItem("DefinitionKind").getNodeValue().equalsIgnoreCase("Namespace"))
				em.setAttribute("fName", node.getAttributes().getNamedItem("DefinitionKind").getNodeValue() + ":" + node.getAttributes().getNamedItem("DefinitionName").getNodeValue());
		}	
		try {
			em.setAttribute("tstamp", convertTime(node.getAttributes().getNamedItem(timeStr).getNodeValue(), 'f', "8"));
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}			
		rootElement.appendChild(em);
	}
	
	/*private void writeFileMetric(Node node, Document fileMetricDoc, Element rootElement) {
		
		Element em = fileMetricDoc.createElement("entry");
		try {
			em.setAttribute("tstamp", convertTime(node.getAttributes().getNamedItem("TimeStamp").getNodeValue(), 'f', "-8"));
			em.setAttribute("lmod", convertTime(node.getAttributes().getNamedItem("TimeStamp").getNodeValue(), 'f', "-8"));
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		em.setAttribute("tool", "VSLogger");
		String filePath = node.getAttributes().getNamedItem("ActiveDocument").getNodeValue();
		em.setAttribute("fileName", filePath);
		
		em.setAttribute("className", filePath.substring(filePath.lastIndexOf("\\")+1, filePath.lastIndexOf(".") ));
		if(node.getAttributes().getNamedItem("Name") != null)
			em.setAttribute("className", node.getAttributes().getNamedItem("Name").getNodeValue());
		
		//if(node.getAttributes().getNamedItem("Kind").getNodeValue().equalsIgnoreCase("class")){
		if(node.getAttributes().getNamedItem("ChangedContents") != null){
			em.setAttribute("loc", "+" + calculateLoc(node.getAttributes().getNamedItem("ChangedContents").getNodeValue()));
			em.setAttribute("data", "Mod");
		}else if(node.getAttributes().getNamedItem("Contents") != null){
			em.setAttribute("loc", calculateLoc(node.getAttributes().getNamedItem("Contents").getNodeValue()));
			em.setAttribute("data", "New");
		}else{
			em.setAttribute("loc", "+0");
			em.setAttribute("data", "Mod");
		}
		rootElement.appendChild(em);
	}*/
	
	/*private String calculateLoc(String nodeValue) {
	
		Scanner scanner = new Scanner(nodeValue);
		String line = null;
		int loc = 0;
		
		try{
			line = scanner.nextLine();
		}catch(NoSuchElementException ne){
			; 
		}
		while(line !=null){
			if(line.contains("//") || line.contains("#") ||  line.trim().isEmpty()) //line.contains("{") || line.contains("}") ||
				;
			else 
				loc++;
			try{
				line = scanner.nextLine();
			}catch(NoSuchElementException nse){
				break;
			}
		}
		return Integer.toString(loc);
	}*/

}
