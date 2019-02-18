package conjur.jenkins;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.console.ConsoleLogFilter;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.FormValidation;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLHandshakeException;
import jenkins.tasks.SimpleBuildWrapper;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class ConjurWrapper extends SimpleBuildWrapper{
    
    private final String username;
    private final String password;
    private final String conjurURL;
    private final List<ResourceVariables> resourceStacks;
    private final List<HostVariable> hostStacks;
    public static List<String> allPasswords = new ArrayList<String>();
    private boolean ignoreSSL = false;
    public static final String ENVINJECT_BUILDER_ACTION_NAME = "EnvInjectBuilderaction";
    static List<String> allSecrets = new ArrayList<>();
    
    
    @DataBoundConstructor
    public ConjurWrapper(String username, String password, String conjurURL, List<ResourceVariables> resourceStacks, List<HostVariable> hostStacks, boolean ignoreSSL) {
        this.username = username;
        this.password = password;
        this.conjurURL = conjurURL;
        this.resourceStacks =  resourceStacks;
        this.hostStacks = hostStacks;
        this.ignoreSSL = ignoreSSL;
    }
    
    public List<ResourceVariables> getResourceStacks()
    {
        return resourceStacks;
    }
    
    public List<HostVariable> getHostStacks()
    {
        return hostStacks;
    }

    @Override
    public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {
        
        boolean error = true;
        CheckIfEmpty checker = new CheckIfEmpty(username,password,conjurURL);
        
        if(checker.checkIfEmpty())
        {
            listener.error(checker.getMessage());
            build.setResult(Result.FAILURE);
        }
        else{
            ConjurToken token = new ConjurToken(username,password,conjurURL);
            if(ignoreSSL){
                try {
                    token.ignoreSSL();
                } catch (Exception ex) {
                    Logger.getLogger(ConjurWrapper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if(token.getAPIKey().equals("No Trust"))
            {
                listener.error("The Certificate from the Conjur host is not accepted as it is not issued by a trusted Certificate Authority or the certificate is not valid.");
                build.setResult(Result.FAILURE);    
            } 
            else if (token.getAPIKey().equals("Unknown Host"))
            {
                listener.error("Conjur Host does not exist.");
                build.setResult(Result.FAILURE);
            }
            else
            {
                if(token.getAPIKey().equals("401"))
                {
                    listener.error("Username/password wrong.");
                    build.setResult(Result.FAILURE);
                }
                else if (token.getAPIKey().equals("404"))
                {
                    listener.error("Page not found or invalid URL.");
                    build.setResult(Result.FAILURE);
                }
                else if (token.getAPIKey().equals("408"))
                {
                    listener.error("Request timed out.");
                    build.setResult(Result.FAILURE);
                }
                else
                { 
                    if(resourceStacks != null)
                    {
                        ConjurSecret retriever = new ConjurSecret(token.getToken(),token.getConjurURL());
                        int counter = 0;
                        for (ResourceVariables resVar : resourceStacks) {

                            counter++;

                            if(resVar.getResource().isEmpty() && resVar.getVariable().isEmpty())
                            {
                                listener.error("Entry #" + counter + " is empty");
                            }
                            else if (resVar.getResource().isEmpty())
                            {
                                listener.error("The resource of " + resVar.getVariable() + " is empty!");
                            }
                            else if(resVar.getVariable().isEmpty())
                            {
                                listener.error("The environment variable of " + resVar.getResource() + " is empty!");
                            }
                            else
                            {
                                String output = retriever.getSecrets(resVar.getResource());
                                if (output.equals("403"))
                                {
                                    listener.error("This account lacks the necessary privilege for " + resVar.getResource() + ".");
                                }
                                else if(output.equals("404"))
                                {
                                    listener.error(resVar.getResource() + " does not exist, or it does not have any secret values");
                                }
                                else
                                {
                                    build.addAction(new EnvInject(resVar.getVariable(),output));
                                    allSecrets.add(output);
                                    error = false;
                                }
                            }

                        }
                    }
                    
                    if(hostStacks != null)
                    {
                        int counter = 0;
                        ConjurHost retriever = new ConjurHost(token.getToken(),token.getConjurURL());
                        
                        for(HostVariable hostVar : hostStacks)
                        {
                            counter++;
                            
                            if(hostVar.getHostFactory().isEmpty() && hostVar.getVariable().isEmpty())
                            {
                                listener.error("Entry #" + counter + " is empty");
                            }
                            else if(hostVar.getHostFactory().isEmpty())
                            {
                                listener.error("The Host Factory of " + hostVar.getVariable() + " is empty!");
                            }
                            else if (hostVar.getVariable().isEmpty())
                            {
                                listener.error("The environment variable of " + hostVar.getHostFactory() + " is empty!");
                            }
                            else
                            {
                                String output = retriever.createHostToken(hostVar);
                                if(output.equals("404"))
                                {
                                    listener.error("Host Factory not found for variable " + hostVar.getVariable());
                                }
                                else if(output.equals("403"))
                                {
                                    listener.error("Permission Denied for Host Factory " + hostVar.getHostFactory());
                                }
                                else
                                {
                                    build.addAction(new EnvInject(hostVar.getVariable(),output));
                                    allSecrets.add(output);
                                    error = false;
                                }
                                
                            }
                            
                        }
                        
                        
                    }
                       
                    if(error)
                    {
                        listener.error("Build have failed to retrieve secrets.");
                        build.setResult(Result.FAILURE);
                    }
                }
            }
        }
    }
    
    
    @Override
    public ConsoleLogFilter createLoggerDecorator(Run<?, ?> build) {
        return new MaskOutputStream(build.getCharset().name(), allSecrets);
    }
        
    
    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

        @Override
        public boolean isApplicable(AbstractProject<?, ?> ap) {
            return true;
        }
        
        @Override
        public String getDisplayName() {
            return Messages.ConjurWrapper_DescriptorImpl_DisplayName();
        }
        
        public FormValidation doTestConnection(@QueryParameter("conjurURL") String url) throws MalformedURLException, ProtocolException, IOException {
        
            if(!url.matches("^(https)://.*$"))
            {
                url = "https://" + url;
            }
            
            try {
                URL connect = new URL(url);
                HttpURLConnection urlConn = (HttpURLConnection) connect.openConnection();
                urlConn.connect();
            
                if (urlConn.getResponseCode() == 404)
                {
                    return FormValidation.error("Page not found or invalid URL. Connection Failed!");
                }
                else if (urlConn.getResponseCode() == 408)
                {
                    return FormValidation.error("Request timed out. Connection Failed!");
                }   
                else
                {
                    return FormValidation.ok("Connection Successful!");
                }
            }
            catch (MalformedURLException e) {
                return FormValidation.error("Connection Failed!");
            }
            catch (SSLHandshakeException e) {
                return FormValidation.error("Certificate is either not trusted or invalid");
            }
            catch (IOException e){
                return FormValidation.error("Connection Failed!");
            }
        } 
    }
     
}