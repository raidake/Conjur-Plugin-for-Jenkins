package conjur.jenkins;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.EnvironmentContributingAction;
import hudson.model.InvisibleAction;


public class EnvInject extends InvisibleAction implements EnvironmentContributingAction {

    private String variable;
    private String resource;
    public EnvInject(String variable, String resource) {
        this.variable = variable;
        this.resource = resource;
    }
    
    public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
        env.put(variable, resource);
    }

}