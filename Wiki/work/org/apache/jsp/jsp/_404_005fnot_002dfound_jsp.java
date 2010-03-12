package org.apache.jsp.jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

public final class _404_005fnot_002dfound_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent {

  private static final JspFactory _jspxFactory = JspFactory.getDefaultFactory();

  private static java.util.List _jspx_dependants;

  static {
    _jspx_dependants = new java.util.ArrayList(2);
    _jspx_dependants.add("/jsp/includes/header.jsp");
    _jspx_dependants.add("/jsp/includes/footer.jsp");
  }

  private javax.el.ExpressionFactory _el_expressionfactory;
  private org.apache.AnnotationProcessor _jsp_annotationprocessor;

  public Object getDependants() {
    return _jspx_dependants;
  }

  public void _jspInit() {
    _el_expressionfactory = _jspxFactory.getJspApplicationContext(getServletConfig().getServletContext()).getExpressionFactory();
    _jsp_annotationprocessor = (org.apache.AnnotationProcessor) getServletConfig().getServletContext().getAttribute(org.apache.AnnotationProcessor.class.getName());
  }

  public void _jspDestroy() {
  }

  public void _jspService(HttpServletRequest request, HttpServletResponse response)
        throws java.io.IOException, ServletException {

    PageContext pageContext = null;
    HttpSession session = null;
    ServletContext application = null;
    ServletConfig config = null;
    JspWriter out = null;
    Object page = this;
    JspWriter _jspx_out = null;
    PageContext _jspx_page_context = null;


    try {
      response.setContentType("text/html; charset=UTF-8");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;

      out.write("\r\n");
      out.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\r\n");
      out.write("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\r\n");
      out.write("\t<head>\r\n");
      out.write("\t\t<title>404 Not Found</title>\r\n");
      out.write("\t\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\r\n");
      out.write("\t\t<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"jsp/css/stylesheet.css\" />\r\n");
      out.write("\t</head>\r\n");
      out.write("\t<body>\r\n");
      out.write("\t\t<div class=\"pageContainer\">\r\n");
      out.write("\t\t\t");
      out.write("\r\n");
      out.write("\t\t\t<div class=\"header\">\r\n");
      out.write("\t\t\t\t<div class=\"left\">\r\n");
      out.write("\t\t\t\t\t<h1>Wiki</h1>\r\n");
      out.write("\t\t\t\t</div>\r\n");
      out.write("\t\t\t\t<div class=\"right\">\r\n");
      out.write("\t\t\t\t\t<a href=\"/Wiki/Entry\">List all internal pages</a> / <a href=\"/Wiki/Entry?wiki=\">List all pages</a>\r\n");
      out.write("\t\t\t\t\t<form name=\"search\" method=\"get\" action=\"/Wiki/Entry\" onsubmit=\"setAction(this)\">\r\n");
      out.write("\t\t\t\t\t\t<input type=\"text\" name=\"pattern\" value=\"Search...\" onclick=\"if(this.value=='Search...')this.value='';\" />\r\n");
      out.write("\t\t\t\t\t\t<input class=\"submit\" type=\"submit\" value=\"Search\" /><br />\r\n");
      out.write("\t\t\t\t\t\t<input class=\"checkbox\" type=\"checkbox\" name=\"luck\" /><label>I am feeling lucky</label>\r\n");
      out.write("\t\t\t\t\t</form>\r\n");
      out.write("\t\t\t\t</div>\r\n");
      out.write("\t\t\t\t<div class=\"floatbreaker\"></div>\r\n");
      out.write("\t\t\t</div>");
      out.write("\r\n");
      out.write("\t\t\t<div class=\"main\">\r\n");
      out.write("\t\t\t\t<h2>404 Not Found:</h2>\r\n");
      out.write("\t\t\t\t<p>The requested page does not exist.</p>\r\n");
      out.write("\t\t\t</div>\r\n");
      out.write("\t\t\t");
      out.write("\r\n");
      out.write("\t\t\t<div class=\"footer\">\r\n");
      out.write("\t\t\t\t<p>Jens Olaf Svanholm Fogh, Troels Leth Jensen, Jesper Lindstrøm Nielsen, Christoffer Quist Adamsen</p>\r\n");
      out.write("\t\t\t</div>");
      out.write("\r\n");
      out.write("\t\t</div>\r\n");
      out.write("\t</body>\r\n");
      out.write("</html>");
    } catch (Throwable t) {
      if (!(t instanceof SkipPageException)){
        out = _jspx_out;
        if (out != null && out.getBufferSize() != 0)
          try { out.clearBuffer(); } catch (java.io.IOException e) {}
        if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
      }
    } finally {
      _jspxFactory.releasePageContext(_jspx_page_context);
    }
  }
}
