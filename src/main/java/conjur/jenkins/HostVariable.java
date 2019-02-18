package conjur.jenkins;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;
import java.util.Calendar;
import org.kohsuke.stapler.DataBoundConstructor;

public class HostVariable extends AbstractDescribableImpl<HostVariable>{
    
    private String hostFactory;
    private String variable;
    private String date;
    private String year;
    private String month;
    private String day;
    private String hour;
    private String minute;
    private String second;
    private String cidr;
    private String timeZone;
    

    
    @DataBoundConstructor
    public HostVariable(String hostFactory, String variable, String date, String year, String month, String day, String hour, String minute, String second, String timeZone){
        super();
        this.hostFactory = hostFactory;
        this.variable = variable;
        this.date = date;
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.timeZone = timeZone;
    }

    
    public String getHostFactory(){
        return hostFactory;
    }
    
    public String getVariable(){
        return variable;
    }
    
    public String getExpiry(){
        String expiry = year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":" + second + timeZone;
        return expiry;
    }
    
    public String getRange(){
        return cidr;
    }
    

    @Extension  
    public static final class DescriptorImpl extends Descriptor<HostVariable> {
        
        @Override
        public String getDisplayName() {
		return "Inject Host Token to Environment Variables";
	}
        public ListBoxModel doFillYearItems() {
            ListBoxModel model = new ListBoxModel();
            int now = Calendar.getInstance().get(Calendar.YEAR);
            for (int i = now; i < now + 100; i++) {
                model.add(Integer.toString(i));
            }
            return model;
        }   
    
        public ListBoxModel doFillMonthItems() {
            ListBoxModel model = new ListBoxModel();

            for (int i = 1; i <= 12; i++) {
                model.add(Integer.toString(i));
            }
            return model;
        }
    
        public ListBoxModel doFillDayItems() {
            ListBoxModel model = new ListBoxModel();
            for (int i = 1; i <= 31; i++) {
                model.add(Integer.toString(i));
            }
            return model;
        }
        
        public ListBoxModel doFillHourItems() {
            ListBoxModel model = new ListBoxModel();
            for (int i = 0; i <= 23; i++) {
                String formatted = String.format("%02d", i);
                model.add(formatted);
            }
            return model;
        }
        
        public ListBoxModel doFillMinuteItems() {
            ListBoxModel model = new ListBoxModel();
            for (int i = 0; i <= 59; i++) {
                String formatted = String.format("%02d", i);
                model.add(formatted);
            }
            return model;
        }
        
        public ListBoxModel doFillSecondItems() {
            ListBoxModel model = new ListBoxModel();
            for (int i = 0; i <= 59; i++) {
                String formatted = String.format("%02d", i);
                model.add(formatted);
            }
            return model;
        }
        
        public ListBoxModel doFillTimeZoneItems() {
            ListBoxModel model = new ListBoxModel();
            for (int i = 0; i <= 12; i++) {
                String formatted = String.format("%02d", i);
                formatted = "-" + formatted;
                
                for (int f = 0; f <= 45; f+=15) {
                    String output = formatted + ":" + String.format("%02d", f);
                    model.add(output);
                } 

            }
            
            for (int i = 0; i <= 14; i++) {
                String formatted = String.format("%02d", i);
                formatted = "+" + formatted;
                
                for (int f = 0; f <= 45; f+=15) {
                    String output = formatted + ":" + String.format("%02d", f);
                    model.add(output);
                }
            } 
            return model;
        }
    }
}
