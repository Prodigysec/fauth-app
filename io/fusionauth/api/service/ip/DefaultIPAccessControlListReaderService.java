package io.fusionauth.api.service.ip;

import com.google.inject.Inject;
import io.fusionauth.api.domain.IPAccessControlListMapper;
import io.fusionauth.domain.IPAccessControlList;
import io.fusionauth.domain.search.IPAccessControlListSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;
import java.util.UUID;

public class DefaultIPAccessControlListReaderService implements IPAccessControlListReaderService {
  private final IPAccessControlListMapper ipAccessControlListMapper;
  
  @Inject
  public DefaultIPAccessControlListReaderService(IPAccessControlListMapper paramIPAccessControlListMapper) {
    this.ipAccessControlListMapper = paramIPAccessControlListMapper;
  }
  
  public List<IPAccessControlList> retrieveAll() {
    return this.ipAccessControlListMapper.retrieveAll();
  }
  
  public IPAccessControlList retrieveById(UUID paramUUID) {
    return this.ipAccessControlListMapper.retrieveById(paramUUID);
  }
  
  public SearchResults<IPAccessControlList> search(IPAccessControlListSearchCriteria paramIPAccessControlListSearchCriteria) {
    int i = this.ipAccessControlListMapper.count(paramIPAccessControlListSearchCriteria);
    List<IPAccessControlList> list = (i > 0) ? this.ipAccessControlListMapper.retrieveByCriteria(paramIPAccessControlListSearchCriteria) : List.of();
    return new SearchResults<>(list, i);
  }
}
