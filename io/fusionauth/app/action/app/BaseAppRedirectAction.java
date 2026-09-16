package io.fusionauth.app.action.app;

import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.util.PKCETools;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.http.Cookie;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.scope.annotation.ManagedCookie;
import org.primeframework.mvc.util.QueryStringBuilder;
import org.primeframework.mvc.util.QueryStringTools;
import org.primeframework.mvc.validation.ValidationMethod;

@Redirect(code = "redirect-to-start", uri = "${redirectToStartURI}")
public abstract class BaseAppRedirectAction extends BaseAppAction {
  private final String baseURI;
  
  @ManagedCookie(name = "app.pkce_v")
  public Cookie codeVerifier;
  
  public String prompt;
  
  public String redirectToStartURI;
  
  public String scope;
  
  public String state;
  
  private OAuthService.OAuthValidationResult result;
  
  protected BaseAppRedirectAction(String paramString, FrontEndSupport paramFrontEndSupport, OAuthService paramOAuthService) {
    super(paramFrontEndSupport, paramOAuthService);
    this.baseURI = paramString;
  }
  
  public String get() {
    if (this.scope == null)
      this.scope = "openid offline_access"; 
    URI uRI = (this.result.appCallback != null) ? this.result.appCallback : URI.create("/app/callback");
    this.state = this.frontEndSupport.serializeObjectToBase64(new OAuthService.AppOAuthState(this.client_id, this.redirect_uri, this.state));
    this.codeVerifier.setValue(PKCETools.generateCodeVerifier());
    HashMap<Object, Object> hashMap = new HashMap<>();
    QueryStringBuilder queryStringBuilder = QueryStringBuilder.builder(this.baseURI);
    if (this.frontEndSupport.request.getQueryString() != null)
      hashMap.putAll(QueryStringTools.parseQueryString(this.frontEndSupport.request.getQueryString())); 
    if (this.client_id != null)
      hashMap.put("client_id", List.of(this.client_id)); 
    if (this.codeTenant != null && this.tenantId == null)
      hashMap.put("tenantId", List.of(this.codeTenant.id.toString())); 
    hashMap.put("code_challenge", List.of(PKCETools.generateCodeChallenge(this.codeVerifier.getValue())));
    hashMap.put("code_challenge_method", List.of("S256"));
    hashMap.put("redirect_uri", List.of(uRI.toString()));
    hashMap.put("response_type", List.of("code"));
    hashMap.put("scope", List.of(this.scope));
    hashMap.put("state", List.of(this.state));
    hashMap.keySet().forEach(paramString -> ((List)paramMap.get(paramString)).forEach(()));
    this.redirectToStartURI = queryStringBuilder.build();
    return "redirect-to-start";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    validateOrigin();
    this.result = this.oauthService.validateAppRedirectRequest(this.client_id, this.redirect_uri);
    this.codeApplication = this.result.application;
    this.codeTenant = this.result.tenant;
    this.redirect_uri = this.result.redirect_uri;
  }
}
