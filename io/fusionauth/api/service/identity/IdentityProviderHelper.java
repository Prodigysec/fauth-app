package io.fusionauth.api.service.identity;

import com.google.inject.Inject;
import com.inversoft.rest.ProxyInfoSupplier;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.api.service.cache.IdentityProviderCache;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.OpenIdConnectIdentityProvider;
import java.util.UUID;
import javax.annotation.Nullable;

public class IdentityProviderHelper {
  @Inject
  private static ProxyInfoSupplier proxyInfoSupplier;
  
  public static ResolvedIdentityProviderResult resolveIdentityProvider(UUID paramUUID, @Nullable String paramString, @Nullable Tenant paramTenant, ExternalIdentifierReaderService paramExternalIdentifierReaderService, IdentityProviderCache paramIdentityProviderCache, IdentityProviderReaderService paramIdentityProviderReaderService) {
    ResolvedIdentityProviderResult resolvedIdentityProviderResult = new ResolvedIdentityProviderResult();
    if (paramUUID == null) {
      resolvedIdentityProviderResult.errors.addFieldError("identityProviderId", "[missing]identityProviderId", null, new Object[0]);
      return resolvedIdentityProviderResult;
    } 
    if (paramString != null) {
      ExternalIdentifierReaderService.ValidationResult validationResult = paramExternalIdentifierReaderService.validate(paramTenant, paramString, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.IdentityProviderConnectionTest });
      if (validationResult.id == null) {
        resolvedIdentityProviderResult.errors.addFieldError("connectionTestId", "[invalid]connectionTestId", null, new Object[0]);
        return resolvedIdentityProviderResult;
      } 
      if (!paramUUID.toString().equals(validationResult.id.getAttribute("identityProviderId"))) {
        resolvedIdentityProviderResult.errors.addGeneralError("[InvalidIdentityProviderId]", null, new Object[0]);
        return resolvedIdentityProviderResult;
      } 
      resolvedIdentityProviderResult.connectionTestId = validationResult.id;
    } 
    BaseIdentityProvider<?> baseIdentityProvider = (BaseIdentityProvider)paramIdentityProviderCache.get(paramUUID);
    if (baseIdentityProvider == null && resolvedIdentityProviderResult.connectionTestId != null) {
      baseIdentityProvider = paramIdentityProviderReaderService.retrieveById(null, paramUUID);
      if (baseIdentityProvider instanceof OpenIdConnectIdentityProvider) {
        OpenIdConnectIdentityProvider openIdConnectIdentityProvider = (OpenIdConnectIdentityProvider)baseIdentityProvider;
        if (openIdConnectIdentityProvider.oauth2 != null && openIdConnectIdentityProvider.oauth2.issuer != null)
          OpenIdConnectIdentityProviderHelper.resolveOpenIDConnectEndpoints(proxyInfoSupplier, openIdConnectIdentityProvider); 
      } 
    } 
    resolvedIdentityProviderResult.identityProvider = baseIdentityProvider;
    return resolvedIdentityProviderResult;
  }
  
  public static class ResolvedIdentityProviderResult extends BaseValidationResult {
    public ExternalIdentifier connectionTestId;
    
    public BaseIdentityProvider<?> identityProvider;
  }
}
