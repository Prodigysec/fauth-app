package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.security.JWTConstraintsValidator;
import org.primeframework.mvc.security.JWTSecurityContext;
import org.primeframework.mvc.security.JWTSecurityScheme;

public class FusionAuthUnsafeJWTSecurityScheme extends JWTSecurityScheme {
  @Inject
  public FusionAuthUnsafeJWTSecurityScheme(ActionInvocationStore paramActionInvocationStore, @Named("UnsafeJWTSecurityContext") JWTSecurityContext paramJWTSecurityContext, @Named("UnsafeJWTValidator") JWTConstraintsValidator paramJWTConstraintsValidator, HTTPRequest paramHTTPRequest) {
    super(paramActionInvocationStore, paramJWTConstraintsValidator, paramJWTSecurityContext, paramHTTPRequest);
  }
}
