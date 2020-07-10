package xml;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CreateXMLDOM {

	public static void createXML(String email, String emailSubject, String emailBody) {
		final String xmlFilePath = "./data/" + email + ".xml";
		
		DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder icBuilder;
	    
	    try {
	    	icBuilder = icFactory.newDocumentBuilder();
	    	Document doc = icBuilder.newDocument();
	    	Element mainRootElement = doc.createElement("email");
	    	doc.appendChild(mainRootElement);
	    	
	    	mainRootElement.appendChild(getEmailElements(doc, "subject", emailSubject));
	    	mainRootElement.appendChild(getEmailElements(doc, "body", emailBody));
	    	
	    	Transformer transformer = TransformerFactory.newInstance().newTransformer();
    		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
	        DOMSource source = new DOMSource(doc);
	         
	        StreamResult streamResult = new StreamResult(new File(xmlFilePath));
	        transformer.transform(source, streamResult);

	 		System.out.println("Sacuvan fajl!");
	    } catch (Exception e) {
	         e.printStackTrace();
	    }
	}
	
	private static Node getEmailElements(Document doc, String name, String value) {
	     Element node = doc.createElement(name);
	     node.appendChild(doc.createTextNode(value));
	     return node;
	}
	
	// Ispisivanje dokumenta u konzoli
	public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	    transformer.transform(new DOMSource(doc), 
	         new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	}
	
	// Ispisivanje poruke iz dokumenta
	public static void printEmail(Document doc) {
		Node fc = doc.getFirstChild();
		NodeList list = fc.getChildNodes();
		for (int i = 0; i <list.getLength(); i++) {
			Node node = list.item(i);
			if("subject".equals(node.getNodeName())) {
				System.out.println("Subject: " + node.getTextContent());
			}
			if("body".equals(node.getNodeName())) {
				System.out.println("Body: " + node.getTextContent());
			}
		}
	}
}
