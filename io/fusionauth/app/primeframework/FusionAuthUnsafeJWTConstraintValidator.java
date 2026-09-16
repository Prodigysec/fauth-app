package io.fusionauth.app.primeframework;

import io.fusionauth.jwt.domain.JWT;
import org.primeframework.mvc.security.JWTConstraintsValidator;

public class FusionAuthUnsafeJWTConstraintValidator implements JWTConstraintsValidator {
  public boolean validate(JWT paramJWT, String[] paramArrayOfString) {
    return true;
  }
}
