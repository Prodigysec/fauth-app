package io.fusionauth.app.action.samlv2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inversoft.util.StringTools;
import io.fusionauth.api.security.KeyCache;
import io.fusionauth.api.service.samlv2.SAMLv2ProviderService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.app.action.BaseThemedAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.samlv2.domain.AuthenticationRequest;
import io.fusionauth.samlv2.domain.Binding;
import io.fusionauth.samlv2.domain.LogoutRequest;
import io.fusionauth.samlv2.domain.NameIDFormat;
import io.fusionauth.samlv2.domain.SAMLException;
import io.fusionauth.samlv2.domain.SAMLRequest;
import io.fusionauth.samlv2.domain.SignatureNotFoundException;
import java.util.Map;
import org.primeframework.mvc.action.result.annotation.Redirect;

@Redirect(code = "redirect-to-reply", uri = "${logoutURI}")
public abstract class BaseSAMLAction extends BaseThemedAction {
  protected final KeyCache keyCache;
  
  protected final SAMLv2ProviderService samlv2ProviderService;
  
  public String RelayState;
  
  public String SAMLRequest;
  
  public String SAMLResponse;
  
  public String SigAlg;
  
  public String Signature;
  
  public String localTenantId;
  
  public String logoutURI;
  
  protected Binding binding;
  
  protected BaseSAMLAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, KeyCache paramKeyCache, LoginIntentService paramLoginIntentService, SAMLv2ProviderService paramSAMLv2ProviderService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramSSOService, paramThreatDetectionService);
    this.keyCache = paramKeyCache;
    this.samlv2ProviderService = paramSAMLv2ProviderService;
  }
  
  protected String buildSAMLLogoutResponse(SAMLv2ProviderService.FusionAuthLogoutResponse paramFusionAuthLogoutResponse) throws SAMLException {
    this
      
      .SAMLResponse = (this.binding == Binding.HTTP_POST) ? this.samlv2ProviderService.buildPostLogoutResponse(paramFusionAuthLogoutResponse) : this.samlv2ProviderService.buildRedirectLogoutResponse(paramFusionAuthLogoutResponse, this.RelayState);
    this.samlv2ProviderService.logSAMLResponse(paramFusionAuthLogoutResponse.application.samlv2Configuration.debug, this.binding, "LogoutResponse", (SAMLRequest)paramFusionAuthLogoutResponse, this.SAMLResponse);
    if (this.binding == Binding.HTTP_Redirect) {
      this.logoutURI = this.logoutURI + "?" + this.logoutURI;
      return "redirect-to-reply";
    } 
    return "post-to-reply";
  }
  
  protected String commonSAMLValidation() {
    if (this.localTenantId == null) {
      this.oauthJSONError = this.frontEndSupport.writeToPrettyString(Map.of("message", isLogin() ? 
            "The login URL does not contain the tenant Id. Ensure your URL includes the tenantId like this: /samlv2/login/{tenantId}." : 
            "The logout URL does not contain the tenant Id. Ensure your URL includes the tenantId like this: /samlv2/logout/{tenantId}."));
      return "render-error";
    } 
    if (this.SAMLRequest == null) {
      this.oauthJSONError = this.frontEndSupport.writeToPrettyString(Map.of("message", isLogin() ? 
            "The SAML v2 login request is missing the SAMLRequest parameter." : 
            "The SAML v2 logout request is missing the SAMLRequest parameter."));
      return "render-error";
    } 
    if (this.frontEndSupport.isGET()) {
      String str = this.samlv2ProviderService.validateRedirectBinding(this.SigAlg, this.Signature);
      if (str != null) {
        this.oauthJSONError = this.frontEndSupport.writeToPrettyString(Map.of("message", str));
        return "render-error";
      } 
    } 
    return null;
  }
  
  protected String handleSAMLException(SAMLCallable paramSAMLCallable) {
    try {
      return paramSAMLCallable.call();
    } catch (JsonProcessingException|SAMLException jsonProcessingException) {
      String str1 = isLogin() ? "AuthnRequest" : "LogoutRequest";
      String str2 = (jsonProcessingException instanceof SignatureNotFoundException) ? ("Unable to complete the SAML v2 " + str1 + " request because it is missing a required signature.") : ("FusionAuth encountered an exception while processing the SAML v2 " + str1 + ". Consult the logs for details.");
      this.oauthJSONError = this.frontEndSupport.writeToPrettyString(Map.of("message", str2));
      if (jsonProcessingException instanceof SignatureNotFoundException) {
        SignatureNotFoundException signatureNotFoundException = (SignatureNotFoundException)jsonProcessingException;
        Application application = (signatureNotFoundException.request != null) ? this.samlv2ProviderService.resolveApplication(this.tenantId, signatureNotFoundException.request.issuer) : null;
        if (application != null && application.samlv2Configuration.debug)
          EventLogHelper.create(new EventLog(EventLogType.Debug, str2 + "\nYour current configuration requires the " + str2 + " to include a signature but a signature was not provided.\nThe request originated from: " + str1 + ".\n\nSAMLRequest: " + this.frontEndSupport
                
                .getOrigin())); 
      } else {
        EventLogHelper.create(new EventLog(EventLogType.Debug, "FusionAuth encountered an exception while processing the SAML v2 " + str1 + ".\nThe request originated from: " + this.frontEndSupport
              .getOrigin() + ".\n\nSAMLRequest: " + this.SAMLRequest, (Throwable)jsonProcessingException));
      } 
      return "render-error";
    } 
  }
  
  protected void normalize(SAMLRequest paramSAMLRequest) {
    if (paramSAMLRequest instanceof LogoutRequest) {
      LogoutRequest logoutRequest = (LogoutRequest)paramSAMLRequest;
      logoutRequest.nameIdFormat = StringTools.defaultIfNull(logoutRequest.nameIdFormat, NameIDFormat.EmailAddress.toSAMLFormat());
    } else if (paramSAMLRequest instanceof AuthenticationRequest) {
      AuthenticationRequest authenticationRequest = (AuthenticationRequest)paramSAMLRequest;
      authenticationRequest.nameIdFormat = StringTools.defaultIfNull(authenticationRequest.nameIdFormat, NameIDFormat.EmailAddress.toSAMLFormat());
    } 
  }
  
  private boolean isLogin() {
    return this instanceof LoginAction;
  }
  
  public static interface SAMLCallable {
    String call() throws JsonProcessingException, SAMLException;
  }
}
