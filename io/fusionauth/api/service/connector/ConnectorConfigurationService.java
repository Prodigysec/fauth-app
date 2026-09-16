package io.fusionauth.api.service.connector;

import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import java.util.List;
import java.util.UUID;

public interface ConnectorConfigurationService {
  void create(BaseConnectorConfiguration paramBaseConnectorConfiguration);
  
  void delete(UUID paramUUID);
  
  List<BaseConnectorConfiguration> retrieveAll();
  
  BaseConnectorConfiguration retrieveById(UUID paramUUID);
  
  void update(BaseConnectorConfiguration paramBaseConnectorConfiguration1, BaseConnectorConfiguration paramBaseConnectorConfiguration2);
  
  ValidationResult validate(BaseConnectorConfiguration paramBaseConnectorConfiguration, boolean paramBoolean);
  
  ValidationResult validateDelete(UUID paramUUID);
  
  public static class ValidationResult extends BaseValidationResult {
    public BaseConnectorConfiguration connector;
    
    public BaseConnectorConfiguration existing;
  }
}
