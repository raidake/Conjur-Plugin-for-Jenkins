package conjur.jenkins;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

public class ResourceVariables extends AbstractDescribableImpl<ResourceVariables>{
    
    private String resource;
    private String variable;
    
    @DataBoundConstructor
    public ResourceVariables(String resource, String variable){
        super();
        this.resource = resource;
        this.variable = variable;
    }
    
    public String getResource(){
        return resource;
    }
    
    public String getVariable(){
        return variable;
    }
    

    @Extension  
    public static final class DescriptorImpl extends Descriptor<ResourceVariables> {
        
        @Override
        public String getDisplayName() {
		return "Inject Environment Variables";
	}
    }
}

