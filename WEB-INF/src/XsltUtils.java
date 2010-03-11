import org.jdom.*;
import org.jdom.transform.*;

public class XsltUtils {
	public synchronized static Document transform(Document document, String xslName) {
		try {
			System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
			XSLTransformer transformer = new XSLTransformer(xslName);
			return transformer.transform(document);
		} catch (Exception e) {
			return null;
		}
	}
}