package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import com.inversoft.authentication.api.domain.AuthenticationKey;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.authentication.api.service.AuthenticationKeySecurityScheme;
import io.fusionauth.api.domain.guice.FusionAuthInternalAPIKey;
import io.fusionauth.domain.APIKey;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.security.UnauthorizedException;

public class FusionAuthInternalAPIKeySecurityScheme extends AuthenticationKeySecurityScheme {
  private final APIKey internalAPIKey;
  
  @Inject
  public FusionAuthInternalAPIKeySecurityScheme(ActionInvocationStore paramActionInvocationStore, HTTPRequest paramHTTPRequest, AuthenticationKeyCache paramAuthenticationKeyCache, @FusionAuthInternalAPIKey APIKey paramAPIKey) {
    super(paramActionInvocationStore, paramHTTPRequest, paramAuthenticationKeyCache);
    this.internalAPIKey = paramAPIKey;
  }
  
  public void handle(String[] paramArrayOfString) {
    super.handle(paramArrayOfString);
    AuthenticationKey authenticationKey = getAuthenticationKey(authenticationKey());
    if (!authenticationKey.id.equals(this.internalAPIKey.id))
      throw new UnauthorizedException(); 
  }
}
