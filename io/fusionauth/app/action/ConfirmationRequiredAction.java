package io.fusionauth.app.action;

import com.google.inject.Inject;
import io.fusionauth.api.domain.ConfirmationRequiredReason;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.annotation.AllowUnknownParameters;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
@Redirect(code = "success", uri = "${returnToPreviousActionURI}")
@AllowUnknownParameters
public class ConfirmationRequiredAction extends BaseThemedAction {
  @FTLVariable
  public ConfirmationRequiredReason confirmationRequiredReason;
  
  public String reason;
  
  public String returnToPreviousActionURI;
  
  @Inject
  public ConfirmationRequiredAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramSSOService, paramThreatDetectionService);
  }
  
  public String get() {
    this.csrfToken = this.frontEndSupport.encrypt(this.reason);
    this.csrfTokenCookie.value = this.csrfToken;
    return "input";
  }
  
  public String post() {
    allowConfirmationBypass();
    this.returnToPreviousActionURI = this.frontEndSupport.request.getParameter("url");
    this.csrfTokenCookie.value = null;
    return "success";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    if (this.reason == null)
      throw new ErrorException("unauthorized"); 
    String str = this.frontEndSupport.request.getParameter("url");
    if (str == null || !str.startsWith("/") || str.startsWith("//") || str.contains("\\") || str.contains("://"))
      throw new ErrorException("unauthorized"); 
    try {
      this.confirmationRequiredReason = ConfirmationRequiredReason.valueOf(this.reason);
    } catch (Exception exception) {
      throw new ErrorException("unauthorized");
    } 
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.csrfTokenCookie.value == null || !this.csrfTokenCookie.value.equals(this.csrfToken)) {
      addGeneralError("[InvalidOrMissingCSRFToken]", new Object[0]);
      throw new ErrorException("input");
    } 
  }
}
