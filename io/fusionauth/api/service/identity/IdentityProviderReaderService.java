package io.fusionauth.api.service.identity;

import io.fusionauth.domain.IdentityProviderLink;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.search.IdentityProviderSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

public interface IdentityProviderReaderService {
  List<BaseIdentityProvider<?>> retrieveAll(@Nullable UUID paramUUID);
  
  BaseIdentityProvider<?> retrieveById(@Nullable UUID paramUUID1, UUID paramUUID2);
  
  List<BaseIdentityProvider<?>> retrieveByType(@Nullable UUID paramUUID, IdentityProviderType paramIdentityProviderType);
  
  IdentityProviderLink retrieveIdProviderUser(UUID paramUUID1, UUID paramUUID2, String paramString);
  
  SearchResults<BaseIdentityProvider<?>> search(IdentityProviderSearchCriteria paramIdentityProviderSearchCriteria);
}
