import java.util.*;
import org.apache.commons.lang.*;

public class WikiWord {
	private String name;
	private String server;
	
	public WikiWord(String name, String server) {
		this.name = name;
		this.server = server;
	}
	
	public String getName() {
		return StringEscapeUtils.escapeHtml(name);
	}
	
	public String getServer() {
		return StringEscapeUtils.escapeHtml(server);
	}
	
	public static Comparator<WikiWord> getNameComparator() {
		return new
			Comparator<WikiWord>() {
				public int compare(WikiWord object, WikiWord otherObject) {
					if (object.server.equals(otherObject.server)) {
						return object.name.compareTo(otherObject.name);
					} else if (object.server.equals("jtjcWiki")){
						return -1;
					} else if (otherObject.server.equals("jtjcWiki")) {
						return 1;
					} else {
						return object.server.compareTo(otherObject.server);
					}
				}
			};
	}
}