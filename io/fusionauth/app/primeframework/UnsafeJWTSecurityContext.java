package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import io.fusionauth.api.service.jwt.FusionAuthJWTDecoder;
import io.fusionauth.api.service.jwt.JWTService;
import io.fusionauth.api.service.jwt.JWTValidationContext;
import io.fusionauth.api.service.jwt.TenantSource;
import io.fusionauth.api.service.jwt.ValidatedJWTResult;
import io.fusionauth.jwt.domain.JWT;
import org.primeframework.mvc.security.DefaultJWTSecurityContext;
import org.primeframework.mvc.security.JWTRequestAdapter;
import org.primeframework.mvc.security.UnauthenticatedException;
import org.primeframework.mvc.security.VerifierProvider;

public class UnsafeJWTSecurityContext extends DefaultJWTSecurityContext {
  private final JWTService jwtService;
  
  @Inject
  public UnsafeJWTSecurityContext(JWTRequestAdapter paramJWTRequestAdapter, VerifierProvider paramVerifierProvider, JWTService paramJWTService) {
    super(paramJWTRequestAdapter, paramVerifierProvider);
    this.jwtService = paramJWTService;
  }
  
  public JWT getJWT() {
    try {
      return super.getJWT();
    } catch (UnauthenticatedException unauthenticatedException) {
      String str = this.requestAdapter.getEncodedJWT();
      if (str == null)
        throw unauthenticatedException; 
      ValidatedJWTResult validatedJWTResult = this.jwtService.validateJWT(str, FusionAuthJWTDecoder.JWTConstraints.OAuthUserIdToken, new JWTValidationContext.AudienceApplicationRequired(TenantSource.SignedTid.INSTANCE));
      if (validatedJWTResult.valid && validatedJWTResult.jwt != null)
        return validatedJWTResult.jwt; 
      this.requestAdapter.invalidateJWT();
      throw unauthenticatedException;
    } 
  }
}
