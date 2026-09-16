package io.fusionauth.app.action.samlv2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.inversoft.util.SecurityTools;
import io.fusionauth.api.security.KeyCache;
import io.fusionauth.api.service.samlv2.SAMLv2Helper;
import io.fusionauth.api.service.samlv2.SAMLv2ProviderService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.util.PKCETools;
import io.fusionauth.app.Cookies;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.primeframework.ThemedForward.List;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.http.Cookie;
import io.fusionauth.samlv2.domain.AuthenticationRequest;
import io.fusionauth.samlv2.domain.Binding;
import io.fusionauth.samlv2.domain.ResponseStatus;
import io.fusionauth.samlv2.domain.SAMLException;
import io.fusionauth.samlv2.domain.SAMLRequest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.parameter.annotation.UnknownParameters;
import org.primeframework.mvc.scope.annotation.ManagedCookie;
import org.primeframework.mvc.scope.annotation.ManagedSessionCookie;
import org.primeframework.mvc.util.QueryStringBuilder;

@Action("{localTenantId}")
@List({@ThemedForward(code = "input", page = "/oauth2/error.ftl", cacheControl = "no-store"), @ThemedForward(code = "post-to-reply", page = "post.ftl")})
@Redirect(code = "redirect-to-oauth", uri = "${redirectToOAuthURI}")
public class LoginAction extends BaseSAMLAction {
  public String callbackURL;
  
  @ManagedCookie(name = "fusionauth.app.pkce-verifier")
  public Cookie codeVerifier;
  
  public String idp_hint;
  
  public String loginId;
  
  public String redirectToOAuthURI;
  
  public SAMLv2ProviderService.FusionAuthAuthenticationResponse response;
  
  @ManagedSessionCookie(name = "saml.csrf", encrypt = false)
  public Cookie samlCSRF;
  
  @UnknownParameters
  public Map<String, Object> unknownParameters = new HashMap<>();
  
  @Inject
  public LoginAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, KeyCache paramKeyCache, LoginIntentService paramLoginIntentService, SAMLv2ProviderService paramSAMLv2ProviderService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramKeyCache, paramLoginIntentService, paramSAMLv2ProviderService, paramSSOService, paramThreatDetectionService);
  }
  
  public String get() {
    String str = commonSAMLValidation();
    if (str != null)
      return str; 
    this.binding = this.frontEndSupport.isGET() ? Binding.HTTP_Redirect : Binding.HTTP_POST;
    return handleSAMLException(() -> {
          AuthenticationRequest authenticationRequest = this.frontEndSupport.isGET() ? this.samlv2ProviderService.parseAuthNRedirectRequest(this.tenantId, this.frontEndSupport.request.getQueryString()) : this.samlv2ProviderService.parseAuthNPostRequest(this.tenantId, this.SAMLRequest);
          normalize((SAMLRequest)authenticationRequest);
          this.response = this.samlv2ProviderService.validateAuthnRequest((SAMLRequest)authenticationRequest, this.tenantId);
          if (this.response.application == null) {
            this.oauthJSONError = this.frontEndSupport.writeToPrettyString(this.response.status);
            return "render-error";
          } 
          this.callbackURL = this.samlv2ProviderService.resolveACS(this.response.application, authenticationRequest);
          this.response.destination = this.callbackURL;
          if (this.response.application.samlv2Configuration.loginHintConfiguration.enabled)
            this.loginId = this.frontEndSupport.request.getParameter(this.response.application.samlv2Configuration.loginHintConfiguration.parameterName); 
          this.samlv2ProviderService.logSAMLRequest(this.response.application.samlv2Configuration.debug, this.binding, "AuthnRequest", (SAMLRequest)authenticationRequest, this.SAMLRequest);
          if (this.response.status.code != ResponseStatus.Success) {
            String str1 = (this.ssoSession.id != null) ? this.ssoSession.id.toString() : null;
            this.response.issuer = SAMLv2Helper.getIdentityProviderEntityId(this.frontEndSupport.getFusionAuthBaseURL(), this.response.application.tenantId);
            this.samlv2ProviderService.populateErrorResponse(this.response, authenticationRequest, str1);
            if (this.response.application.samlv2Configuration.debug)
              EventLogHelper.create(new EventLog(EventLogType.Debug, "The SAML AuthnRequest was invalid or did not pass validation.\nThe error code is [" + String.valueOf(this.response.status.code) + "] and the error message is [" + this.response.status.message + "].")); 
            this.SAMLResponse = this.samlv2ProviderService.buildResponse(this.response);
            this.samlv2ProviderService.logSAMLResponse(this.response.application.samlv2Configuration.debug, Binding.HTTP_POST, "Response", (SAMLRequest)this.response, this.SAMLResponse);
            return "post-to-reply";
          } 
          this.codeVerifier.value = PKCETools.generateCodeVerifier();
          this.samlCSRF.value = SecurityTools.secureRandom(12);
          SAMLv2ProviderService.SAMLv2State sAMLv2State = new SAMLv2ProviderService.SAMLv2State(this.callbackURL, this.response.application.id, authenticationRequest.id, authenticationRequest.nameIdFormat, this.RelayState, this.samlCSRF.value);
          String str = (Boolean.TRUE == authenticationRequest.forceAuthn) ? "login" : null;
          if (str != null)
            sAMLv2State.atc = this.ssoSession.getAuthTimeClaim(); 
          this.redirectToOAuthURI = QueryStringBuilder.builder("/oauth2/authorize").with("code_challenge", PKCETools.generateCodeChallenge(this.codeVerifier.getValue())).with("code_challenge_method", "S256").with("client_id", this.response.application.oauthConfiguration.clientId).with("idp_hint", this.idp_hint).with("login_hint", this.loginId).with("prompt", str).with("redirect_uri", "/samlv2/callback/" + String.valueOf(this.tenantId)).with("response_type", "code").with("state", Base64.getUrlEncoder().encodeToString(this.frontEndSupport.objectMapper.writeValueAsBytes(sAMLv2State))).build();
          return "redirect-to-oauth";
        });
  }
  
  public String post() {
    if (Cookies.get(this.frontEndSupport.request, "fusionauth.sso") == null)
      this.ssoCookie.value = null; 
    return get();
  }
}
