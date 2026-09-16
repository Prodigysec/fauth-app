package io.fusionauth.app.action.admin;

import com.google.inject.Inject;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.http.server.HTTPRequest;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.primeframework.mvc.action.annotation.Action;

@Action
public class ProxyConfigTestAction extends BaseAction {
  public ProxyConfigReport report;
  
  @Inject
  public ProxyConfigTestAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "success";
  }
  
  public String post() {
    this.report = new ProxyConfigReport(this.frontEndSupport.request);
    return "success";
  }
  
  public static class ProxyConfigReport {
    public Map<String, String> actualHeaders = new LinkedHashMap<>();
    
    public String actualOrigin;
    
    public String expectedOrigin;
    
    public boolean failed;
    
    public Map<String, String> requiredHeaders = new LinkedHashMap<>();
    
    public ProxyConfigReport(HTTPRequest param1HTTPRequest) {
      String str = param1HTTPRequest.getHeader("Origin");
      if (str == null)
        str = param1HTTPRequest.getHeader("Referer"); 
      if (str == null)
        return; 
      URI uRI1 = URI.create(str);
      this.expectedOrigin = uRI1.getScheme().toLowerCase() + "://" + uRI1.getScheme().toLowerCase();
      if (uRI1.getScheme().equalsIgnoreCase("https") && uRI1.getPort() != 443 && uRI1.getPort() != -1) {
        this.expectedOrigin = this.expectedOrigin + ":" + this.expectedOrigin;
      } else if (uRI1.getScheme().equalsIgnoreCase("http") && uRI1.getPort() != 80 && uRI1.getPort() != -1) {
        this.expectedOrigin = this.expectedOrigin + ":" + this.expectedOrigin;
      } 
      URI uRI2 = URI.create(param1HTTPRequest.getBaseURL());
      this.actualOrigin = uRI2.toString();
      this.failed = !this.expectedOrigin.equals(this.actualOrigin);
      if (this.failed) {
        if (!uRI2.getScheme().equalsIgnoreCase(uRI1.getScheme()))
          this.requiredHeaders.put("X-Forwarded-Proto", uRI1.getScheme().toLowerCase()); 
        if (!uRI2.getHost().equalsIgnoreCase(uRI1.getHost()))
          this.requiredHeaders.put("X-Forwarded-Host", uRI1.getHost().toLowerCase()); 
        if (uRI2.getPort() != uRI1.getPort()) {
          int i = uRI1.getPort();
          if (i == -1)
            i = uRI1.getScheme().equalsIgnoreCase("https") ? 443 : 80; 
          if (i != uRI2.getPort())
            this.requiredHeaders.put("X-Forwarded-Port", String.valueOf(i)); 
        } 
        String str1 = param1HTTPRequest.getHeader("X-Forwarded-Proto");
        if (str1 != null)
          this.actualHeaders.put("X-Forwarded-Proto", str1); 
        String str2 = param1HTTPRequest.getHeader("X-Forwarded-Host");
        if (str2 != null)
          this.actualHeaders.put("X-Forwarded-Host", str2); 
        String str3 = param1HTTPRequest.getHeader("X-Forwarded-Port");
        if (str3 != null)
          this.actualHeaders.put("X-Forwarded-Port", str3); 
      } 
    }
  }
}
