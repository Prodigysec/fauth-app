package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import com.inversoft.authentication.api.domain.AuthenticationKeyMapper;
import io.fusionauth.api.domain.APIKeyMapper;
import io.fusionauth.api.domain.api.APIKeyBridge;
import io.fusionauth.api.domain.api.APIKeySearchCriteria;
import io.fusionauth.domain.APIKey;
import io.fusionauth.domain.search.SearchResults;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DefaultAPIKeyReaderService implements APIKeyReaderService {
  private final APIKeyMapper apiKeyMapper;
  
  private final AuthenticationKeyMapper authenticationKeyMapper;
  
  @Inject
  public DefaultAPIKeyReaderService(APIKeyMapper paramAPIKeyMapper, AuthenticationKeyMapper paramAuthenticationKeyMapper) {
    this.apiKeyMapper = paramAPIKeyMapper;
    this.authenticationKeyMapper = paramAuthenticationKeyMapper;
  }
  
  public List<APIKey> retrieveAll(UUID paramUUID) {
    return (List<APIKey>)this.authenticationKeyMapper.retrieveAll(paramUUID)
      .stream()
      .map(APIKeyBridge::convert)
      .collect(Collectors.toList());
  }
  
  public List<UUID> retrieveAllUsingIPAccessControlList(UUID paramUUID) {
    return this.authenticationKeyMapper.retrieveAllUsingIPAccessControlList(paramUUID);
  }
  
  public APIKey retrieveById(UUID paramUUID1, UUID paramUUID2) {
    return APIKeyBridge.convert(this.authenticationKeyMapper.retrieveById(paramUUID1, paramUUID2));
  }
  
  public APIKey retrieveByKey(UUID paramUUID, String paramString) {
    return APIKeyBridge.convert(this.authenticationKeyMapper.retrieveByKey(paramUUID, paramString));
  }
  
  public SearchResults<APIKey> search(APIKeySearchCriteria paramAPIKeySearchCriteria) {
    APIKeySearchCriteria aPIKeySearchCriteria = new APIKeySearchCriteria(paramAPIKeySearchCriteria);
    aPIKeySearchCriteria.secure().prepare();
    int i = this.apiKeyMapper.retrieveCountByCriteria(aPIKeySearchCriteria.nameOrDescription, aPIKeySearchCriteria.key, aPIKeySearchCriteria.tenantId, aPIKeySearchCriteria.keyManager, aPIKeySearchCriteria.expirationStart, aPIKeySearchCriteria.expirationEnd);
    if (i == 0)
      return new SearchResults<>(Collections.emptyList(), 0L); 
    List<APIKey> list = this.apiKeyMapper.retrieveByCriteria(aPIKeySearchCriteria.nameOrDescription, aPIKeySearchCriteria.key, aPIKeySearchCriteria.tenantId, aPIKeySearchCriteria.keyManager, aPIKeySearchCriteria.expirationStart, aPIKeySearchCriteria.expirationEnd, aPIKeySearchCriteria.orderBy, aPIKeySearchCriteria.numberOfResults, aPIKeySearchCriteria.startRow).stream().map(APIKeyBridge::convert).toList();
    return new SearchResults<>(list, i);
  }
}
