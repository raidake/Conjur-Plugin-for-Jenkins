package conjur.jenkins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class ConjurSecret {
    private final String loginToken;
    private final String conjurURL;
    
    public ConjurSecret(String loginToken, String conjurURL)
    {
        this.loginToken = loginToken;
        this.conjurURL = conjurURL;
    }
    
    public String getSecrets(String resource)
    {
        try {

                String APIurl = conjurURL + "/api/variables/" + resource.replaceAll("/","%2F") + "/value?";
                URL url = new URL(APIurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                
                String token = loginToken;
		conn.setRequestMethod("GET");   
		conn.setRequestProperty("Authorization", "Token token=\"" + token + "\"" );

		if (conn.getResponseCode() != 200) {
			return Integer.toString(conn.getResponseCode());
                }

		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

		String output;
		while ((output = br.readLine()) != null) {
			conn.disconnect();
                        return output;
		}
                
		
	  } catch (MalformedURLException e) {

		return e.getMessage();

	  } catch (IOException e) {

		return e.getMessage();
          }
        return "secret";
    }
}
