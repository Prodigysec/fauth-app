package io.fusionauth.app.action;

import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.oauth2.AccessToken;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.domain.oauth2.UserState;
import io.fusionauth.http.Cookie;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.primeframework.mvc.parameter.annotation.UnknownParameters;
import org.primeframework.mvc.scope.annotation.ManagedCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseOAuthCallbackAction extends BaseThemedAction {
  private static final Logger logger = LoggerFactory.getLogger(BaseOAuthCallbackAction.class);
  
  public String code;
  
  @ManagedCookie(name = "fusionauth.app.pkce-verifier")
  public Cookie codeVerifier;
  
  public boolean passwordChanged;
  
  public String state;
  
  public String timezone;
  
  @UnknownParameters
  public Map<String, String[]> unknownParameters = (Map)new HashMap<>();
  
  public UserState userState;
  
  protected BaseOAuthCallbackAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramSSOService, paramThreatDetectionService);
  }
  
  protected OAuthResult exchangeCodeForToken(Application paramApplication, String paramString) {
    ClientResponse<AccessToken, OAuthError> clientResponse = this.client.exchangeOAuthCodeForAccessTokenUsingPKCE(this.code, paramApplication.oauthConfiguration.clientId, paramApplication.oauthConfiguration.clientSecret, paramString, this.codeVerifier.getValue());
    if (clientResponse.exception != null) {
      logger.error("Unable to call FusionAuth Token endpoint using code [{}].\n", this.code);
      logger.error("Returned Exception", clientResponse.exception);
      addGeneralError("[TokenExchangeException]", new Object[0]);
      return null;
    } 
    if (!clientResponse.wasSuccessful()) {
      logger.error("Unable to call FusionAuth Token endpoint using code [{}]. HTTP Status Code [{}]. Error message: \n[{}]", new Object[] { this.code, Integer.valueOf(clientResponse.status), clientResponse.errorResponse });
      addGeneralError("[TokenExchangeFailed]", new Object[0]);
      return null;
    } 
    this.codeVerifier = null;
    ClientResponse<UserResponse, Errors> clientResponse1 = this.client.retrieveUser(((AccessToken)clientResponse.successResponse).userId);
    if (clientResponse1.wasSuccessful())
      return new OAuthResult(((UserResponse)clientResponse1.successResponse).user, (AccessToken)clientResponse.successResponse); 
    EventLogHelper.create(new EventLog(EventLogType.Error, "Unexpected error during FusionAuth login. The request for the User with a JWT came back as unsuccessful.\nStatus code: ${statusCode}\nActual JWT: ${token}\n"


          
          .replace("${statusCode}", "" + clientResponse1.status)
          .replace("${token}", ((AccessToken)clientResponse.successResponse).token)));
    transferErrors((Errors)clientResponse1.errorResponse);
    return null;
  }
  
  protected Application loadApplication(UUID paramUUID) {
    ClientResponse<ApplicationResponse, Void> clientResponse = this.client.retrieveApplication(paramUUID);
    if (!clientResponse.wasSuccessful())
      return null; 
    return ((ApplicationResponse)clientResponse.successResponse).application;
  }
  
  public static class OAuthResult {
    public AccessToken accessToken;
    
    public User user;
    
    public OAuthResult(User param1User, AccessToken param1AccessToken) {
      this.user = param1User;
      this.accessToken = param1AccessToken;
    }
  }
}
