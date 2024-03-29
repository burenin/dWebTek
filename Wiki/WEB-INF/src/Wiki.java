import java.io.*;
import java.util.*;
import org.jdom.*;

public class Wiki {
	private String xmlPath = "../webapps/Wiki/"; // from "/users/cqa/apache-tomcat-6.0.24/bin/"
	private Document document;
	
	public Wiki() {
		document = XmlUtils.getXmlDocument(xmlPath + "words.xml", false);
		if (document == null) {
			Element root = new Element("wiki");
			document = new Document(root);
			save();
		}
	}
	
	public void putWord(String name) {
		Element root = document.getRootElement();
		Element word = getWord(name);
		if (word == null) {
			// Add new word
			word = new Element("word").setText(name).setAttribute("version", "1");
			root.addContent(word);
		} else {
			// Add new version
			int version = getWordVersion(word);
			word.setAttribute("version", version++ + "");
		}
		save();
	}
	
	public boolean deleteWord(String name) {
		Element word = getWord(name);
		if (word != null) {
			// Delete XML
			boolean success = XmlUtils.deleteXmlDocument(xmlPath + "words/" + name + ".xml");
			if (!success) {
				return false;
			} else {
				// Remove from memory
				List<Element> words = getWords();
				for (Element wordElement : words) {
					if (wordElement.getText().equals(name)) {
						wordElement.detach();
						break;
					}
				}
				save();
				return true;
			}
		}
		// Word doesn't exist (successful delete)
		return true;
	}
	
	public Element getWord(String name) {
		List<Element> words = getWords();
		for (Element w : words) {
			if (w.getText().equals(name)) {
				return w;
			}
		}
		return null;
	}
	
	public int getWordVersion(Element word) {
		int version = 1;
		try {
			version = word.getAttribute("version").getIntValue();
		} catch (DataConversionException e) {
			//
		}
		return version;
	}
	
	public List<Element> getWords() {
		return document.getRootElement().getChildren();
	}
	
	public Document getDocument() {
		return (Document) document.clone();
	}
	
	public void save() {
		File f = new File(xmlPath + "words.xml");
		try {
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(f), "utf-8");
			XmlUtils.outputDocument(document, out);
			out.close();
		} catch (IOException e) {
		}
	}
}
