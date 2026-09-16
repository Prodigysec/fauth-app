package io.fusionauth.app.action.samlv2;

import com.google.inject.Inject;
import com.inversoft.util.MapBuilder;
import io.fusionauth.api.service.samlv2.SAMLv2Helper;
import io.fusionauth.api.service.samlv2.SAMLv2ProviderService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.util.XMLTools;
import io.fusionauth.app.action.BaseOAuthCallbackAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.http.Cookie;
import io.fusionauth.jwt.JWTUtils;
import io.fusionauth.samlv2.domain.AuthenticationRequest;
import io.fusionauth.samlv2.domain.Binding;
import io.fusionauth.samlv2.domain.ResponseStatus;
import io.fusionauth.samlv2.util.SAMLTools;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Map;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.scope.annotation.ManagedSessionCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Action("{tenantId}")
@ThemedForward(code = "post-to-reply", page = "post.ftl")
public class CallbackAction extends BaseOAuthCallbackAction {
  private static final Logger logger = LoggerFactory.getLogger(CallbackAction.class);
  
  private final SAMLv2ProviderService samlv2ProviderService;
  
  public String RelayState;
  
  public String SAMLResponse;
  
  @FTLVariable
  public String callbackURL;
  
  @ManagedSessionCookie(name = "saml.csrf", encrypt = false)
  public Cookie samlCSRF;
  
  @Inject
  public CallbackAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, SAMLv2ProviderService paramSAMLv2ProviderService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramSSOService, paramThreatDetectionService);
    this.samlv2ProviderService = paramSAMLv2ProviderService;
    this.ssoService = paramSSOService;
  }
  
  public String get() {
    if (this.code == null || this.state == null) {
      Map map = MapBuilder.map((Object[])new String[] { "message", "OAuth return is missing the authorization code and/or the SAML encoded state." });
      this.oauthJSONError = this.frontEndSupport.writeToPrettyString(map);
      return "render-error";
    } 
    try {
      SAMLv2ProviderService.SAMLv2State sAMLv2State = (SAMLv2ProviderService.SAMLv2State)this.frontEndSupport.objectMapper.readValue(Base64.getUrlDecoder().decode(this.state), SAMLv2ProviderService.SAMLv2State.class);
      String str1 = this.samlCSRF.value;
      this.samlCSRF.value = null;
      if (str1 == null || !str1.equals(sAMLv2State.csrf)) {
        Map map = Map.of("message", "OAuth return is missing a valid CSRF token.");
        this.oauthJSONError = this.frontEndSupport.writeToPrettyString(map);
        return "render-error";
      } 
      SAMLv2ProviderService.FusionAuthAuthenticationResponse fusionAuthAuthenticationResponse = new SAMLv2ProviderService.FusionAuthAuthenticationResponse();
      fusionAuthAuthenticationResponse.application = loadApplication(sAMLv2State.ai);
      if (fusionAuthAuthenticationResponse.application == null) {
        Map map = MapBuilder.map((Object[])new String[] { "message", "OAuth returned successfully but the SAML request was modified and is now invalid." });
        this.oauthJSONError = this.frontEndSupport.writeToPrettyString(map);
        return "render-error";
      } 
      AuthenticationRequest authenticationRequest = new AuthenticationRequest();
      authenticationRequest.id = sAMLv2State.id;
      authenticationRequest.acsURL = sAMLv2State.acs;
      authenticationRequest.issuer = fusionAuthAuthenticationResponse.application.samlv2Configuration.issuer;
      authenticationRequest.nameIdFormat = sAMLv2State.nf;
      authenticationRequest.version = "2.0";
      String str2 = (this.ssoSession.id != null) ? this.ssoSession.id.toString() : null;
      fusionAuthAuthenticationResponse.issuer = SAMLv2Helper.getIdentityProviderEntityId(this.frontEndSupport.getFusionAuthBaseURL(), fusionAuthAuthenticationResponse.application.tenantId);
      this.callbackURL = sAMLv2State.acs;
      this.RelayState = sAMLv2State.rs;
      BaseOAuthCallbackAction.OAuthResult oAuthResult = exchangeCodeForToken(fusionAuthAuthenticationResponse.application, "/samlv2/callback/" + String.valueOf(fusionAuthAuthenticationResponse.application.tenantId));
      if (oAuthResult == null) {
        this.samlv2ProviderService.populateErrorResponse(fusionAuthAuthenticationResponse, authenticationRequest, str2);
        fusionAuthAuthenticationResponse.status.code = ResponseStatus.AuthenticationFailed;
        fusionAuthAuthenticationResponse.status.message = "Unable to authentication the user via the nested OAuth workflow. Consult the logs for additional details.";
        this.SAMLResponse = this.samlv2ProviderService.buildResponse(fusionAuthAuthenticationResponse);
        return "post-to-reply";
      } 
      if (sAMLv2State.atc != null) {
        boolean bool = reauthenticationOccurred(oAuthResult, sAMLv2State);
        if (!bool) {
          this.samlv2ProviderService.populateErrorResponse(fusionAuthAuthenticationResponse, authenticationRequest, str2);
          fusionAuthAuthenticationResponse.status.code = ResponseStatus.AuthenticationFailed;
          fusionAuthAuthenticationResponse.status.message = "ForceAuthn was requested. Unable to reauthenticate the user.";
          this.SAMLResponse = this.samlv2ProviderService.buildResponse(fusionAuthAuthenticationResponse);
          return "post-to-reply";
        } 
      } 
      fusionAuthAuthenticationResponse.authnInstant = this.ssoSession.getAuthTime(ZonedDateTime.now(ZoneOffset.UTC));
      fusionAuthAuthenticationResponse.status.code = ResponseStatus.Success;
      this.samlv2ProviderService.populateResponse(fusionAuthAuthenticationResponse, authenticationRequest, str2, oAuthResult.user);
      this.SAMLResponse = this.samlv2ProviderService.buildResponse(fusionAuthAuthenticationResponse);
      logger.debug("SAMLResponse being sent back to the service provider at [{}] is [{}]", this.callbackURL, this.SAMLResponse);
      if (fusionAuthAuthenticationResponse.application.samlv2Configuration.debug) {
        String str = new String(SAMLTools.decode(this.SAMLResponse), StandardCharsets.UTF_8);
        EventLogHelper.create(new EventLog(EventLogType.Debug, "SAMLResponse being sent back to the service provider.\n" + this.callbackURL + "\n\nBinding:\n" + Binding.HTTP_POST
              
              .toSAMLFormat() + "\n\nEncoded response:\n" + this.SAMLResponse + "\n\nUn-encoded XML response:\n" + 
              
              XMLTools.prettyPrint(str) + "\n"));
      } 
      return "post-to-reply";
    } catch (IOException|io.fusionauth.samlv2.domain.SAMLException|io.fusionauth.api.service.system.LambdaInvocationException iOException) {
      Map map = MapBuilder.map((Object[])new String[] { "message", "Either parsing the SAML state or generating the SAML response failed (including a possible lambda failure). Consult the event log or log files for details." });
      this.oauthJSONError = this.frontEndSupport.writeToPrettyString(map);
      logger.error("Unable to parse SAML encoded state or generate SAMLResponse.", iOException);
      EventLogHelper.create(new EventLog(EventLogType.Error, "Unable to parse SAML encoded state or generate SAMLResponse.", iOException));
      return "render-error";
    } 
  }
  
  private boolean reauthenticationOccurred(BaseOAuthCallbackAction.OAuthResult paramOAuthResult, SAMLv2ProviderService.SAMLv2State paramSAMLv2State) {
    Object object = JWTUtils.decodePayload(paramOAuthResult.accessToken.token).getObject("auth_time");
    if (object != null) {
      long l1 = Long.parseLong(paramSAMLv2State.atc);
      long l2 = Long.parseLong(object.toString());
      return (l1 < l2);
    } 
    return true;
  }
}
