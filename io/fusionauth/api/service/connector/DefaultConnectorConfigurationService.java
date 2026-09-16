package io.fusionauth.api.service.connector;

import com.google.inject.Inject;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ConnectorConfigurationMapper;
import io.fusionauth.api.domain.TenantMapper;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DefaultConnectorConfigurationService implements ConnectorConfigurationService {
  private final ConnectorConfigurationMapper connectorConfigurationMapper;
  
  private final TenantMapper tenantMapper;
  
  @Inject
  public DefaultConnectorConfigurationService(ConnectorConfigurationMapper paramConnectorConfigurationMapper, TenantMapper paramTenantMapper) {
    this.connectorConfigurationMapper = paramConnectorConfigurationMapper;
    this.tenantMapper = paramTenantMapper;
  }
  
  public void create(BaseConnectorConfiguration paramBaseConnectorConfiguration) {
    if (paramBaseConnectorConfiguration.id == null)
      paramBaseConnectorConfiguration.id = UUID.randomUUID(); 
    paramBaseConnectorConfiguration.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramBaseConnectorConfiguration.lastUpdateInstant = paramBaseConnectorConfiguration.insertInstant;
    this.connectorConfigurationMapper.create(paramBaseConnectorConfiguration);
  }
  
  public void delete(UUID paramUUID) {
    this.connectorConfigurationMapper.delete(paramUUID);
  }
  
  public List<BaseConnectorConfiguration> retrieveAll() {
    return this.connectorConfigurationMapper.retrieveAll();
  }
  
  public BaseConnectorConfiguration retrieveById(UUID paramUUID) {
    return this.connectorConfigurationMapper.retrieveById(paramUUID);
  }
  
  public void update(BaseConnectorConfiguration paramBaseConnectorConfiguration1, BaseConnectorConfiguration paramBaseConnectorConfiguration2) {
    paramBaseConnectorConfiguration2.insertInstant = paramBaseConnectorConfiguration1.insertInstant;
    paramBaseConnectorConfiguration2.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    if (paramBaseConnectorConfiguration1.id == BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID) {
      paramBaseConnectorConfiguration1.name = paramBaseConnectorConfiguration2.name;
      this.connectorConfigurationMapper.update(paramBaseConnectorConfiguration1);
    } else {
      this.connectorConfigurationMapper.update(paramBaseConnectorConfiguration2);
    } 
  }
  
  public ConnectorConfigurationService.ValidationResult validate(BaseConnectorConfiguration paramBaseConnectorConfiguration, boolean paramBoolean) {
    ConnectorConfigurationService.ValidationResult validationResult = new ConnectorConfigurationService.ValidationResult();
    validationResult.connector = paramBaseConnectorConfiguration;
    validationResult.existing = (paramBaseConnectorConfiguration.id == null) ? null : this.connectorConfigurationMapper.retrieveById(paramBaseConnectorConfiguration.id);
    BaseConnectorConfiguration baseConnectorConfiguration = (paramBaseConnectorConfiguration.name == null) ? null : this.connectorConfigurationMapper.retrieveExistingByName(paramBaseConnectorConfiguration.name, paramBaseConnectorConfiguration.id);
    validationResult







      
      .errors = (new Validator()).ifTrue(paramBoolean, paramValidator -> paramValidator.notDuplicate(paramValidationResult.existing, "connectorId", new Object[] { paramBaseConnectorConfiguration.id })).ifFalse(paramBoolean, paramValidator -> paramValidator.ensure((paramBaseConnectorConfiguration.id != null), "connectorId", "[missing]", new Object[0])).notBlank(paramBaseConnectorConfiguration.name, "connector.name", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.notDuplicate(paramBaseConnectorConfiguration1, "connector.name", new Object[] { paramBaseConnectorConfiguration2.name })).done();
    return validationResult;
  }
  
  public ConnectorConfigurationService.ValidationResult validateDelete(UUID paramUUID) {
    ConnectorConfigurationService.ValidationResult validationResult = new ConnectorConfigurationService.ValidationResult();
    validationResult.existing = this.connectorConfigurationMapper.retrieveById(paramUUID);
    validationResult



      
      .errors = (new Validator()).emptyWithCode(this.tenantMapper.retrieveTenantIdByConnectorId(paramUUID), "connectorId", "[inUseByTenant]connectorId", paramCollection -> (String)paramCollection.stream().map(UUID::toString).sorted().collect(Collectors.joining(", "))).done();
    return validationResult;
  }
}
