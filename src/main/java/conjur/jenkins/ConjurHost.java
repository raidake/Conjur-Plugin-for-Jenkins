/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package conjur.jenkins;

import com.google.gson.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONArray;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author wsim
 */
public class ConjurHost {
    private final String loginToken;
    private final String conjurURL;
    
    
    @DataBoundConstructor
    public ConjurHost(String loginToken, String conjurURL){
        this.loginToken = loginToken;
        this.conjurURL = conjurURL;
    }
    
    
    public String createHostToken(HostVariable entry)
    {
        try {
            
                String APIurl = conjurURL + "/api/host_factories/" + entry.getHostFactory() + "/tokens?expiration=" + 
                        entry.getExpiry().replaceAll(":","%3A");
                
                if(entry.getRange() != null)
                {
                    APIurl += "&cidr%5B%5D=" + entry.getRange().replaceAll("/","%3F");
                }
                URL url = new URL(APIurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                
                String token = loginToken;
                
                conn.setRequestMethod("POST");
		conn.setRequestProperty("Authorization", "Token token=\"" + token + "\"");
                
                if (conn.getResponseCode() != 200) {
			return Integer.toString(conn.getResponseCode());
                }
                
		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                JsonParser parser = new JsonParser();
                
		String output;
                String result = "";
		while ((output = br.readLine()) != null) {
                    
                    JsonArray jsonArray = parser.parse(output).getAsJsonArray();
                    
                    for (JsonElement js : jsonArray)
                    {
                        JsonObject jsonObject = js.getAsJsonObject();
                        result = jsonObject.get("token").getAsString();
                    }
                    conn.disconnect();
                    return result;
		}

                
                
                
        }catch (MalformedURLException e) {

		return e.getMessage();

	} catch (IOException e) {

		return e.getMessage();
        }
        
        
        return "";
    }
    
    
}
