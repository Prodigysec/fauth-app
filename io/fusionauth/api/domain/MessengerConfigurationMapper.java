package io.fusionauth.api.domain;

import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import io.fusionauth.domain.messenger.MessengerType;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Param;

public interface MessengerConfigurationMapper {
  void create(BaseMessengerConfiguration paramBaseMessengerConfiguration);
  
  void delete(UUID paramUUID);
  
  List<BaseMessengerConfiguration> retrieveAll();
  
  BaseMessengerConfiguration retrieveById(@Param("id") UUID paramUUID);
  
  List<BaseMessengerConfiguration> retrieveByType(@Param("type") MessengerType paramMessengerType);
  
  BaseMessengerConfiguration retrieveExistingByName(@Param("name") String paramString, @Param("id") UUID paramUUID);
  
  void update(BaseMessengerConfiguration paramBaseMessengerConfiguration);
}
