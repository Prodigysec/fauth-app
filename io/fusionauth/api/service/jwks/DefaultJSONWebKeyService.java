package io.fusionauth.api.service.jwks;

import com.google.inject.Inject;
import io.fusionauth.api.domain.IdentityProviderMapper;
import io.fusionauth.domain.jwks.JSONWebKeyInfoProvider;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import java.util.ArrayList;
import java.util.List;

public class DefaultJSONWebKeyService implements JSONWebKeyService {
  private final IdentityProviderMapper identityProviderMapper;
  
  @Inject
  public DefaultJSONWebKeyService(IdentityProviderMapper paramIdentityProviderMapper) {
    this.identityProviderMapper = paramIdentityProviderMapper;
  }
  
  public List<JSONWebKeyInfoProvider> retrieveAllProviders() {
    ArrayList<JSONWebKeyInfoProvider> arrayList = new ArrayList();
    this.identityProviderMapper.retrieveAll(null)
      .stream()
      .filter(paramBaseIdentityProvider -> paramBaseIdentityProvider instanceof JSONWebKeyInfoProvider)
      .filter(BaseIdentityProvider::inUse)
      .forEach(paramBaseIdentityProvider -> paramList.add((JSONWebKeyInfoProvider)paramBaseIdentityProvider));
    return arrayList;
  }
}
