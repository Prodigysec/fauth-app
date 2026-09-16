package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.fusionauth.api.scim.SCIMException;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.security.JWTConstraintsValidator;
import org.primeframework.mvc.security.JWTSecurityContext;
import org.primeframework.mvc.security.JWTSecurityScheme;

public class FusionAuthSCIMSecurityScheme extends JWTSecurityScheme {
  @Inject
  public FusionAuthSCIMSecurityScheme(ActionInvocationStore paramActionInvocationStore, JWTSecurityContext paramJWTSecurityContext, @Named("SCIMConstraintValidator") JWTConstraintsValidator paramJWTConstraintsValidator, HTTPRequest paramHTTPRequest) {
    super(paramActionInvocationStore, paramJWTConstraintsValidator, paramJWTSecurityContext, paramHTTPRequest);
  }
  
  public void handle(String[] paramArrayOfString) {
    try {
      super.handle(paramArrayOfString);
    } catch (ErrorException errorException) {
      throw new SCIMException(errorException);
    } 
  }
}
