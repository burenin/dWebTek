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
		String wiki = request.getParameter("wiki");
		String pattern = request.getParameter("pattern");
		model.setSelectedWord(word);
		model.setSelectedWiki(wiki);
		model.setPattern(pattern);
				
		// Handle request		
		if (!command.equals("/Entry")) {
			if (command.equals("/Read") && word != null && !word.equals("")) {
				// READ!
				// Check for special version request
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

				// Set default server
				if (wiki == null || wiki.equals("")) {
					model.setSelectedWiki(model.getDefaultWiki());
				}
				servletContext.getRequestDispatcher("/jsp/wiki_read.jsp").forward(request, response);
				
			} else if (command.equals("/Edit") && wiki != null && !wiki.equals("")) {
				// EDIT!
				servletContext.getRequestDispatcher("/jsp/wiki_edit.jsp").forward(request, response);
				
			} else if (command.equals("/Luck")) {
				// LUCK!
				servletContext.getRequestDispatcher("/jsp/wiki_read.jsp").forward(request, response);
				
			} else {
				command = "/Entry";
			}
		}
		
		if (command.equals("/Entry")) {			
			// ENTRY!
			String delete = request.getParameter("delete");
			if (delete == null) {
				// Usual entry
				model.setVersion(-1);
				servletContext.getRequestDispatcher("/jsp/wiki_entry.jsp").forward(request, response);
				
			} else if (delete != null && delete.equals("yes") && wiki != null && !wiki.equals("") && word != null && !word.equals("")) {
				// Send delete request
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
						int deleteResponseCode = HttpUtils.getResponseCodeFromHttpRequest("DELETE", service, new String[0]);
						if (deleteResponseCode >= 200 && deleteResponseCode < 300) {
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
								// Create request, check for already exist
								int createResponseCode = HttpUtils.getResponseCodeFromHttpRequest("GET", service + word, new String[0]);
								if (createResponseCode != 404) {
									modified = true;
									model.setSelectedWord(word);
									model.setSelectedWiki(wiki);
									model.setText(text);
									model.setConflict(true);
									model.setMessage("Conflict detected", "A conflict was detected. The word you are trying to create, has already been put.", "");
									servletContext.getRequestDispatcher("/jsp/wiki_edit.jsp").forward(request, response);
								}
							} else if (oldETag != null && !oldETag.equals("")) {
								// Edit response, check for new version since edit
								String[] properties = new String[2];
								properties[0] = "If-None-Match";
								properties[1] = oldETag;
								int editResponseCode = HttpUtils.getResponseCodeFromHttpRequest("GET", service + word, properties);
								if (editResponseCode != 304) {
									modified = true;
									model.setSelectedWord(word);
									model.setSelectedWiki(wiki);
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
								deleteResponse = HttpUtils.getResponseCodeFromHttpRequest("DELETE", service + oldWord, new String[0]);
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
								// Edit with word-name change!
								if (putResponse >= 200 && putResponse < 300 && deleteResponse >= 200 && deleteResponse < 300) {
									// All ok
									success = true;
								} else {
									// Something went wrong
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
								// Create or edit
								if (putResponse >= 200 && putResponse < 300) {
									// All ok
									success = true;
								} else {
									// Bad put
									success = false;
									model.setText(text);
									model.setMessage("Bad put", "The word <i>" + word + "</i> could not be put to <i>" + wiki + "</i>.", "");
								}
							}
							
							if (success) {
								model.setMessage("Successful update", "<a href=\"Read?wiki=" + wiki + "&amp;word=" + word + "\">Go to word <i>" + word + "</i> on server <i>" + wiki + "</i></a>.", "");
								servletContext.getRequestDispatcher("/jsp/wiki_read.jsp").forward(request, response);
							} else {
								// Show form with message
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