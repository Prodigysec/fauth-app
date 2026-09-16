package io.fusionauth.api.service.authentication;

import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLoginRequest;
import io.fusionauth.domain.api.identityProvider.IdentityProviderStartLoginRequest;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderType;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.primeframework.mvc.security.UnauthenticatedException;

public interface IdentityProviderAuthenticationService {
  public static final int READ_TIMEOUT_MS = 10000;
  
  static AuthenticationType authenticationType(IdentityProviderType paramIdentityProviderType) {
    switch (paramIdentityProviderType) {
      default:
        throw new MatchException(null, null);
      case Apple:
      
      case EpicGames:
      
      case ExternalJWT:
      
      case Facebook:
      
      case Google:
      
      case HYPR:
      
      case LinkedIn:
      
      case Nintendo:
      
      case OpenIDConnect:
      
      case SAMLv2:
      
      case SAMLv2IdPInitiated:
      
      case SonyPSN:
      
      case Steam:
      
      case Twitch:
      
      case Twitter:
      
      case Xbox:
        break;
    } 
    return 














      
      AuthenticationType.Xbox;
  }
  
  AuthenticationType authenticationType();
  
  AuthenticationService.AuthenticationResult login(Tenant paramTenant, Application paramApplication, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) throws UnauthenticatedException;
  
  default StartResult start(Tenant paramTenant, Application paramApplication, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, User paramUser, IdentityProviderStartLoginRequest paramIdentityProviderStartLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) throws UnauthenticatedException {
    throw new UnsupportedOperationException("This method is not implemented by this Identity Provider.");
  }
  
  ValidationResult validate(Tenant paramTenant, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier);
  
  default ValidationResult validateStart(Tenant paramTenant, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderStartLoginRequest paramIdentityProviderStartLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    throw new UnsupportedOperationException("This method is not implemented by this Identity Provider.");
  }
  
  default OAuthService.LinkingTokenDetails verifyLinkingToken(Tenant paramTenant, Application paramApplication, UUID paramUUID, String paramString) {
    throw new UnsupportedOperationException("This method is not implemented by this Identity Provider.");
  }
  
  public static class ValidationResult extends BaseValidationResult {
    public Application application;
    
    public ExternalIdentifier connectionTestId;
    
    public ExternalIdentifier externalIdentifier;
    
    public BaseIdentityProvider<?> identityProvider;
    
    public Tenant tenant;
    
    public User user;
  }
}
