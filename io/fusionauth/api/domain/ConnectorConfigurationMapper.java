package io.fusionauth.api.domain;

import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.connector.ConnectorType;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface ConnectorConfigurationMapper {
  void create(BaseConnectorConfiguration paramBaseConnectorConfiguration);
  
  @Delete({"DELETE FROM connectors WHERE id = #{id}"})
  int delete(UUID paramUUID);
  
  List<BaseConnectorConfiguration> retrieveAll();
  
  List<BaseConnectorConfiguration> retrieveAllByType(@Param("type") ConnectorType paramConnectorType);
  
  BaseConnectorConfiguration retrieveById(UUID paramUUID);
  
  BaseConnectorConfiguration retrieveExistingByName(@Param("name") String paramString, @Param("id") UUID paramUUID);
  
  void update(BaseConnectorConfiguration paramBaseConnectorConfiguration);
}
