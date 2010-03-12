import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.jdom.*;

public class Server extends HttpServlet {
	private String xmlPath = "../webapps/Wiki/words/"; // from "/users/cqa/apache-tomcat-6.0.24/bin/"
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		PrintWriter out = response.getWriter();
		
		// Get ServletContext-object
		ServletContext servletContext = getServletContext();
		Wiki wiki;
		if (servletContext.getAttribute("wiki") != null) {
			wiki = (Wiki) servletContext.getAttribute("wiki");
		} else {
			wiki = new Wiki();
			servletContext.setAttribute("wiki", wiki);
		}
		
		// Get request-info
		String ifMatchHeader = request.getHeader("If-Match");
		String ifNoneMatchHeader = request.getHeader("If-None-Match");
		String wordName = request.getParameter("word");
		
		// Handle request
		if (wordName != null && !wordName.equals("")) {
			// Print file
			wordName = escapeFileName(wordName);
			Element word = wiki.getWord(wordName);
			if (word != null) {
				String eTag = wiki.getWordVersion(word) + "";
				if ((ifNoneMatchHeader != null && ifNoneMatchHeader.equals(eTag)) || (ifMatchHeader != null && !ifMatchHeader.equals(eTag))) {
					// Not Modified
					response.setStatus(304);
				} else {
					Document document = XmlUtils.getXmlDocument(xmlPath + wordName + ".xml", true);
					if (document != null) {
						response.setHeader("Content-Type", "application/xml; charset:UTF-8");
						response.setHeader("ETag", eTag);
						response.setHeader("Cache-Control", "public, no-cache");
						try {
							XmlUtils.outputDocument(document, out);
						} catch (IOException e) {
							// Internal Server Error
							response.sendError(500, "Server could not write requested XML-document to client");
						}
					} else {
						// Not Found
						response.sendError(500, "The requested XML-document has bad syntax");
					}
				}
			} else {
				// Not Found
				response.sendError(404, "The requested XML-document does not exist");
			}
		} else if (wordName == null) {
			// Print list
			Document document = wiki.getDocument();
			response.setHeader("Content-Type", "application/xml; charset:UTF-8");
			try {
				XmlUtils.outputDocument(document, out);
			} catch (IOException e) {
				// Internal Server Error
				response.sendError(500, "Server could not write requested XML-document to client");
			}
		} else { // xmlFileName.equals("")
			// Bad Request
			response.sendError(400, "A XML-document name must be specified (/Server?word=<name>)");
		}
	}

	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		
		// Get ServletContext-object
		ServletContext servletContext = getServletContext();
		Wiki wiki;
		if (servletContext.getAttribute("wiki") != null) {
			wiki = (Wiki) servletContext.getAttribute("wiki");
		} else {
			wiki = new Wiki();
			servletContext.setAttribute("wiki", wiki);
		}
		
		// Get request-info
		String word = request.getParameter("word");
		
		response.getWriter().write("Test");
		
		// Handle request
		if (word == null || word.equals("")) {
			// Bad Request
			response.sendError(400, "A XML-document name must be specified (/Server?word=<name>)");
		} else {
			word = escapeFileName(word);
			boolean success = wiki.deleteWord(word);
			if (!success) {
				// Internal Server Error
				response.sendError(501, "Server could not delete the XML-document");
			}
		}
	}
	
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		
		// Check encoding
		String encoding = request.getCharacterEncoding();
		if (encoding != null && !encoding.equals("UTF-8")) {
			// Bad Request (encoding)
			response.sendError(400, "XML-document must be encoded with UTF-8");
		}
		
		// Get body from request
		BufferedReader reader = null;
		String body = "";
		try {
			reader = request.getReader();
			String line = reader.readLine();
			while (line != null) {
				body += line + System.getProperty("line.separator");
				line = reader.readLine();
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		body = URLDecoder.decode(body, "UTF-8");
		
		// Get ServletContext-object
		ServletContext servletContext = getServletContext();
		Wiki wiki;
		if (servletContext.getAttribute("wiki") != null) {
			wiki = (Wiki) servletContext.getAttribute("wiki");
		} else {
			wiki = new Wiki();
			servletContext.setAttribute("wiki", wiki);
		}
		
		// Get request-info
		String wordName = request.getParameter("word");
		
		// Handle request
		if (wordName == null || wordName.equals("")) {
			// Bad Request
			response.sendError(400, "A XML-document name must be specified (/Server?word=<name>)");
		} else {
			wordName = escapeFileName(wordName);
			boolean success = XmlUtils.putStringToXml(xmlPath + wordName + ".xml", body);
			if (success) {
				// Validate XML-document and put it
				Document document = XmlUtils.getXmlDocument(xmlPath + wordName + ".xml", true);
				if (document != null) {
					wiki.putWord(wordName);
				} else {
					// Bad syntax
					response.sendError(400, "The XML-document to be put has bad syntax");
				}
			} else {
				// Internal Server Error
				response.sendError(500, "Server could not create XML-document");
			}
		}
	}
	
	public String escapeFileName(String name) {
		return name.replaceAll("[^a-zA-Z_]{1}+", "");
	}
}