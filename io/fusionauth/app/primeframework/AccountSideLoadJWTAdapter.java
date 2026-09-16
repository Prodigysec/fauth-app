package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import io.fusionauth.api.util.ActionTools;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.security.DefaultJWTRequestAdapter;

public class AccountSideLoadJWTAdapter extends DefaultJWTRequestAdapter {
  @Inject
  public AccountSideLoadJWTAdapter(HTTPRequest paramHTTPRequest, HTTPResponse paramHTTPResponse) {
    super(paramHTTPRequest, paramHTTPResponse);
  }
  
  public String getEncodedJWT() {
    return ActionTools.extractBearerTokenFromAuthorizationHeader(this.request.getHeader("Authorization")).orElse(null);
  }
  
  protected String cookieName() {
    return "account.at";
  }
}
