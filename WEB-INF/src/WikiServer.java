import java.net.*;
import java.util.*;
import org.jdom.*;

public class WikiServer {
	private String name;
	private String host;
	private String port;
	
	public WikiServer(String name, String host, String port) {
		this.name = name;
		this.host = host;
		this.port = port;
	}
	
	public String getName() {
		return name;
	}
	
	public String getHost() {
		return host;
	}
	
	public String getPort() {
		return port;
	}
	
	public List<WikiWord> getWords(String pattern) {
		List<WikiWord> result = new ArrayList<WikiWord>();
		Document document;
		try {
			document = XmlUtils.getXmlDocumentFromHttpGetRequest("http://" + host + ":" + port + "/Wiki/Server");
		} catch (JDOMException e) {
			return result;
		}
		if (document != null) {
			List<Element> words = document.getRootElement().getChildren("word");
			for (Element word : words) {
				String name = word.getText();
				if (name.toLowerCase().contains(pattern.toLowerCase())) {
					result.add(new WikiWord(name, this.name));
				}
			}
		}
		return result;
	}
	
	public int getWordVersion(String name) {
		int result = -1;
		Document document;
		try {
			document = XmlUtils.getXmlDocumentFromHttpGetRequest("http://" + host + ":" + port + "/Wiki/Server");
		} catch (JDOMException e) {
			return result;
		}
		if (document != null) {
			List<Element> words = document.getRootElement().getChildren("word");
			for (Element word : words) {
				if (name.equals(word.getText())) {
					int version = -1;
					try {
						version = word.getAttribute("version").getIntValue();
					} catch (DataConversionException e) {
						//
					}
					if (version > result) {
						result = version;
					}
				}
			}
		}
		return result;
	}
	
	public static Comparator<WikiServer> getNameComparator() {
		return new
			Comparator<WikiServer>() {
				public int compare(WikiServer object, WikiServer otherObject) {
					if (object.name.equals(otherObject.name)) {
						if (object.host.equals(otherObject.host)) {
							return object.port.compareTo(otherObject.port);
						} else {
							return object.host.compareTo(otherObject.host);
						}
					} else if (object.name.equals("jtjcWiki")) {
						return -1;
					} else if (otherObject.name.equals("jtjcWiki")) {
						return 1;
					} else {
						return object.name.compareTo(otherObject.name);
					}
				}
			};
	}
}