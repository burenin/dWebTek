import java.util.*;
import org.jdom.*;
import org.jdom.output.*;
import org.apache.commons.lang.*;

public class Model {
	private String xsltPath = "../webapps/Wiki/xslt/"; // from "/users/cqa/apache-tomcat-6.0.24/bin/"
	private List<WikiServer> servers;
	private String pattern;
	private String selectedWord;
	private String selectedWiki;
	private String defaultWiki;
	private boolean editable;
	private String message;
	
	public Model() {
		servers = new ArrayList<WikiServer>();
		defaultWiki = "jtjcWiki";
	}
	
	public void setPattern(String pattern) {
		if (pattern == null) {
			pattern = "";
		}
		this.pattern = pattern;
	}
	
	public void setSelectedWord(String name) {
		if (name == null) {
			name = "";
		}
		selectedWord = name;
	}
	
	public void setSelectedWiki(String name) {
		if (name == null) {
			name = "";
		}
		selectedWiki = name;
	}
	
	public String getDefaultWiki() {
		return defaultWiki;
	}
	
	public List<WikiServer> getServers() {
		List<WikiServer> result = new ArrayList<WikiServer>();
		List<Element> servers = null;
		Document document;
		try {
			document = XmlUtils.getXmlDocumentFromHttpGetRequest("http://services.brics.dk/java3/Wiki/Meta");
		} catch (JDOMException e) {
			return result;
		}
		if (document != null) {
			servers = document.getRootElement().getChildren("server");
		}
		if (servers != null) {
			for (Element server : servers) {
				String name = server.getAttribute("name").getValue();
				String host = server.getAttribute("host").getValue();
				String port = server.getAttribute("port").getValue();
				result.add(new WikiServer(name, host, port));
			}
		}
		Collections.sort(result, WikiServer.getNameComparator());
		return result;
	}
	
	public WikiServer getServer(String name) {
		List<WikiServer> servers = getServers();
		for (WikiServer server : servers) {
			if (name.equals(server.getName())) {
				return server;
			}
		}
		return null;
	}
	
	public List<WikiWord> getWords() {
		List<WikiWord> matches = new ArrayList<WikiWord>();
		List<WikiServer> servers = getServers();
		for (WikiServer server : servers) {
			if (selectedWiki == null || selectedWiki.equals("")) {
				matches.addAll(server.getWords(pattern));
			} else if (selectedWiki.contains(server.getName())) {
				matches.addAll(server.getWords(pattern));
			}
		}
		Collections.sort(matches, WikiWord.getNameComparator());
		return matches;
	}
	
	public WikiWord getRandomWord() {
		List<WikiWord> words = getWords();
		if (words.size() != 0) {
			int random = new Random().nextInt(words.size());
			return words.get(random);
		}
		return null;
	}
	
	public Document getXmlDocumentFromServer(String wiki, String word) throws JDOMException {
		Document xmlDocument = null;
		WikiServer server = getServer(wiki);
		if (server != null) {
			xmlDocument = XmlUtils.getWordDocumentFromHttpGetRequest(server, word);
		}
		return xmlDocument;
	}
	
	public String getText() {
		if (message != null) {
			return null;
		}
		Document xmlDocument;
		try {
			xmlDocument = getXmlDocumentFromServer(selectedWiki, selectedWord);
		} catch (JDOMException e) {
			message = getError("The word <i>" + selectedWord + "</i> on the wiki <i>" + selectedWiki + "</i> has bad syntax.");
			return null;
		}
		if (xmlDocument != null) {
			Document textDocument = XsltUtils.transform(xmlDocument, xsltPath + "wikixml-to-text.xsl");
			if (textDocument != null) {
				// Get string of root element "text" and escape for presentation
				try {
					String text = textDocument.getRootElement().getText();
					return StringEscapeUtils.escapeHtml(text);
				} catch(IllegalStateException e) {
					message = getError("The namespace of the word, " + selectedWord + ", on the wiki, " + selectedWiki + ", does not match <i>http://cs.au.dk/dWebTek/WikiXML</i>.");
					return null;
				}
			} else {
				message = getError("The word <i>" + selectedWord + "</i> was found, but could not be transformed to text (bad syntax).");
				return null;
			}
		} else {
			message = getError("The word <i>" + selectedWord + "</i> on the wiki <i>" + selectedWiki + "</i> does not exist.");
			return null;
		}
	}
	
	public boolean getEditable() {
		return editable;
	}
	
	public String getMessage() {
		// Prevent that an error on /Read, can trigger the /Edit-message
		String result = message;
		message = null;
		return result;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getXhtml() {
		editable = false;
		message = null;
		
		Document xmlDocument;
		if (selectedWord == null || selectedWord.equals("")) {
			WikiWord word = getRandomWord();
			if (word != null) {
				selectedWord = word.getName();
				selectedWiki = word.getServer();
			} else {
				message = getError("No words matching <i>" + pattern + "</i>");
				return null;
			}
		}
		try {
			xmlDocument = getXmlDocumentFromServer(selectedWiki, selectedWord);
		} catch (JDOMException e) {
			message = getError(e.getMessage());
			return null;
		}
		if (xmlDocument != null) {
			Document xhtmlDocument = XsltUtils.transform(xmlDocument, xsltPath + "wikixml-to-xhtml.xsl");
			if (xhtmlDocument != null) {
				// Get body
				Namespace xhtmlNamespace = Namespace.getNamespace("http://www.w3.org/1999/xhtml");
				try {
					// Output body element to String
					Element body = xhtmlDocument.getRootElement().getChild("body", xhtmlNamespace);
					XMLOutputter outputter = new XMLOutputter();
					String xhtml = outputter.outputString(body);
					editable = true;
					return xhtml;
				} catch(IllegalStateException e) {
					message = getError("The namespace of the word <i>" + selectedWord + "</i> on the wiki <i>" + selectedWiki + "</i> does not match <i>http://cs.au.dk/dWebTek/WikiXML</i>.");
					return null;
				}
			} else {
				message = getError("The word <i>" + selectedWord + "</i> was found, but could not be transformed to XHTML (bad syntax).");
				return null;
			}
		} else {
			message = getError("The word <i>" + selectedWord + "</i> on the wiki <i>" + selectedWiki + "</i> does not exist.");
			return null;
		}
	}
	
	public String getError(String error) {
		return
			"<h2>Error</h2>" +
			"<p>" + error + "</p>";
	}
}