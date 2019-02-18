package conjur.jenkins;

public class CheckIfEmpty {
    
    private final String conjurURL;
    private final String username;
    private final String password;
    private String message;
    
    public CheckIfEmpty(String username, String password, String conjurURL){
        this.username = username;
        this.password = password;
        this.conjurURL = conjurURL;
    }
    
    public void setMessage(String message)
    {
        this.message = message;
    }
    
    public String getMessage()
    {
        return message;
    }
    
    public boolean checkIfEmpty() {
        StringBuilder message = new StringBuilder();
        boolean errorDetected = false;
        if(username.isEmpty())
        {
            message.append("username");
            errorDetected = true;
        }
        
        if(password.isEmpty())
        {
           if(message.length() > 0)
           {
               message.append(", ");
           } 
           message.append("password");
           errorDetected = true;
        }
        
        if(conjurURL.isEmpty())
        {
            if(message.length() > 0)
            {
                message.append(", ");
            }
            message.append("conjur host");
            errorDetected = true;
        }
        if(errorDetected)
        {
            message.append(" field(s) are empty!");
            setMessage(message.toString());
        }
        return errorDetected;
        
    }
}
