import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.jdom.*;

public class Controller extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		HttpSession session = request.getSession();
		String command = request.getServletPath();
		Model model;
		
		// Get ServletContext-object
		ServletContext servletContext = getServletContext();
		
		// Check session
		if (session.getAttribute("model") == null) {
			model = new Model();
			session.setAttribute("model", model);
		}
		model = (Model) session.getAttribute("model");
		
		// Set session
		String luck = request.getParameter("luck");
		if (luck != null && luck.equals("on")) {
			command = "/Luck";
		}
		
		String word = request.getParameter("word");
		model.setSelectedWord(word);
		
		String wiki = request.getParameter("wiki");
		model.setSelectedWiki(wiki);
		
		String pattern = request.getParameter("pattern");
		model.setPattern(pattern);
		
		// Handle request		
		if (!command.equals("/Entry")) {
			if (command.equals("/Read") && word != null && !word.equals("")) {
				servletContext.getRequestDispatcher("/jsp/wiki_read.jsp").forward(request, response);
				
			} else if (command.equals("/Edit") && wiki != null && !wiki.equals("")) {
				servletContext.getRequestDispatcher("/jsp/wiki_edit.jsp").forward(request, response);
				
			} else if (command.equals("/Luck")) {
				model.setPattern(pattern);
				servletContext.getRequestDispatcher("/jsp/wiki_read.jsp").forward(request, response);
				
			} else {
				command = "/Entry";
			}
		}
		
		if (command.equals("/Entry")) {			
			// Forward
			String delete = request.getParameter("delete");
			if (delete == null) {
				servletContext.getRequestDispatcher("/jsp/wiki_entry.jsp").forward(request, response);
				
			} else if (delete != null && delete.equals("yes") && wiki != null && !wiki.equals("") && word != null && !word.equals("")) {
				WikiServer server = model.getServer(wiki);
				if (server != null) {
					try {
						// Open connection to service
						String service = "http://" + server.getHost() + ":" + server.getPort() + "/Wiki/Server?word=" + word;
						HttpURLConnection connection = (HttpURLConnection) (new URL(service)).openConnection();
						connection.setRequestMethod("DELETE");
						connection.setReadTimeout(150);
						connection.setConnectTimeout(150);
						
						// Check response			
						int responseCode = connection.getResponseCode();
						if (responseCode >= 200 && responseCode < 300) {
							model.setPattern("");
							model.setSelectedWiki("");
							servletContext.getRequestDispatcher("/jsp/wiki_entry.jsp").forward(request, response);
						} else {
							servletContext.getRequestDispatcher("/jsp/wiki_read.jsp").forward(request, response);
						}
					} catch (Exception e) {
						//
					}
				}
			} else {
				// required attributes not filled
			}
		}
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		HttpSession session = request.getSession();
		String command = request.getServletPath();
		Model model;
		
		// Get ServletContext-object
		ServletContext servletContext = getServletContext();
		
		if (command.equals("/Edit")) {
			if (session.getAttribute("model") == null) {
				model = new Model();
				session.setAttribute("model", model);
			}
			model = (Model) session.getAttribute("model");

			String word = request.getParameter("word");
			String oldWord = request.getParameter("oldword");
			String wiki = request.getParameter("wiki");
			if (word != null && !word.equals("") && wiki != null && !wiki.equals("")) {
				WikiServer server = model.getServer(wiki);
				if (server != null) {
					String text = request.getParameter("text");
					StringReader reader = new StringReader(text);
					try {
						String service = "http://" + server.getHost() + ":" + server.getPort() + "/Wiki/Server?word=";
						
						int deleteResponse = -1;
						int putResponse = -1;
						
						if (oldWord != null && !oldWord.equals(word)) {
							// Do a delete request to remove oldWord
							HttpURLConnection connection = (HttpURLConnection) (new URL(service + oldWord)).openConnection();
							connection.setRequestMethod("DELETE");
							connection.setReadTimeout(150);
							connection.setConnectTimeout(150);
							
							deleteResponse = connection.getResponseCode();
						}
						
						// Do a put request to create/edit word
						HttpURLConnection connection = (HttpURLConnection) (new URL(service + word)).openConnection();
						connection.setRequestMethod("PUT");
						connection.setDoOutput(true);
						connection.setReadTimeout(150);
						connection.setConnectTimeout(150);
						
						// Write form to service
						BufferedWriter out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
						Txt2WikiXML parser = new Txt2WikiXML(reader);
						Document xmlDocument = parser.parseWiki();
						XmlUtils.outputDocument(xmlDocument, out);
						
						// Check response
						putResponse = connection.getResponseCode();
						
						boolean success;
						
						if (oldWord != null && !oldWord.equals(word)) {
							if (putResponse >= 200 && putResponse < 300 && deleteResponse >= 200 && deleteResponse < 300) {
								success = true;
							} else {
								success = false;
							}
						} else {
							if (putResponse >= 200 && putResponse < 300) {
								success = true;
							} else {
								success = false;
							}
						}
						
						if (success) {
							model.setSelectedWord(word);
							model.setSelectedWiki(wiki);
							servletContext.getRequestDispatcher("/jsp/wiki_read.jsp").forward(request, response);
						} else {
							model.setMessage("Fejl et sted");
							servletContext.getRequestDispatcher("/jsp/wiki_edit.jsp?test=lol").forward(request, response);
						}
					} catch (Exception e) {
						//
					}
				} else {
					// The server does not exist
				}
			}
		}
	}
}