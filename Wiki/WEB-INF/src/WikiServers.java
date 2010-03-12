import java.net.SocketTimeoutException;
import java.util.*;
import org.jdom.*;

public class WikiServers {
	private List<WikiServer> wikiServers;
	
	public WikiServers() {
		wikiServers = new ArrayList<WikiServer>();
	}
	
	public List<WikiServer> getServers() {
		if (!isEmpty()) {
			return wikiServers;
		}
		List<WikiServer> result = new ArrayList<WikiServer>();
		List<Element> servers = getServerList();
		if (servers != null) {
			for (Element server : servers) {
				String serverName = server.getAttribute("name").getValue();
				String serverHost = server.getAttribute("host").getValue();
				String serverPort = server.getAttribute("port").getValue();
				result.add(new WikiServer(serverName, serverHost, serverPort));
			}
		}
		wikiServers = result;
		return result;
	}
	
	public WikiServer getServer(String name) {
		List<WikiServer> servers = getServers();
		for (WikiServer server : servers) {
			if (name.equals(server.getName())) {
				return new WikiServer(server.getName(), server.getHost(), server.getPort());
			}
		}
		return null;
	}
	
	public List<Element> getServerList() {
		try {
			Document document = XmlUtils.getXmlDocumentFromHttpGetRequest("http://services.brics.dk/java3/Wiki/Meta", false);
			if (document != null) {
				return document.getRootElement().getChildren("server");
			}
		} catch (SocketTimeoutException e) {
			// return null
		}
		return null;
	}
	
	public void removeBadServer(String name) {
		for (WikiServer server : wikiServers) {
			if (name.equals(server.getName())) {
				wikiServers.remove(server);
				break;
			}
		}
	}
	
	public boolean isEmpty() {
		return wikiServers.size() == 0;
	}
}