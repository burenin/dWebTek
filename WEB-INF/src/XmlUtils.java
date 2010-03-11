import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import java.io.*;
import java.net.*;

public class XmlUtils {
	public static Document getXmlDocument(String fileName, boolean validate) {
		File file = new File(fileName);
		SAXBuilder builder = new SAXBuilder();
		if (validate) {
			builder.setValidation(true);
			builder.setProperty(
				"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
				"http://www.w3.org/2001/XMLSchema");
			builder.setProperty(
				"http://java.sun.com/xml/jaxp/properties/schemaSource",
				"http://camel21.cs.au.dk:8181/Wiki/xsd/wikiXmlSchema.xsd");
		}
		try {
			return builder.build(file);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Document getWordDocumentFromHttpGetRequest(WikiServer server, String selectedWord) throws JDOMException {
		String service = "http://" + server.getHost() + ":" + server.getPort() + "/Wiki/Server?word=" + selectedWord;
		Document document = getDocumentFromHttpGetRequest(service, true);
		return document;
	}
	
	public static Document getXmlDocumentFromHttpGetRequest(String service) throws JDOMException {
		Document document = getDocumentFromHttpGetRequest(service, false);
		return document;
	}
	
	public static Document getDocumentFromHttpGetRequest(String service, boolean validate) throws JDOMException {
		try {
			// Open connection to service
			HttpURLConnection connection = (HttpURLConnection) (new URL(service)).openConnection();
			
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(150);
			connection.setReadTimeout(150);
			
			// Build document from inputstream
			InputStream in = connection.getInputStream();
			SAXBuilder builder = new SAXBuilder();
			if (validate) {
				builder.setValidation(true);
				builder.setProperty(
					"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
					"http://www.w3.org/2001/XMLSchema");
				builder.setProperty(
					"http://java.sun.com/xml/jaxp/properties/schemaSource",
					"http://camel21.cs.au.dk:8181/Wiki/xsd/wikiXmlSchema.xsd");
			}
			return builder.build(in);
		} catch(IOException e) {
			return null;
		}
	}
	
	public static boolean deleteXmlDocument(String fileName) {
		File file = new File(fileName);
		if (!file.exists()) {
			return true;
		}
		if (!file.canWrite()) {
			return false;
		}
		return file.delete();
	}
	
	public static boolean putStringToXml(String fileName, String content) throws JDOMException {
		File file = new File(fileName);
		try {
			StringReader reader = new StringReader(content);
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(reader);
			
			FileWriter out = new FileWriter(file);
			outputDocument(document, out);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public static void outputDocument(Document document, Writer out) throws IOException {
		XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat().setEncoding("UTF-8"));
		outputter.output(document, out);
	}
}