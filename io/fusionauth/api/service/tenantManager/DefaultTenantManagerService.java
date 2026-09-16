package io.fusionauth.api.service.tenantManager;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ApplicationMapper;
import io.fusionauth.api.domain.TenantManagerMapper;
import io.fusionauth.api.service.form.FormService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.form.Form;
import io.fusionauth.domain.form.FormField;
import io.fusionauth.domain.form.FormStep;
import io.fusionauth.domain.form.FormType;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.tenantManager.TenantManagerApplicationConfiguration;
import io.fusionauth.domain.tenantManager.TenantManagerConfiguration;
import io.fusionauth.domain.tenantManager.TenantManagerIdentityProviderTypeConfiguration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.mybatis.guice.transactional.Transactional;

public class DefaultTenantManagerService implements TenantManagerService {
  public static final String TenantManagerSource = "Tenant Manager";
  
  private final ApplicationMapper applicationMapper;
  
  private final FormService formService;
  
  private final TenantManagerMapper mapper;
  
  @Inject
  public DefaultTenantManagerService(ApplicationMapper paramApplicationMapper, FormService paramFormService, TenantManagerMapper paramTenantManagerMapper) {
    this.applicationMapper = paramApplicationMapper;
    this.formService = paramFormService;
    this.mapper = paramTenantManagerMapper;
  }
  
  @Transactional
  public void createTenantManagerIdentityProviderTypeConfiguration(TenantManagerIdentityProviderTypeConfiguration paramTenantManagerIdentityProviderTypeConfiguration) {
    paramTenantManagerIdentityProviderTypeConfiguration.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramTenantManagerIdentityProviderTypeConfiguration.lastUpdateInstant = paramTenantManagerIdentityProviderTypeConfiguration.insertInstant;
    this.mapper.createTenantManagerIdentityProviderTypeConfiguration(paramTenantManagerIdentityProviderTypeConfiguration);
  }
  
  @Transactional
  public void deleteTenantManagerIdentityProviderTypeConfiguration(TenantManagerIdentityProviderTypeConfiguration paramTenantManagerIdentityProviderTypeConfiguration) {
    this.mapper.deleteTenantManagerIdentityProviderTypeConfiguration(paramTenantManagerIdentityProviderTypeConfiguration.type);
  }
  
  public TenantManagerConfiguration retrieve() {
    TenantManagerConfiguration tenantManagerConfiguration = this.mapper.retrieve();
    tenantManagerConfiguration.identityProviderTypeConfigurations.putAll((Map<? extends String, ? extends TenantManagerIdentityProviderTypeConfiguration>)this.mapper
        .retrieveTenantManagerIdentityProviderTypeConfigurations()
        .stream()
        .collect(Collectors.toMap(paramTenantManagerIdentityProviderTypeConfiguration -> paramTenantManagerIdentityProviderTypeConfiguration.type.toString(), paramTenantManagerIdentityProviderTypeConfiguration -> paramTenantManagerIdentityProviderTypeConfiguration)));
    tenantManagerConfiguration.applicationConfigurations.addAll(this.mapper.retrieveTenantManagerApplicationConfigurations());
    return tenantManagerConfiguration;
  }
  
  public TenantManagerIdentityProviderTypeConfiguration retrieveTenantManagerIdentityProviderTypeConfiguration(IdentityProviderType paramIdentityProviderType) {
    return this.mapper.retrieveTenantManagerIdentityProviderTypeConfiguration(paramIdentityProviderType);
  }
  
  @Transactional
  public void update(TenantManagerConfiguration paramTenantManagerConfiguration) {
    TenantManagerConfiguration tenantManagerConfiguration = this.mapper.retrieve();
    paramTenantManagerConfiguration.insertInstant = tenantManagerConfiguration.insertInstant;
    paramTenantManagerConfiguration.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    this.mapper.update(paramTenantManagerConfiguration);
    this.mapper.deleteAllTenantManagerApplicationConfigurations();
    if (!paramTenantManagerConfiguration.applicationConfigurations.isEmpty())
      this.mapper.createTenantManagerApplicationConfigurations(paramTenantManagerConfiguration.applicationConfigurations); 
  }
  
  @Transactional
  public void updateTenantManagerIdentityProviderTypeConfiguration(TenantManagerIdentityProviderTypeConfiguration paramTenantManagerIdentityProviderTypeConfiguration1, TenantManagerIdentityProviderTypeConfiguration paramTenantManagerIdentityProviderTypeConfiguration2) {
    paramTenantManagerIdentityProviderTypeConfiguration2.insertInstant = paramTenantManagerIdentityProviderTypeConfiguration1.insertInstant;
    paramTenantManagerIdentityProviderTypeConfiguration2.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    this.mapper.updateTenantManagerIdentityProviderTypeConfiguration(paramTenantManagerIdentityProviderTypeConfiguration2);
  }
  
  public Errors validate(TenantManagerConfiguration paramTenantManagerConfiguration) {
    HashSet hashSet = new HashSet();
    return (new Validator())
      
      .ifTrue((paramTenantManagerConfiguration.brandName != null), paramValidator -> paramValidator.maxLength(paramTenantManagerConfiguration.brandName, 255, "tenantManagerConfiguration.brandName", new Object[0]))


      
      .ifTrue((paramTenantManagerConfiguration.attributeFormId != null), paramValidator -> paramValidator.holdMyBeer(this.formService.retrieveById(paramTenantManagerConfiguration.attributeFormId)).validObject(paramValidator.barkeep(), "tenantManagerConfiguration.attributeFormId", new Object[] { paramTenantManagerConfiguration.attributeFormId }).ifLastCheckHadNoError(())).forEach(paramTenantManagerConfiguration.applicationConfigurations, (paramValidator, paramTenantManagerApplicationConfiguration, paramInteger) -> paramValidator.holdMyBeer(this.applicationMapper.retrieveByIdIgnoreActive(null, paramTenantManagerApplicationConfiguration.applicationId)).validObjectWithCode(paramValidator.barkeep(), "tenantManagerConfiguration.applicationConfigurations[%d].applicationId".formatted(new Object[] { paramInteger }, ), "[invalid]tenantManagerConfiguration.applicationConfigurations.applicationId", new Object[] { paramTenantManagerApplicationConfiguration.applicationId }).ifLastCheckHadNoError(()).ensureWithCode(paramSet.add(paramTenantManagerApplicationConfiguration.applicationId), "tenantManagerConfiguration.applicationConfigurations[%d].applicationId".formatted(new Object[] { paramInteger }, ), "[duplicate]tenantManagerConfiguration.applicationConfigurations.applicationId", new Object[] { paramTenantManagerApplicationConfiguration.applicationId })).done();
  }
  
  public TenantManagerService.TenantManagerIdentityProviderTypeConfigurationValidationResult validateTenantManagerIdentityProviderTypeConfigurationCreate(TenantManagerIdentityProviderTypeConfiguration paramTenantManagerIdentityProviderTypeConfiguration) {
    TenantManagerService.TenantManagerIdentityProviderTypeConfigurationValidationResult tenantManagerIdentityProviderTypeConfigurationValidationResult = new TenantManagerService.TenantManagerIdentityProviderTypeConfigurationValidationResult(paramTenantManagerIdentityProviderTypeConfiguration);
    TenantManagerIdentityProviderTypeConfiguration tenantManagerIdentityProviderTypeConfiguration = this.mapper.retrieveTenantManagerIdentityProviderTypeConfiguration(paramTenantManagerIdentityProviderTypeConfiguration.type);
    tenantManagerIdentityProviderTypeConfigurationValidationResult

      
      .errors = (new Validator()).withErrors(commonTenantManagerIdentityProviderTypeConfigurationValidation(paramTenantManagerIdentityProviderTypeConfiguration)).notDuplicate(tenantManagerIdentityProviderTypeConfiguration, "type", new Object[] { paramTenantManagerIdentityProviderTypeConfiguration.type }).done();
    return tenantManagerIdentityProviderTypeConfigurationValidationResult;
  }
  
  public TenantManagerService.TenantManagerIdentityProviderTypeConfigurationValidationResult validateTenantManagerIdentityProviderTypeConfigurationDelete(IdentityProviderType paramIdentityProviderType) {
    TenantManagerService.TenantManagerIdentityProviderTypeConfigurationValidationResult tenantManagerIdentityProviderTypeConfigurationValidationResult = new TenantManagerService.TenantManagerIdentityProviderTypeConfigurationValidationResult();
    tenantManagerIdentityProviderTypeConfigurationValidationResult.existing = (paramIdentityProviderType != null) ? this.mapper.retrieveTenantManagerIdentityProviderTypeConfiguration(paramIdentityProviderType) : null;
    tenantManagerIdentityProviderTypeConfigurationValidationResult.errors = (new Validator()).notMissing(paramIdentityProviderType, "type", new Object[0]).done();
    return tenantManagerIdentityProviderTypeConfigurationValidationResult;
  }
  
  public TenantManagerService.TenantManagerIdentityProviderTypeConfigurationValidationResult validateTenantManagerIdentityProviderTypeConfigurationUpdate(TenantManagerIdentityProviderTypeConfiguration paramTenantManagerIdentityProviderTypeConfiguration) {
    TenantManagerService.TenantManagerIdentityProviderTypeConfigurationValidationResult tenantManagerIdentityProviderTypeConfigurationValidationResult = new TenantManagerService.TenantManagerIdentityProviderTypeConfigurationValidationResult(paramTenantManagerIdentityProviderTypeConfiguration);
    tenantManagerIdentityProviderTypeConfigurationValidationResult.existing = this.mapper.retrieveTenantManagerIdentityProviderTypeConfiguration(paramTenantManagerIdentityProviderTypeConfiguration.type);
    tenantManagerIdentityProviderTypeConfigurationValidationResult.errors = commonTenantManagerIdentityProviderTypeConfigurationValidation(paramTenantManagerIdentityProviderTypeConfiguration);
    return tenantManagerIdentityProviderTypeConfigurationValidationResult;
  }
  
  private Errors commonTenantManagerIdentityProviderTypeConfigurationValidation(TenantManagerIdentityProviderTypeConfiguration paramTenantManagerIdentityProviderTypeConfiguration) {
    pruneTenantManagerIdentityProviderTypeConfigurationDefaultAttributeMapping(paramTenantManagerIdentityProviderTypeConfiguration);
    return (new Validator())
      .notMissing(paramTenantManagerIdentityProviderTypeConfiguration.type, "type", new Object[0])
      .ifLastCheckHadNoError(paramValidator -> paramValidator.ensureWithCode(SupportedTenantManagerIdentityProviderTypes.contains(paramTenantManagerIdentityProviderTypeConfiguration.type), "type", "[unsupported]type", new Object[] { paramTenantManagerIdentityProviderTypeConfiguration.type, String.join(", ", SupportedTenantManagerIdentityProviderTypes.stream().map(Enum::toString).toList()) })).notMissing(paramTenantManagerIdentityProviderTypeConfiguration.linkingStrategy, "typeConfiguration.linkingStrategy", new Object[0])
      .ifLastCheckHadNoError(paramValidator -> paramValidator.ensureWithCode(ValidTenantManagerIdentityProviderLinkingStrategies.contains(paramTenantManagerIdentityProviderTypeConfiguration.linkingStrategy), "typeConfiguration.linkingStrategy", "[invalid]typeConfiguration.linkingStrategy", new Object[] { paramTenantManagerIdentityProviderTypeConfiguration.linkingStrategy, String.join(", ", ValidTenantManagerIdentityProviderLinkingStrategies.stream().map(Enum::toString).toList()) })).done();
  }
  
  private void pruneTenantManagerIdentityProviderTypeConfigurationDefaultAttributeMapping(TenantManagerIdentityProviderTypeConfiguration paramTenantManagerIdentityProviderTypeConfiguration) {
    TenantManagerConfiguration tenantManagerConfiguration = this.mapper.retrieve();
    if (tenantManagerConfiguration.attributeFormId == null) {
      paramTenantManagerIdentityProviderTypeConfiguration.defaultAttributeMappings.clear();
    } else if (!paramTenantManagerIdentityProviderTypeConfiguration.defaultAttributeMappings.isEmpty()) {
      Form form = this.formService.retrieveById(tenantManagerConfiguration.attributeFormId);
      Set<?> set = (Set)form.steps.stream().flatMap(paramFormStep -> paramFormStep.fieldObjects.stream()).map(paramFormField -> paramFormField.key).filter(paramString -> !RestrictedAttributeMappingKeys.contains(paramString)).collect(Collectors.toSet());
      paramTenantManagerIdentityProviderTypeConfiguration.defaultAttributeMappings.keySet().retainAll(set);
    } 
  }
}
