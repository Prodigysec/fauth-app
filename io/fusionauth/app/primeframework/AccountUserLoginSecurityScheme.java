package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.fusionauth.app.Cookies;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.HTTPMethod;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import io.fusionauth.jwt.domain.JWT;
import org.primeframework.mvc.security.JWTRequestAdapter;
import org.primeframework.mvc.security.JWTSecurityContext;
import org.primeframework.mvc.security.SecurityScheme;
import org.primeframework.mvc.security.UnauthenticatedException;
import org.primeframework.mvc.security.UnauthorizedException;
import org.primeframework.mvc.security.UserLoginSecurityScheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountUserLoginSecurityScheme implements SecurityScheme {
  private static final Logger logger = LoggerFactory.getLogger(AccountUserLoginSecurityScheme.class);
  
  private final JWTRequestAdapter jwtRequestAdapter;
  
  private final JWTSecurityContext jwtSecurityContext;
  
  private final HTTPRequest request;
  
  private final HTTPResponse response;
  
  private final UserLoginSecurityScheme userLoginSecurityScheme;
  
  @Inject
  public AccountUserLoginSecurityScheme(HTTPRequest paramHTTPRequest, HTTPResponse paramHTTPResponse, @Named("AccountSideLoadJWTAdapter") JWTRequestAdapter paramJWTRequestAdapter, @Named("AccountSideLoadJWTSecurityContext") JWTSecurityContext paramJWTSecurityContext, UserLoginSecurityScheme paramUserLoginSecurityScheme) {
    this.request = paramHTTPRequest;
    this.response = paramHTTPResponse;
    this.jwtRequestAdapter = paramJWTRequestAdapter;
    this.jwtSecurityContext = paramJWTSecurityContext;
    this.userLoginSecurityScheme = paramUserLoginSecurityScheme;
  }
  
  public void handle(String[] paramArrayOfString) {
    logger.debug("Calling scheme handle with constraints: [{}]. Note, constraints are unexpected in this path.", String.join(", ", (CharSequence[])paramArrayOfString));
    JWT jWT = null;
    try {
      jWT = this.jwtSecurityContext.getJWT();
    } catch (UnauthenticatedException unauthenticatedException) {}
    if (this.request.getMethod() == HTTPMethod.GET) {
      if (jWT != null) {
        String str = this.jwtRequestAdapter.getEncodedJWT();
        Cookies.addHttpOnlySession(this.request, this.response, "account.at", str);
        this.request.addCookies(new Cookie[] { new Cookie("account.at", str) });
      } 
    } else if (jWT != null) {
      throw new UnauthorizedException();
    } 
    this.userLoginSecurityScheme.handle(paramArrayOfString);
  }
}
