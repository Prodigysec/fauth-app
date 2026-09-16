package io.fusionauth.api.service.cache;

import com.google.inject.Inject;
import com.inversoft.cache.BaseCacheLoader;
import com.inversoft.cache.Cache;
import com.inversoft.rest.ProxyInfoSupplier;
import io.fusionauth.api.service.authentication.IdentityProviderAuthenticationService;
import io.fusionauth.api.service.identity.IdentityProviderReaderService;
import io.fusionauth.api.service.identity.OpenIdConnectIdentityProviderHelper;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.provider.OpenIdConnectIdentityProvider;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class IdentityProviderCacheLoader extends BaseCacheLoader<UUID, BaseIdentityProvider<?>> implements Runnable {
  private final IdentityProviderCache cache;
  
  private final IdentityProviderReaderService identityProviderReader;
  
  private final ProxyInfoSupplier proxyInfoSupplier;
  
  @Inject
  public IdentityProviderCacheLoader(IdentityProviderCache paramIdentityProviderCache, IdentityProviderReaderService paramIdentityProviderReaderService, ProxyInfoSupplier paramProxyInfoSupplier, Map<IdentityProviderType, IdentityProviderAuthenticationService> paramMap) {
    super(Collections.emptyList());
    this.cache = paramIdentityProviderCache;
    this.proxyInfoSupplier = paramProxyInfoSupplier;
    this.cache.identityProviderAuthenticationServices = paramMap;
    this.identityProviderReader = paramIdentityProviderReaderService;
  }
  
  public void run() {
    load();
  }
  
  protected void internalLoad(Cache<UUID, BaseIdentityProvider<?>> paramCache) {
    paramCache.replace((Map)retrieveAllAndResolveOpenIDConnectEndpoints()
        .stream()
        
        .filter(BaseIdentityProvider::inUse)
        .collect(Collectors.toMap(paramBaseIdentityProvider -> paramBaseIdentityProvider.id, paramBaseIdentityProvider -> paramBaseIdentityProvider)));
  }
  
  protected Cache<UUID, BaseIdentityProvider<?>> resolveCache() {
    return (Cache<UUID, BaseIdentityProvider<?>>)this.cache;
  }
  
  private List<BaseIdentityProvider<?>> retrieveAllAndResolveOpenIDConnectEndpoints() {
    List<BaseIdentityProvider<?>> list = this.identityProviderReader.retrieveAll(null);
    for (BaseIdentityProvider<?> baseIdentityProvider : list) {
      if (baseIdentityProvider instanceof OpenIdConnectIdentityProvider) {
        OpenIdConnectIdentityProvider openIdConnectIdentityProvider = (OpenIdConnectIdentityProvider)baseIdentityProvider;
        if (openIdConnectIdentityProvider.inUse() && openIdConnectIdentityProvider.oauth2 != null && openIdConnectIdentityProvider.oauth2.issuer != null)
          OpenIdConnectIdentityProviderHelper.resolveOpenIDConnectEndpoints(this.proxyInfoSupplier, openIdConnectIdentityProvider); 
      } 
    } 
    return list;
  }
}
