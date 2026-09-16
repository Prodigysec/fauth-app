package io.fusionauth.app.action;

import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import java.util.List;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.util.QueryStringBuilder;

@Redirect(code = "return", uri = "${redirectURI}")
public abstract class BaseSentAction extends BaseThemedAction {
  public String redirectURI;
  
  protected BaseSentAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramSSOService, paramThreatDetectionService);
  }
  
  protected String checkLoginId(String paramString) {
    if (paramString == null) {
      QueryStringBuilder queryStringBuilder = QueryStringBuilder.builder(restartURL());
      this.frontEndSupport.request.getParameters()
        .forEach((paramString, paramList) -> paramList.forEach(()));
      this.redirectURI = queryStringBuilder.build();
      return "return";
    } 
    return "input";
  }
  
  protected abstract String restartURL();
}
