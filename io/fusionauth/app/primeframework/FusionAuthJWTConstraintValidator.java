package io.fusionauth.app.primeframework;

import io.fusionauth.api.service.jwt.FusionAuthJWTDecoder;
import io.fusionauth.api.service.jwt.ValidatedJWTResult;
import io.fusionauth.domain.Application;
import io.fusionauth.jwt.domain.JWT;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.primeframework.mvc.security.JWTConstraintsValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FusionAuthJWTConstraintValidator implements JWTConstraintsValidator {
  private static final Logger logger = LoggerFactory.getLogger(FusionAuthJWTConstraintValidator.class);
  
  public boolean validate(JWT paramJWT, String[] paramArrayOfString) {
    if (paramJWT == null)
      return false; 
    ValidatedJWTResult validatedJWTResult = FusionAuthJWTDecoder.validateConstraints(paramJWT, FusionAuthJWTDecoder.JWTConstraints.UserAccessToken);
    if (!validatedJWTResult.valid) {
      logger.debug(validatedJWTResult.getMessage());
      return false;
    } 
    if (paramArrayOfString.length > 0) {
      String str = paramJWT.getString("applicationId");
      if (!Application.FUSIONAUTH_APP_ID.toString().equals(str)) {
        logger.debug("JWT is valid. However, the [applicationId] claims is not for the FusionAuth admin application.");
        return false;
      } 
      List list = paramJWT.getList("roles");
      if (list == null) {
        logger.debug("JWT is valid. However, the [roles] claim is missing which indicates the user does not have access to this application.");
        return false;
      } 
      Objects.requireNonNull(list);
      boolean bool = Arrays.<String>stream(paramArrayOfString).anyMatch(list::contains);
      if (!bool) {
        logger.debug("JWT is valid. However, the [roles] claim does not contain any of the required roles.");
        return false;
      } 
    } 
    return true;
  }
}
