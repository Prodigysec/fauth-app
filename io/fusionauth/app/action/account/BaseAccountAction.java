package io.fusionauth.app.action.account;

import com.google.inject.Inject;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.action.oauth2.BaseOAuthAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import java.net.URI;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.action.result.annotation.SaveRequest;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.util.QueryStringBuilder;

@List({@Redirect(code = "invalid-request", uri = "${redirectToRoot}"), @Redirect(code = "redirect-to-account", uri = "${redirectToAccount}"), @Redirect(code = "success", uri = "${redirectToAccount}")})
@SaveRequest(uri = "${redirectToLogin}")
public abstract class BaseAccountAction extends BaseOAuthAction {
  @FTLVariable
  public boolean formConfigured;
  
  @FTLVariable
  public boolean multiFactorAvailable;
  
  @FTLVariable
  public boolean passwordSet;
  
  public String redirectToAccount;
  
  public String redirectToLogin;
  
  public String redirectToRoot;
  
  @FTLVariable
  public String redirectURI;
  
  public UserRegistration registration = new UserRegistration();
  
  public User user;
  
  public UUID userId;
  
  @FTLVariable
  public boolean webauthnAvailable;
  
  @Inject
  public BaseAccountAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
  }
  
  @PostParameterMethod
  public void setup() {
    if (this.client_id == null) {
      this.redirectToRoot = baseQueryBuilder("/").build();
      throw new ErrorException("invalid-request", false);
    } 
    if (this.codeApplication != null)
      setupAccountRequestAttributes(this.codeApplication, this.frontEndSupport.request); 
    this.redirectToLogin = baseQueryBuilder("/account/login").build();
    resolveUserAndZoneId();
    resolveRedirectURI();
    this


      
      .redirectToAccount = QueryStringBuilder.builder("/account/").with("client_id", this.client_id).with("tenantId", this.tenantId).with("redirect_uri", this.redirectURI).build();
  }
  
  private void resolveRedirectURI() {
    if (this.redirect_uri != null) {
      this.redirectURI = this.redirect_uri.toString();
      return;
    } 
    if (this.frontEndSupport.isInternalRedirect())
      return; 
    String str = this.frontEndSupport.request.getHeader("Referer");
    if (str == null)
      return; 
    try {
      URI.create(str);
    } catch (IllegalArgumentException illegalArgumentException) {
      return;
    } 
    this.redirectURI = str;
    this.frontEndSupport.request.addURLParameter("redirect_uri", str);
  }
  
  protected void accountValidation() {
    OAuthService.OAuthValidationResult oAuthValidationResult = this.oauthService.validateSelfServiceRequest(this.tenantId, this.client_id);
    setAntiClickJackingHeader(oAuthValidationResult.application);
    if (oAuthValidationResult.error != null) {
      if (oAuthValidationResult.doNotRedirect) {
        this.oauthJSONError = this.frontEndSupport.writeToPrettyString(oAuthValidationResult.error);
        throw new ErrorException("render-error", false);
      } 
      buildAuthorizedRedirectWithError(oAuthValidationResult);
      throw new ErrorException("authorized-redirect-with-error", false);
    } 
    setResultValues(oAuthValidationResult);
    User user = (User)this.frontEndSupport.userLoginSecurityContext.getCurrentUser();
    this.passwordSet = (user.encryptionScheme != null);
    setUserVariables(user);
    this.user = setupUserForFTL();
    this.userId = this.codeUser.id;
    if (this.codeUser.getRegistrationForApplication(this.codeApplication.id) == null) {
      if (this instanceof IndexAction) {
        addGeneralInfo("[SelfServiceUserNotRegisteredException]", new Object[0]);
        throw new ErrorException("input", false);
      } 
      throw new ErrorException("redirect-to-account");
    } 
    this.formConfigured = (this.codeApplication.formConfiguration.selfServiceFormId != null);
    if (!this.formConfigured) {
      if (!(this instanceof IndexAction))
        throw new ErrorException("redirect-to-account"); 
      addGeneralError("[SelfServiceFormNotConfigured]", new Object[0]);
    } 
    this.multiFactorAvailable = this.tenant.multiFactorConfiguration.anyEnabled();
    this.webauthnAvailable = this.tenant.webAuthnConfiguration.enabled;
  }
  
  protected User setupUserForFTL() {
    return (new User(this.codeUser)).secure();
  }
  
  public static class AccountConfirm {
    public UserRegistration registration;
    
    public User user;
  }
}
