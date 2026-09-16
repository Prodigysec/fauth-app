package io.fusionauth.api.service.identity;

import com.google.inject.Inject;
import io.fusionauth.api.domain.IdentityProviderLinkMapper;
import io.fusionauth.api.domain.IdentityProviderMapper;
import io.fusionauth.domain.IdentityProviderLink;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.BaseSAMLv2IdentityProvider;
import io.fusionauth.domain.provider.ExternalJWTIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.search.IdentityProviderSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DefaultIdentityProviderReaderService implements IdentityProviderReaderService {
  private final IdentityProviderLinkMapper identityProviderLinkMapper;
  
  private final IdentityProviderMapper identityProviderMapper;
  
  @Inject
  public DefaultIdentityProviderReaderService(IdentityProviderLinkMapper paramIdentityProviderLinkMapper, IdentityProviderMapper paramIdentityProviderMapper) {
    this.identityProviderLinkMapper = paramIdentityProviderLinkMapper;
    this.identityProviderMapper = paramIdentityProviderMapper;
  }
  
  public List<BaseIdentityProvider<?>> retrieveAll(UUID paramUUID) {
    return this.identityProviderMapper.retrieveAll(paramUUID).stream().map(this::populate).toList();
  }
  
  public BaseIdentityProvider<?> retrieveById(UUID paramUUID1, UUID paramUUID2) {
    return populate(this.identityProviderMapper.retrieveById(paramUUID1, paramUUID2));
  }
  
  public List<BaseIdentityProvider<?>> retrieveByType(UUID paramUUID, IdentityProviderType paramIdentityProviderType) {
    return this.identityProviderMapper.retrieveByType(paramUUID, paramIdentityProviderType).stream().map(this::populate).toList();
  }
  
  public IdentityProviderLink retrieveIdProviderUser(UUID paramUUID1, UUID paramUUID2, String paramString) {
    return this.identityProviderLinkMapper.retrieveIdentityProviderLink(paramUUID1, paramUUID2, paramString, null);
  }
  
  public SearchResults<BaseIdentityProvider<?>> search(IdentityProviderSearchCriteria paramIdentityProviderSearchCriteria) {
    int i = this.identityProviderMapper.retrieveCountByCriteria(paramIdentityProviderSearchCriteria);
    if (i == 0)
      return new SearchResults<>(Collections.emptyList(), i); 
    List<BaseIdentityProvider<?>> list = this.identityProviderMapper.retrieveByCriteria(paramIdentityProviderSearchCriteria);
    return new SearchResults<>(list.stream().map(this::populate).toList(), i);
  }
  
  private BaseIdentityProvider<?> populate(BaseIdentityProvider<?> paramBaseIdentityProvider) {
    if (paramBaseIdentityProvider instanceof ExternalJWTIdentityProvider) {
      ExternalJWTIdentityProvider externalJWTIdentityProvider = (ExternalJWTIdentityProvider)paramBaseIdentityProvider;
      externalJWTIdentityProvider.verificationKeyIds
        .stream()
        .findFirst()
        .ifPresentOrElse(paramUUID -> paramExternalJWTIdentityProvider.defaultKeyId = paramUUID, () -> paramExternalJWTIdentityProvider.defaultKeyId = null);
    } else if (paramBaseIdentityProvider instanceof BaseSAMLv2IdentityProvider) {
      BaseSAMLv2IdentityProvider baseSAMLv2IdentityProvider = (BaseSAMLv2IdentityProvider)paramBaseIdentityProvider;
      baseSAMLv2IdentityProvider.verificationKeyIds
        .stream()
        .findFirst()
        .ifPresentOrElse(paramUUID -> paramBaseSAMLv2IdentityProvider.keyId = paramUUID, () -> paramBaseSAMLv2IdentityProvider.keyId = null);
    } 
    return paramBaseIdentityProvider;
  }
}
