import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.*;
import org.apache.commons.lang.*;

public class Model {
	private String xsltPath = "../webapps/Wiki/xslt/"; // from "/users/cqa/apache-tomcat-6.0.24/bin/"
	private List<WikiServer> servers;
	private String pattern;
	private String selectedWord;
	private int selectedWordVersion;
	private String selectedWiki;
	private String defaultWiki;
	private boolean editable;
	private boolean conflict;
	private String oldWord;
	private String eTag;
	private String message;
	private String text;
	
	public Model() {
		servers = new ArrayList<WikiServer>();
		defaultWiki = "jtjcWiki";
		editable = false;
		conflict = false;
		message = null;
		text = null;
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
	
	public void setVersion(int version) {
		selectedWordVersion = version;
	}
	
	public int getVersions() {
		List<String> versions = new ArrayList<String>();
		if (selectedWiki.equals(defaultWiki)) {
			WikiServer server = getServer(selectedWiki);
			int result = server.getWordVersion(selectedWord);
			return result;
		}
		return 1;
	}
	
	public boolean getEditable() {
		boolean result = editable;
		editable = false;
		return result;
	}
	
	public void setConflict(boolean conflict) {
		this.conflict = conflict;
	}
	
	public boolean getConflict() {
		boolean result = conflict;
		conflict = false;
		return result;
	}
	
	public String getMessage() {
		// Prevent that an error on /Read, can trigger the /Edit-message
		String result = message;
		message = null;
		return result;
	}
	
	public void setMessage(String header, String message, String additional) {
		this.message = "<h2>" + header + "</h2>" +
					   "<p>" + message + "</p>" +
					   additional;
	}
	
	public String getTextlost() {
		String result = text;
		text = null;
		return result;
	}
	
	public void setText(String text) {
		this.text = text;
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
		String versionRequest = "";
		if (selectedWordVersion != -1) {
			versionRequest = "&version=" + selectedWordVersion;
		}
		Document xmlDocument = null;
		WikiServer server = getServer(wiki);
		if (server != null) {
			xmlDocument = XmlUtils.getWordDocumentFromHttpGetRequest(server, word + versionRequest);
		}
		return xmlDocument;
	}
	
	public void setETag(String eTag) {
		this.eTag = eTag;
	}
	
	public String getEtag() {
		String result = eTag;
		eTag = "";
		return result;
	}
	
	public void setOldword(String name) {
		oldWord = name;
	}
	
	public String getOldword() {
		String result = oldWord;
		oldWord = null;
		return result;
	}
	
	public String getText() {
		if (message != null || conflict) {
			return null;
		}
		Document xmlDocument = null;
		try {
			// Open connection to service
			WikiServer server = getServer(selectedWiki);
			if (server != null) {
				String service = "http://" + server.getHost() + ":" + server.getPort() + "/Wiki/Server?word=" + selectedWord;
				HttpURLConnection connection = (HttpURLConnection) (new URL(service)).openConnection();
				
				connection.setRequestMethod("GET");
				connection.setConnectTimeout(150);
				connection.setReadTimeout(150);
				eTag = connection.getHeaderField("ETag");
				
				// Build document from inputstream
				InputStream in = connection.getInputStream();
				SAXBuilder builder = new SAXBuilder();
				if (false) {
					builder.setValidation(true);
					builder.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", "xmlSchema");
				}
				xmlDocument = builder.build(in);
			} else {
				setMessage("Server does not exist", "The wiki <i>" + selectedWiki + "</i> does not exist.", "");
				return null;
			}
		} catch (JDOMException e) {
			setMessage("Bad XML syntax", "The word <i>" + selectedWord + "</i> on the wiki <i>" + selectedWiki + "</i> has bad syntax.", "");
			return null;
		} catch(IOException e) {
			xmlDocument = null;
		}
		if (xmlDocument != null) {
			Document textDocument = XsltUtils.transform(xmlDocument, xsltPath + "wikixml-to-text.xsl");
			if (textDocument != null) {
				// Get string of root element "text" and escape for presentation
				try {
					String text = textDocument.getRootElement().getText();
					return StringEscapeUtils.unescapeHtml(text);
				} catch(IllegalStateException e) {
					setMessage("Bad XML wiki namespace", "The namespace of the word, " + selectedWord + ", on the wiki, " + selectedWiki + ", does not match <i>http://cs.au.dk/dWebTek/WikiXML</i>.", "");
					return null;
				}
			} else {
				setMessage("Bad wiki syntax", "The word <i>" + selectedWord + "</i> was found, but could not be transformed to text (bad syntax).", "");
				return null;
			}
		} else {
			setMessage("Not Found", "The word <i>" + selectedWord + "</i> on the wiki <i>" + selectedWiki + "</i> does not exist.", "");
			return null;
		}
	}
	
	public String getXhtml() {
		if (message != null) {
			return null;
		}
		String versionRequest = "";
		if (selectedWordVersion != -1) {
			versionRequest = " (version " + selectedWordVersion + ")";
		}
		
		editable = false;
		
		Document xmlDocument;
		if (selectedWord == null || selectedWord.equals("")) {
			WikiWord word = getRandomWord();
			if (word != null) {
				selectedWord = word.getName();
				selectedWiki = word.getServer();
			} else {
				setMessage("Not Found", "No words matching <i>" + pattern + "</i>", "");
				return null;
			}
		}
		try {
			xmlDocument = getXmlDocumentFromServer(selectedWiki, selectedWord); 
		} catch (JDOMException e) {
			setMessage("Bad XML syntax", "The word <i>" + selectedWord + "</i>" + versionRequest + " on the wiki <i>" + selectedWiki + "</i> has bad syntax.", "");
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
					if (xhtml.length() > 45) { // != <body xmlns="http://www.w3.org/1999/xhtml" />
						xhtml = StringEscapeUtils.unescapeHtml(xhtml.substring(43, xhtml.length() - 7));
					}
					editable = true;
					return xhtml;
				} catch(IllegalStateException e) {
					setMessage("Bad XML wiki namespace", "The namespace of the word <i>" + selectedWord + "</i>" + versionRequest + " on the wiki <i>" + selectedWiki + "</i> does not match <i>http://cs.au.dk/dWebTek/WikiXML</i>.", "");
					return null;
				}
			} else {
				setMessage("Bad wiki syntax", "The word <i>" + selectedWord + "</i>" + versionRequest + " was found, but could not be transformed to XHTML (bad syntax).", "");
				return null;
			}
		} else {
			setMessage("Not Found", "The word <i>" + selectedWord + "</i>" + versionRequest + " on the wiki <i>" + selectedWiki + "</i> does not exist.", "");
			return null;
		}
	}
}