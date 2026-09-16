package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import io.fusionauth.api.domain.KeyMapper;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.search.KeySearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DefaultKeyReaderService implements KeyReaderService {
  private final KeyMapper keyMapper;
  
  @Inject
  public DefaultKeyReaderService(KeyMapper paramKeyMapper) {
    this.keyMapper = paramKeyMapper;
  }
  
  public List<Key> retrieveAll() {
    List<Key> list = this.keyMapper.retrieveAll();
    list.forEach(KeyHelper::setupSyntheticFields);
    return list;
  }
  
  public Key retrieveById(UUID paramUUID) {
    Key key = this.keyMapper.retrieveById(paramUUID);
    if (key != null)
      KeyHelper.setupSyntheticFields(key); 
    return key;
  }
  
  public Key retrieveByKid(String paramString) {
    Key key = this.keyMapper.retrieveByKid(paramString);
    if (key != null)
      KeyHelper.setupSyntheticFields(key); 
    return key;
  }
  
  public Key retrieveByName(String paramString) {
    Key key = this.keyMapper.retrieveByName(paramString);
    if (key != null)
      KeyHelper.setupSyntheticFields(key); 
    return key;
  }
  
  public SearchResults<Key> search(KeySearchCriteria paramKeySearchCriteria) {
    int i = this.keyMapper.retrieveCountByCriteria(paramKeySearchCriteria);
    if (i == 0)
      return new SearchResults<>(Collections.emptyList(), i); 
    List<Key> list = this.keyMapper.retrieveByCriteria(paramKeySearchCriteria);
    list.forEach(KeyHelper::setupSyntheticFields);
    return new SearchResults<>(list, i);
  }
}
