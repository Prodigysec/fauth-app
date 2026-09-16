package io.fusionauth.app.action.oauth2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.inject.Inject;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.user.IdentityHelper;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.primeframework.PostAuthenticationStep;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationOAuthScope;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.api.LoginPingRequest;
import io.fusionauth.domain.oauth2.AccessToken;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.domain.oauth2.OAuthScopeConsentMode;
import io.fusionauth.domain.oauth2.ProvidedScopePolicy;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class ConsentAction extends BaseOAuthCompletionAction {
  public String action;
  
  @FTLVariable
  public List<ApplicationOAuthScope> optionalScopes = new ArrayList<>();
  
  @FTLVariable
  public List<ApplicationOAuthScope> requiredScopes = new ArrayList<>();
  
  public Map<String, Boolean> scopeConsents = new HashMap<>();
  
  @FTLVariable
  public Set<String> unknownScopes = new HashSet<>();
  
  @Inject
  protected ConsentAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
  }
  
  public String get() {
    if (!shouldPromptForOAuthScopeConsent(this.codeUser.id)) {
      this.userState = IdentityHelper.getUserStateFromUserAndRegistration(this.codeUser, this.codeUser.getRegistrationForApplication(this.codeApplication.id));
      return buildAuthorizedResponse();
    } 
    if (this.prompts.contains("none")) {
      buildAuthorizedRedirectWithError((new OAuthService.OAuthValidationResult()).withError(new OAuthError(OAuthError.OAuthErrorType.consent_required, OAuthError.OAuthErrorReason.consent_required, "The user is required to consent to one or more scopes in order to complete authentication.")));
      return "authorized-redirect-with-error";
    } 
    populateScopeLists();
    return "input";
  }
  
  public String post() {
    this.userState = IdentityHelper.getUserStateFromUserAndRegistration(this.codeUser, this.codeUser.getRegistrationForApplication(this.codeApplication.id));
    if (this.codeApplication.oauthConfiguration.consentMode.equals(OAuthScopeConsentMode.RememberDecision))
      this.oauthService.persistUserConsentChoices(this.codeTenant, this.codeApplication, this.codeUser, this.scopeConsents); 
    return buildAuthorizedResponse();
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    String str = validateAndHandleErrors(false);
    if (str != null)
      throw new ErrorException(str, false); 
    if (this.codeUser == null) {
      deleteLoginIntent();
      buildRedirectToAuthorizeURI();
      throw new ErrorException("redirect-to-authorize");
    } 
    if (getPostAuthenticationStep() != this.codeLoginIntent.step)
      throw new ErrorException(handleRedirectToExpectedStep(this.codeLoginIntent)); 
  }
  
  @ValidationMethod
  public void validatePost() {
    try {
      if (this.codeUser == null) {
        deleteLoginIntent();
        buildRedirectToAuthorizeURI();
        throw new ErrorException("redirect-to-authorize");
      } 
      if (!ReactorStatusValidator.isLicensedFor(this.frontEndSupport.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.advancedOAuthScopesThirdPartyApplications)) {
        buildRedirectToAuthorizeURI();
        throw new ErrorException("redirect-to-authorize");
      } 
      Set<String> set = (Set)this.scopeConsents.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.toSet());
      String str = validateAndHandleErrors(false, (Predicate<OAuthError>)null, set);
      if (str != null)
        throw new ErrorException(str, false); 
      if ("cancel".equals(this.action)) {
        buildAuthorizedRedirectWithError((new OAuthService.OAuthValidationResult())
            .withError(new OAuthError(OAuthError.OAuthErrorType.access_denied, OAuthError.OAuthErrorReason.consent_canceled, "The user has denied access to the requested scopes")));
        throw new ErrorException("authorized-redirect-with-error", false);
      } 
    } catch (ErrorException errorException) {
      deleteLoginIntent();
      throw errorException;
    } 
    if (getPostAuthenticationStep() != this.codeLoginIntent.step)
      throw new ErrorException(handleRedirectToExpectedStep(this.codeLoginIntent)); 
  }
  
  @JsonIgnore
  protected PostAuthenticationStep getPostAuthenticationStep() {
    return PostAuthenticationStep.Consent;
  }
  
  private String buildAuthorizedResponse() {
    boolean bool1 = (this.response_mode != null && this.response_mode.equals("form_post")) ? true : false;
    boolean bool2 = (this.ssoSession.user != null) ? true : false;
    ZonedDateTime zonedDateTime = this.codeLoginIntent.insertInstant;
    boolean bool3 = false;
    if (this.ssoSession.user != null && 
      !this.codeLoginIntent.user.id.equals(this.ssoSession.user.id)) {
      bool2 = false;
      Tenant tenant = (new Tenant()).with(paramTenant -> paramTenant.id = this.ssoSession.user.tenantId);
      this.ssoService.logout(this.ssoSession, this.ssoCookie, tenant, this.frontEndSupport.buildEventInfo(this.metaData));
    } 
    AuthenticationType authenticationType = null;
    if (this.oauth_context != null && this.oauth_context.getFirstAuthenticationType() != null) {
      authenticationType = this.oauth_context.getFirstAuthenticationType();
      bool3 = (this.oauth_context.authenticationTypes.size() == 1 && authenticationType == AuthenticationType.PING) ? true : false;
    } 
    if (bool2 && bool3)
      zonedDateTime = this.ssoSession.getAuthTime(zonedDateTime); 
    if (this.codeLoginIntent.rememberDeviceState == SSOService.RememberDeviceState.Remember) {
      this.ssoService.login(this.ssoSession, this.ssoCookie, this.codeTenant, this.codeUser, this.metaData, zonedDateTime, authenticationType, this.frontEndSupport.getTrustedClientIPAddress());
    } else if (this.codeLoginIntent.rememberDeviceState == SSOService.RememberDeviceState.Forget) {
      this.ssoService.logout(this.ssoSession, this.ssoCookie, this.codeTenant, this.frontEndSupport.buildEventInfo(this.metaData));
    } 
    deleteLoginIntent();
    if (bool3)
      this.client.setTenantId(this.codeLoginIntent.tenantId)
        .loginPingWithRequest((new LoginPingRequest())
          .with(paramLoginPingRequest -> paramLoginPingRequest.userId = this.codeLoginIntent.user.id)
          .with(paramLoginPingRequest -> paramLoginPingRequest.applicationId = this.codeApplication.id)
          .with(paramLoginPingRequest -> paramLoginPingRequest.eventInfo = this.frontEndSupport.buildEventInfo(this.metaData))); 
    if (this.response_type.equals("code")) {
      String str1 = createAuthorizationCode(this.codeUser, this.codeApplication.id, authenticationType, zonedDateTime);
      if (bool1) {
        this.formPostResponse = new BaseOAuthAction.FormPostResponse(str1, this.codeTenant.issuer);
        return "post-to-reply";
      } 
      buildAuthorizedRedirectForAuthorizationGrant(str1);
      return "authorized-redirect";
    } 
    AccessToken accessToken = createAccessTokenForImplicitGrant(this.codeUser, authenticationType);
    String str = this.scopes.isEmpty() ? null : String.join(" ", (Iterable)this.scopes);
    if (bool1) {
      this.formPostResponse = new BaseOAuthAction.FormPostResponse(accessToken, str, this.codeTenant.issuer);
      return "post-to-reply";
    } 
    buildAuthorizedRedirectForImplicitGrant(accessToken, str);
    return "authorized-redirect";
  }
  
  private void populateScopeLists() {
    Map<String, ApplicationOAuthScope> map = prepareApplicationOAuthScopesMap(this.codeApplication);
    this.scopes.forEach(paramString -> {
          this.scopeConsents.put(paramString, Boolean.valueOf(true));
          ApplicationOAuthScope applicationOAuthScope = (ApplicationOAuthScope)paramMap.get(paramString);
          if (applicationOAuthScope != null) {
            if (applicationOAuthScope.required) {
              this.requiredScopes.add(applicationOAuthScope);
            } else {
              this.optionalScopes.add(applicationOAuthScope);
            } 
          } else {
            this.unknownScopes.add(paramString);
          } 
        });
  }
  
  private Map<String, ApplicationOAuthScope> prepareApplicationOAuthScopesMap(Application paramApplication) {
    Map<String, ApplicationOAuthScope> map = (Map)paramApplication.scopes.stream().collect(Collectors.toMap(paramApplicationOAuthScope -> paramApplicationOAuthScope.name, paramApplicationOAuthScope -> paramApplicationOAuthScope));
    ProvidedScopePolicy providedScopePolicy = paramApplication.oauthConfiguration.providedScopePolicy;
    Arrays.<Field>stream(ProvidedScopePolicy.class.getDeclaredFields())
      .map(Field::getName)
      .filter(paramString -> (paramProvidedScopePolicy.getScopePolicy(paramString)).enabled)
      .map(paramString -> {
          ApplicationOAuthScope applicationOAuthScope = new ApplicationOAuthScope(paramString);
          applicationOAuthScope.required = (paramProvidedScopePolicy.getScopePolicy(paramString)).required;
          return applicationOAuthScope;
        }).forEach(paramApplicationOAuthScope -> paramMap.put(paramApplicationOAuthScope.name, paramApplicationOAuthScope));
    return map;
  }
}
