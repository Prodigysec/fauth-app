package io.fusionauth.api.domain;

import io.fusionauth.domain.Key;
import io.fusionauth.domain.search.KeySearchCriteria;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Param;

public interface KeyMapper {
  void create(Key paramKey);
  
  int delete(@Param("id") UUID paramUUID);
  
  List<Key> retrieveAll();
  
  List<Key> retrieveByCriteria(KeySearchCriteria paramKeySearchCriteria);
  
  Key retrieveById(@Param("id") UUID paramUUID);
  
  Key retrieveByKid(@Param("kid") String paramString);
  
  Key retrieveByName(@Param("name") String paramString);
  
  int retrieveCountByCriteria(KeySearchCriteria paramKeySearchCriteria);
  
  Key retrieveExisting(@Param("id") UUID paramUUID, @Param("name") String paramString);
  
  int update(Key paramKey);
}
