import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;

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
		Document document = XmlUtils.getXmlDocumentFromHttpGetRequest("http://" + host + ":" + port + "/Wiki/Server");
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