package io.fusionauth.app.action.app;

import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.util.URITools;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.oauth2.AccessToken;
import io.fusionauth.domain.util.DefaultTools;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.action.result.annotation.JSON.List;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.action.result.annotation.Status.List;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

@List({@JSON(code = "render", status = 200, cacheControl = "no-store"), @JSON(code = "input", status = 400, cacheControl = "no-store"), @JSON(code = "error", status = 500, cacheControl = "no-store")})
@List({@Status(code = "success", status = 200), @Status(code = "unauthorized", status = 401), @Status(code = "forbidden", status = 403), @Status(code = "not-allowed", status = 405)})
public abstract class BaseAppAction {
  protected final FrontEndSupport frontEndSupport;
  
  protected final OAuthService oauthService;
  
  public String client_id;
  
  public URI redirect_uri;
  
  public UUID tenantId;
  
  protected FusionAuthClient client;
  
  protected Application codeApplication;
  
  protected Tenant codeTenant;
  
  public BaseAppAction(FrontEndSupport paramFrontEndSupport, OAuthService paramOAuthService) {
    this.client = paramFrontEndSupport.fusionAuthClientProvider.get();
    this.frontEndSupport = paramFrontEndSupport;
    this.oauthService = paramOAuthService;
  }
  
  protected static long getTokenExpirationInSeconds(AccessToken paramAccessToken) {
    return Instant.now().plusSeconds(paramAccessToken.expiresIn.intValue()).getEpochSecond();
  }
  
  @PostParameterMethod
  public void setupClient() {
    this
      
      .client = (this.tenantId != null) ? this.frontEndSupport.fusionAuthClientProvider.get(this.tenantId) : this.frontEndSupport.fusionAuthClientProvider.get();
  }
  
  protected void deleteCookies(String... paramVarArgs) {
    String str = getCookieDomain();
    this.frontEndSupport.deleteCookiesWithDomain(str, paramVarArgs);
  }
  
  protected String getCookieDomain() {
    return URITools.getApexDomain(URI.create(this.frontEndSupport.getFusionAuthBaseURL()));
  }
  
  protected void handleAccessTokenResponse(AccessToken paramAccessToken) {
    String str = URITools.getApexDomain(URI.create(this.frontEndSupport.getFusionAuthBaseURL()));
    this.frontEndSupport.addHttpOnlyPersistentCookie("app.at", paramAccessToken.token, str);
    this.frontEndSupport.addPersistentCookie("app.at_exp", "" + getTokenExpirationInSeconds(paramAccessToken), paramAccessToken.expiresIn.intValue(), str);
    if (paramAccessToken.idToken != null) {
      this.frontEndSupport.addPersistentCookie("app.idt", paramAccessToken.idToken, str);
    } else {
      this.frontEndSupport.deleteCookies(new String[] { "app.idt" });
    } 
    if (paramAccessToken.refreshToken != null) {
      this.frontEndSupport.addHttpOnlyPersistentCookie("app.rt", paramAccessToken.refreshToken, str);
    } else {
      this.frontEndSupport.deleteCookies(new String[] { "app.rt" });
    } 
  }
  
  protected void validateOrigin() {
    String str = DefaultTools.<String>defaultIfNull(this.frontEndSupport.request.getHeader("Origin"), this.frontEndSupport.request.getHeader("Referer"));
    if (str == null)
      throw new ErrorException("forbidden"); 
    URI uRI1 = URI.create(str);
    URI uRI2 = URI.create(this.frontEndSupport.request.getBaseURL());
    if (!URITools.hostDomainsEqual(uRI1, uRI2))
      throw new ErrorException("forbidden"); 
  }
}
