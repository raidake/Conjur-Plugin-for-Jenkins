package conjur.jenkins;

import java.util.Base64;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLHandshakeException;

import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

class ConjurToken {

    private final String conjurURL;
    private final String username;
    private final String password;
    public ConjurToken(String username, String password, String conjurURL){
        this.username = username;
        this.password = password;
        
        if(conjurURL.matches("^(https)://.*$"))
        {
            this.conjurURL = conjurURL;
        }
        else
        {
            this.conjurURL = "https://" + conjurURL;
        }
    }
    
    public String getUsername(){
        return username;
    }
    
    public String getPassword(){
        return password;
    }
    
    public String getBase64(String value) {
        
        byte[] encodedBytes = Base64.getEncoder().encode(value.getBytes());
        return new String(encodedBytes);
    }
    
    public String getConjurURL(){
        return conjurURL;
    }
    
    public String getAPIKey() throws MalformedURLException, ProtocolException, IOException
    {
        try { 
            String APIKey;
            String APIurl = getConjurURL() + "/api/authn/users/login";
            URL url = new URL(APIurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("GET");
           
            String base64 = getUsername() + ":" + getPassword();
            conn.setRequestProperty("Authorization", "Basic " + getBase64(base64));
            
            if (conn.getResponseCode() != 200) {
			return Integer.toString(conn.getResponseCode());
            }
            
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            
            if ((APIKey = br.readLine()) != null) {
                conn.disconnect();
                return APIKey;
            }  
            
        } catch (UnknownHostException e) {
            
            return "Unknown Host";
            
        } catch (MalformedURLException e) {
            
            return e.getMessage();
            
        } catch (SSLHandshakeException e) {
            
            return "No Trust";
            
        } catch (IOException e) {
            
            return e.getMessage();
            
        } 
        return "apikey"; 
    }
    
    public String getToken()
    {
        try {
                String APIurl = getConjurURL() + "/api/authn/users/" + getUsername() + "/authenticate";
                URL url = new URL(APIurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "text/plain");

                String input = getAPIKey();
		OutputStream os = conn.getOutputStream();
                os.write(input.getBytes());
                os.close();

                
		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

		String output;
		while ((output = br.readLine()) != null) {
			conn.disconnect();
                        return getBase64(output);
		}
                
	  } catch (MalformedURLException e) {

		return e.getMessage();

	  } catch (IOException e) {

		return e.getMessage();

	 }
        return "token";
    }
    
    public void ignoreSSL() throws KeyManagementException, NoSuchAlgorithmException
    {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };
        
        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        
        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
 
}
    
    
 

    

