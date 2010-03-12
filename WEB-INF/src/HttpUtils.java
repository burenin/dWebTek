import java.io.*;
import java.net.*;

public class HttpUtils {
	public static int getResponseCodeFromHttpRequest(String requestMethod, String service, String[] properties) throws MalformedURLException, IOException {
		HttpURLConnection connection = (HttpURLConnection) (new URL(service)).openConnection();
		connection.setRequestMethod(requestMethod);
		connection.setReadTimeout(150);
		connection.setConnectTimeout(150);
		for (int i = 0; i < properties.length; i = i + 2) {
			connection.setRequestProperty(properties[i], properties[i+1]);
		}
		return connection.getResponseCode();
	}
	
	public static HttpURLConnection getConnection(String requestMethod, String service, String[] properties) throws MalformedURLException, IOException {
		HttpURLConnection connection = (HttpURLConnection) (new URL(service)).openConnection();
		connection.setRequestMethod(requestMethod);
		connection.setConnectTimeout(150);
		connection.setReadTimeout(150);
		for (int i = 0; i < properties.length; i = i + 2) {
			connection.setRequestProperty(properties[i], properties[i+1]);
		}
		return connection;
	}
}