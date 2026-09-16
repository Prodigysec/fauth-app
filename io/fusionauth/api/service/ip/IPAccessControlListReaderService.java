package io.fusionauth.api.service.ip;

import io.fusionauth.domain.IPAccessControlList;
import io.fusionauth.domain.search.IPAccessControlListSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;
import java.util.UUID;

public interface IPAccessControlListReaderService {
  List<IPAccessControlList> retrieveAll();
  
  IPAccessControlList retrieveById(UUID paramUUID);
  
  SearchResults<IPAccessControlList> search(IPAccessControlListSearchCriteria paramIPAccessControlListSearchCriteria);
}
