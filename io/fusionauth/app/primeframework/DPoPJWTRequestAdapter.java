package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import io.fusionauth.api.domain.TokenResult;
import io.fusionauth.api.service.oauth2.DPoPService;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.security.JWTRequestAdapter;
import org.primeframework.mvc.security.UnauthenticatedException;

public class DPoPJWTRequestAdapter implements JWTRequestAdapter {
  private static final String JWTScheme = "jwt ";
  
  protected final DPoPService dpopService;
  
  protected final HTTPRequest request;
  
  protected final HTTPResponse response;
  
  @Inject
  public DPoPJWTRequestAdapter(DPoPService paramDPoPService, HTTPRequest paramHTTPRequest, HTTPResponse paramHTTPResponse) {
    this.dpopService = paramDPoPService;
    this.request = paramHTTPRequest;
    this.response = paramHTTPResponse;
  }
  
  public String getEncodedJWT() {
    TokenResult tokenResult = this.dpopService.extractAccessTokenFromAuthorizationHeader(this.request);
    if (tokenResult.error() != null)
      throw new UnauthenticatedException(); 
    if (tokenResult.accessToken() != null)
      return tokenResult.accessToken(); 
    String str1 = this.request.getHeader("Authorization");
    if (str1 != null && 
      str1.toLowerCase().startsWith("jwt ")) {
      String str = str1.substring("jwt ".length());
      if (this.dpopService.isJWTDPoPBound(str))
        throw new UnauthenticatedException(); 
      return str;
    } 
    Cookie cookie = this.request.getCookie(cookieName());
    String str2 = (cookie != null) ? cookie.value : null;
    if (str2 != null && 
      this.dpopService.isJWTDPoPBound(str2))
      throw new UnauthenticatedException(); 
    return str2;
  }
  
  public String invalidateJWT() {
    Cookie cookie = this.request.getCookie(cookieName());
    if (cookie != null) {
      String str = cookie.value;
      cookie.value = null;
      cookie.maxAge = Long.valueOf(0L);
      cookie.path = "/";
      this.response.addCookie(cookie);
      return str;
    } 
    return null;
  }
  
  public boolean requestContainsJWT() throws UnauthenticatedException {
    return (getEncodedJWT() != null);
  }
  
  protected String cookieName() {
    return "access_token";
  }
}
