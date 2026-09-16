package io.fusionauth.api.service.system;

import io.fusionauth.api.domain.api.APIKeySearchCriteria;
import io.fusionauth.domain.APIKey;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;
import java.util.UUID;

public interface APIKeyReaderService {
  List<APIKey> retrieveAll(UUID paramUUID);
  
  List<UUID> retrieveAllUsingIPAccessControlList(UUID paramUUID);
  
  APIKey retrieveById(UUID paramUUID1, UUID paramUUID2);
  
  APIKey retrieveByKey(UUID paramUUID, String paramString);
  
  SearchResults<APIKey> search(APIKeySearchCriteria paramAPIKeySearchCriteria);
}
