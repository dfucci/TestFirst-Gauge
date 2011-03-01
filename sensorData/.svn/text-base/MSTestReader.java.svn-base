package sensorData;

import java.text.ParseException;
import java.util.Vector;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import common.IO;


public class MSTestReader extends IO{
	
	public MSTestReader(int numOutput, Vector<String> rootElementNames) {
		
		super(numOutput, rootElementNames);	
	}
	
	@Override
	protected void processXMLEntry(Node node) {
		
		Document testDoc = this.document.elementAt(0);
		Element rootElement = this.rootElements.elementAt(0);
		Element em = testDoc.createElement("entry");
		
		// TODO Are we interested in the test-case names? If yes, fix the following assumption: there is only one test case and its name is "hard-coded" below.
		em.setAttribute("testCaseName", "UnitTest");
		em.setAttribute("testName", node.getAttributes().getNamedItem("testName").getNodeValue());
		em.setAttribute("tool", "MSTest");
		em.setAttribute("errorString", " ");
		em.setAttribute("failureString", " ");
		
		if(!node.getAttributes().getNamedItem("outcome").getNodeValue().equalsIgnoreCase("Passed"))
			if(node.getAttributes().getNamedItem("outcome").getNodeValue().equalsIgnoreCase("Failed"))
				em.setAttribute("failureString", "failed");
			else
				em.setAttribute("errorString", "error");
		
		try {
			em.setAttribute("tstamp", convertTime(node.getAttributes().getNamedItem("startTime").getNodeValue(), 'f', "0"));
			em.setAttribute("elapsedTime", convertTime(node.getAttributes().getNamedItem("duration").getNodeValue(), 's', "8"));
			//em.setAttribute("elapsedTime", "5");
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (StringIndexOutOfBoundsException se) {
			try {
				em.setAttribute("elapsedTime", convertTime("00:00:00.000-08:00", 's', "8"));
			} catch (DOMException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		rootElement.appendChild(em);
	}
}
