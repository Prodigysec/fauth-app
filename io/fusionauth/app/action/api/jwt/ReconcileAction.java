package io.fusionauth.app.action.api.jwt;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.authentication.IdentityProviderAuthenticationService;
import io.fusionauth.api.service.cache.IdentityProviderCache;
import io.fusionauth.api.service.identity.IdentityProviderReaderService;
import io.fusionauth.api.service.jwt.JWTService;
import io.fusionauth.api.service.jwt.RefreshTokenService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.app.action.api.identityProvider.LoginAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.provider.IdentityProviderType;
import java.util.Map;
import org.primeframework.mvc.action.annotation.Action;

@Action
public class ReconcileAction extends LoginAction {
  @Inject
  public ReconcileAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ApplicationReaderService paramApplicationReaderService, ExternalIdentifierReaderService paramExternalIdentifierReaderService, JWTService paramJWTService, IdentityProviderCache paramIdentityProviderCache, Map<IdentityProviderType, IdentityProviderAuthenticationService> paramMap, IdentityProviderReaderService paramIdentityProviderReaderService, RefreshTokenService paramRefreshTokenService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache, paramApplicationReaderService, paramExternalIdentifierReaderService, paramJWTService, paramIdentityProviderCache, paramMap, paramIdentityProviderReaderService, paramRefreshTokenService);
  }
  
  protected IdentityProviderType requiredType() {
    return IdentityProviderType.ExternalJWT;
  }
}
