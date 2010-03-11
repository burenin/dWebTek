import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.lang.*;
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
		
		String versionRequest = request.getParameter("version");
		int version = -1;
		if (versionRequest != null && !versionRequest.equals("")) {
			try {
				version = Integer.parseInt(versionRequest);
			} catch(NumberFormatException e) {
				//
			}
		}
		model.setVersion(version);
		
		// Handle request		
		if (!command.equals("/Entry")) {
			if (command.equals("/Read") && word != null && !word.equals("")) {
				if (wiki == null || wiki.equals("")) {
					model.setSelectedWiki(model.getDefaultWiki());
				}
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
						String all = request.getParameter("all");
						if (all != null && all.equals("yes")) {
							all = "&all=yes";
						} else {
							all ="";
						}
						String service = "http://" + server.getHost() + ":" + server.getPort() + "/Wiki/Server?word=" + word + all;
						HttpURLConnection connection = (HttpURLConnection) (new URL(service)).openConnection();
						connection.setRequestMethod("DELETE");
						connection.setReadTimeout(150);
						connection.setConnectTimeout(150);
						
						// Check response			
						int responseCode = connection.getResponseCode();
						if (responseCode >= 200 && responseCode < 300) {
							model.setPattern("");
							model.setSelectedWiki("");
							model.setMessage("Successful deletion", "The word <i>" + word + "</i> was deleted from <i>" + wiki + "</i>.", "");
							servletContext.getRequestDispatcher("/jsp/wiki_read.jsp").forward(request, response);
						} else {
							model.setMessage("Bad deletion", "The word <i>" + word + "</i> could not be deleted from <i>" + wiki + "</i>, an internal error on the server occured.", "");
							servletContext.getRequestDispatcher("/jsp/wiki_read.jsp").forward(request, response);
						}
					} catch (Exception e) {
						model.setMessage("Bad deletion", "The word <i>" + word + "</i> could not be deleted from <i>" + wiki + "</i>, since the server does not respond.", "");
						servletContext.getRequestDispatcher("/jsp/wiki_read.jsp").forward(request, response);
					}
				} else {
					model.setMessage("Bad deletion", "The word <i>" + word + "</i> was not deleted, since the server <i>" + wiki + "</i> does not exist. <a href=\"Entry\">Return to entries</a>.", "");
					servletContext.getRequestDispatcher("/jsp/wiki_read.jsp").forward(request, response);
				}
			} else {
				// required attributes not filled
				servletContext.getRequestDispatcher("/jsp/wiki_entry.jsp").forward(request, response);
			}
		}
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		HttpSession session = request.getSession();
		String command = request.getServletPath();
		Model model;
		
		if(request.getCharacterEncoding() == null) {
			request.setCharacterEncoding("UTF-8");
		}
		
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
			String text = request.getParameter("text");
			String oldETag = request.getParameter("etag");
			String ignoreConflict = request.getParameter("ignoreconflict");
			
			if (word != null && !word.equals("") && wiki != null && !wiki.equals("")) {
				WikiServer server = model.getServer(wiki);
				if (server != null) {
					try {
						String service = "http://" + server.getHost() + ":" + server.getPort() + "/Wiki/Server?word=";

						boolean modified = false;
						
						if (ignoreConflict == null || !ignoreConflict.equals("yes")) {
							
							if (oldETag == null || oldETag.equals("")) {
								HttpURLConnection existConnection = (HttpURLConnection) (new URL(service + word)).openConnection();
								existConnection.setRequestMethod("GET");
								existConnection.setReadTimeout(150);
								existConnection.setConnectTimeout(150);
								
								if (existConnection.getResponseCode() != 404) {
									modified = true;
									model.setText(text);
									model.setConflict(true);
									model.setMessage("Conflict detected", "A conflict was detected. The word you are trying to create, has already been put.", "");
									servletContext.getRequestDispatcher("/jsp/wiki_edit.jsp").forward(request, response);
								}
							} else if (oldETag != null && !oldETag.equals("")) {
								HttpURLConnection existConnection = (HttpURLConnection) (new URL(service + word)).openConnection();
								existConnection.setRequestMethod("GET");
								existConnection.setReadTimeout(150);
								existConnection.setConnectTimeout(150);
								
								existConnection.setRequestProperty("If-None-Match", oldETag);
								
								if (existConnection.getResponseCode() != 304) {
									modified = true;
									model.setETag(oldETag);
									model.setText(text);
									model.setConflict(true);
									model.setOldword(oldWord);
									model.setMessage("Conflict detected", "A conflict was detected. The word you are trying to update, has been updated before your put-request.", "");
									servletContext.getRequestDispatcher("/jsp/wiki_edit.jsp").forward(request, response);
								}
							}
						}
						
						if (!modified) {
						
							int deleteResponse = -1;
							int putResponse = -1;
							
							if (oldWord != null && !oldWord.equals(word)) {
								// Do a delete request to remove oldWord
								HttpURLConnection deleteConnection = (HttpURLConnection) (new URL(service + oldWord)).openConnection();
								deleteConnection.setRequestMethod("DELETE");
								deleteConnection.setReadTimeout(150);
								deleteConnection.setConnectTimeout(150);
								
								deleteResponse = deleteConnection.getResponseCode();
							}
							
							// Do a put request to create/edit word
							HttpURLConnection putConnection = (HttpURLConnection) (new URL(service + word)).openConnection();
							putConnection.setRequestMethod("PUT");
							putConnection.setDoOutput(true);
							putConnection.setReadTimeout(150);
							putConnection.setConnectTimeout(150);
							
							// Write form to service
							BufferedWriter out = new BufferedWriter(new OutputStreamWriter(putConnection.getOutputStream()));
							
							StringReader reader = new StringReader(StringEscapeUtils.escapeHtml(text));
							
							Txt2WikiXML parser = new Txt2WikiXML(reader);
							Document xmlDocument = parser.parseWiki();
							XmlUtils.outputDocument(xmlDocument, out);
							
							// Check response
							putResponse = putConnection.getResponseCode();
							
							boolean success;
							
							if (oldWord != null && !oldWord.equals(word)) {
								if (putResponse >= 200 && putResponse < 300 && deleteResponse >= 200 && deleteResponse < 300) {
									success = true;
								} else {
									success = false;
									if ((putResponse < 200 || putResponse >= 300) && (deleteResponse < 200 || deleteResponse >= 300)) {
										model.setText(text);
										model.setMessage("Bad put and deletion", "The word <i>" + word + "</i> could not be put to <i>" + wiki + "</i>, and the former word <i>" + oldWord + "</i> could not be deleted.", "");
									} else if (putResponse < 200 || putResponse >= 300) {
										model.setText(text);
										model.setMessage("Successful put, bad deletion", "The word <i>" + word + "</i> could not be put to <i>" + wiki + "</i>, but the former word <i>" + oldWord + "</i> was successfully deleted.", "");
									} else {
										model.setMessage("Successful deletion, bad put", "The word <i>" + word + "</i> was successfully put to <i>" + wiki + "</i>, but the former word <i>" + oldWord + "</i> could not be deleted. <a href=\"/Entry?wiki=" + wiki + "&amp;word=" + oldWord + "&amp;delete=yes\">Try again</a>.", "");
									}
								}
							} else {
								if (putResponse >= 200 && putResponse < 300) {
									success = true;
								} else {
									success = false;
									model.setText(text);
									model.setMessage("Bad put", "The word <i>" + word + "</i> could not be put to <i>" + wiki + "</i>.", "");
								}
							}
							
							if (success) {
								model.setMessage("Successful update", "<a href=\"Read?wiki=" + wiki + "&amp;word=" + word + "\">Go to word <i>" + word + "</i> on server <i>" + wiki + "</i></a>.", "");
								servletContext.getRequestDispatcher("/jsp/wiki_read.jsp").forward(request, response);
							} else {
								servletContext.getRequestDispatcher("/jsp/wiki_edit.jsp").forward(request, response);
							}
						}
					} catch (Exception e) {
						model.setText(text);
						List<WikiServer> servers = model.getServers();
						String message = "The server does not respond. Try choosing another server amongst the following:";
						String additional = "<p style=\"font-size: 12px; padding: 5px;\">";
						for (int i = 0; i < servers.size(); i++) {
							additional += "<i>" + servers.get(i).getName() + "</i>";
							if (i != servers.size()-1) {
								additional += ", ";
							}
						}
						model.setMessage("Server does not respond", message, additional);
						servletContext.getRequestDispatcher("/jsp/wiki_edit.jsp").forward(request, response);
					}
				} else {
					model.setText(text);
					model.setMessage("Server does not exist", "The word <i>" + word + "</i> could not be put to <i>" + wiki + ", since the server does not exist.", "");
					servletContext.getRequestDispatcher("/jsp/wiki_edit.jsp").forward(request, response);
				}
			} else {
				model.setText(text);
				model.setMessage("Word name required", "A word name is required.", "");
				servletContext.getRequestDispatcher("/jsp/wiki_edit.jsp").forward(request, response);
			}
		}
	}
}