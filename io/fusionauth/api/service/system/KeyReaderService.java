package io.fusionauth.api.service.system;

import io.fusionauth.domain.Key;
import io.fusionauth.domain.search.KeySearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;
import java.util.UUID;

public interface KeyReaderService {
  List<Key> retrieveAll();
  
  Key retrieveById(UUID paramUUID);
  
  Key retrieveByKid(String paramString);
  
  Key retrieveByName(String paramString);
  
  SearchResults<Key> search(KeySearchCriteria paramKeySearchCriteria);
}
