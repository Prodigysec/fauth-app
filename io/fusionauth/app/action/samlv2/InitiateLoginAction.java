package io.fusionauth.app.action.samlv2;

import com.google.inject.Inject;
import com.inversoft.util.SecurityTools;
import io.fusionauth.api.security.KeyCache;
import io.fusionauth.api.service.samlv2.SAMLv2Helper;
import io.fusionauth.api.service.samlv2.SAMLv2ProviderService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.util.PKCETools;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.http.Cookie;
import io.fusionauth.samlv2.domain.Binding;
import io.fusionauth.samlv2.domain.SAMLException;
import io.fusionauth.samlv2.domain.SAMLRequest;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.scope.annotation.ManagedCookie;
import org.primeframework.mvc.scope.annotation.ManagedSessionCookie;
import org.primeframework.mvc.util.QueryStringBuilder;
import org.primeframework.mvc.validation.ValidationMethod;

@Action("{localTenantId}/{client_id}")
@Redirect(code = "redirect-to-oauth", uri = "${redirectToOAuthURI}")
@ThemedForward(code = "post-to-reply", page = "post.ftl")
public class InitiateLoginAction extends BaseSAMLAction {
  public String callbackURL;
  
  @ManagedCookie(name = "fusionauth.app.pkce-verifier")
  public Cookie codeVerifier;
  
  public String issuer;
  
  public String loginId;
  
  public String redirectToOAuthURI;
  
  public String redirect_uri;
  
  @ManagedSessionCookie(name = "saml.csrf", encrypt = false)
  public Cookie samlCSRF;
  
  @Inject
  public InitiateLoginAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, KeyCache paramKeyCache, LoginIntentService paramLoginIntentService, SAMLv2ProviderService paramSAMLv2ProviderService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramKeyCache, paramLoginIntentService, paramSAMLv2ProviderService, paramSSOService, paramThreatDetectionService);
  }
  
  public String get() {
    this.codeVerifier.value = PKCETools.generateCodeVerifier();
    this.samlCSRF.value = SecurityTools.secureRandom(12);
    this.callbackURL = this.samlv2ProviderService.resolveACSDuringIdPInitiatedLoginRequest(this.application, this.redirect_uri, this.RelayState);
    String str = Objects.equals(this.callbackURL, this.RelayState) ? null : this.RelayState;
    SAMLv2ProviderService.SAMLv2State sAMLv2State = new SAMLv2ProviderService.SAMLv2State(this.callbackURL, this.application.id, null, this.application.samlv2Configuration.initiatedLogin.nameIdFormat, str, this.samlCSRF.value);
    this






      
      .redirectToOAuthURI = QueryStringBuilder.builder("/oauth2/authorize").with("code_challenge", PKCETools.generateCodeChallenge(this.codeVerifier.value)).with("code_challenge_method", "S256").with("client_id", this.application.oauthConfiguration.clientId).with("login_hint", this.loginId).with("redirect_uri", "/samlv2/callback/" + String.valueOf(this.tenantId)).with("response_type", "code").with("state", this.frontEndSupport.serializeObjectToBase64(sAMLv2State)).build();
    return "redirect-to-oauth";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validate() {
    if (this.application == null) {
      this.oauthJSONError = this.frontEndSupport.writeToPrettyString(Map.of("message", "The Id provided in the URL does not match any configured SAMLv2 Application."));
      throw new ErrorException("render-error");
    } 
    SAMLv2ProviderService.FusionAuthAuthenticationResponse fusionAuthAuthenticationResponse = this.samlv2ProviderService.validateInitiatedLoginRequest(this.application, this.redirect_uri, this.RelayState);
    if (fusionAuthAuthenticationResponse != null) {
      if (this.application.samlv2Configuration.debug)
        EventLogHelper.create(new EventLog(EventLogType.Debug, "The SAML AuthnRequest was invalid or did not pass validation.\nThe error code is [" + String.valueOf(fusionAuthAuthenticationResponse.status.code) + "] and the error message is [" + fusionAuthAuthenticationResponse.status.message + "].")); 
      try {
        this.callbackURL = this.samlv2ProviderService.resolveACSDuringIdPInitiatedLoginRequest(this.application, this.redirect_uri, this.RelayState);
        if (this.callbackURL == null)
          this.callbackURL = ((URI)this.application.samlv2Configuration.authorizedRedirectURLs.get(0)).toString(); 
        this.RelayState = null;
        String str = (this.ssoSession.id != null) ? this.ssoSession.id.toString() : null;
        fusionAuthAuthenticationResponse.destination = this.callbackURL;
        fusionAuthAuthenticationResponse.issuer = SAMLv2Helper.getIdentityProviderEntityId(this.frontEndSupport.getFusionAuthBaseURL(), fusionAuthAuthenticationResponse.application.tenantId);
        this.samlv2ProviderService.populateErrorResponse(fusionAuthAuthenticationResponse, null, str);
        this.SAMLResponse = this.samlv2ProviderService.buildResponse(fusionAuthAuthenticationResponse);
        this.samlv2ProviderService.logSAMLResponse(fusionAuthAuthenticationResponse.application.samlv2Configuration.debug, Binding.HTTP_POST, "Response", (SAMLRequest)fusionAuthAuthenticationResponse, this.SAMLResponse);
        throw new ErrorException("post-to-reply");
      } catch (SAMLException sAMLException) {
        throw new ErrorException(sAMLException, new Object[0]);
      } 
    } 
  }
}
