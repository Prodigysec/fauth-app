package io.fusionauth.api.service.message;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.MessageTemplateMapper;
import io.fusionauth.api.service.application.ApplicationService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.api.service.system.TenantService;
import io.fusionauth.api.util.InUseValidator;
import io.fusionauth.api.util.MapperTools;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.message.MessageTemplate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DefaultMessageTemplateService implements MessageTemplateService {
  private final ApplicationReaderService applicationReader;
  
  private final InUseValidator inUseValidator;
  
  private final MessageTemplateMapper messageTemplateMapper;
  
  private final TenantReaderService tenantReader;
  
  @Inject
  public DefaultMessageTemplateService(ApplicationReaderService paramApplicationReaderService, InUseValidator paramInUseValidator, MessageTemplateMapper paramMessageTemplateMapper, TenantReaderService paramTenantReaderService) {
    this.applicationReader = paramApplicationReaderService;
    this.inUseValidator = paramInUseValidator;
    this.messageTemplateMapper = paramMessageTemplateMapper;
    this.tenantReader = paramTenantReaderService;
  }
  
  public void create(MessageTemplate paramMessageTemplate) {
    if (paramMessageTemplate.id == null)
      paramMessageTemplate.id = UUID.randomUUID(); 
    paramMessageTemplate.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramMessageTemplate.lastUpdateInstant = paramMessageTemplate.insertInstant;
    this.messageTemplateMapper.create(paramMessageTemplate);
  }
  
  public boolean delete(MessageTemplate paramMessageTemplate) {
    return (this.messageTemplateMapper.delete(paramMessageTemplate) == 1);
  }
  
  public List<MessageTemplate> retrieveAll() {
    return this.messageTemplateMapper.retrieveAll();
  }
  
  public MessageTemplate retrieveById(UUID paramUUID) {
    return this.messageTemplateMapper.retrieveById(paramUUID);
  }
  
  public MessageTemplate retrieveByName(String paramString) {
    return this.messageTemplateMapper.retrieveByName(paramString);
  }
  
  public boolean update(MessageTemplate paramMessageTemplate1, MessageTemplate paramMessageTemplate2) {
    paramMessageTemplate2.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    return (this.messageTemplateMapper.update(paramMessageTemplate2) == 1);
  }
  
  public MessageTemplateService.ValidationResult validateCreate(MessageTemplate paramMessageTemplate) {
    MessageTemplateService.ValidationResult validationResult = new MessageTemplateService.ValidationResult();
    validationResult.existing = (paramMessageTemplate.id == null) ? null : this.messageTemplateMapper.retrieveById(paramMessageTemplate.id);
    validationResult







      
      .errors = (new Validator()).withErrors(commonValidation(paramMessageTemplate)).notDuplicate(this.messageTemplateMapper.retrieveByName(paramMessageTemplate.name), "messageTemplate.name", new Object[] { paramMessageTemplate.name }).notDuplicate(validationResult.existing, "messageTemplateId", new Object[] { paramMessageTemplate.id }).done();
    return validationResult;
  }
  
  public MessageTemplateService.ValidationResult validateDelete(UUID paramUUID) {
    MessageTemplateService.ValidationResult validationResult = new MessageTemplateService.ValidationResult();
    validationResult.existing = this.messageTemplateMapper.retrieveById(paramUUID);
    List<Tenant> list = this.tenantReader.retrieveAll();
    List<Application> list1 = this.applicationReader.retrieveAll(null, Collections.emptySet());
    validationResult



      
      .errors = (new Validator()).forEach(list1, (paramValidator, paramApplication, paramInteger) -> notInUse(paramValidator, paramApplication, paramUUID)).forEach(list, (paramValidator, paramTenant, paramInteger) -> notInUse(paramValidator, paramTenant, paramUUID)).done();
    return validationResult;
  }
  
  public Errors validateTemplate(String paramString) {
    return new Errors();
  }
  
  public MessageTemplateService.ValidationResult validateUpdate(MessageTemplate paramMessageTemplate) {
    MessageTemplateService.ValidationResult validationResult = new MessageTemplateService.ValidationResult();
    validationResult.existing = (paramMessageTemplate.id != null) ? this.messageTemplateMapper.retrieveById(paramMessageTemplate.id) : null;
    validationResult










      
      .errors = (new Validator()).withErrors(commonValidation(paramMessageTemplate)).notMissing(paramMessageTemplate.id, "messageTemplateId", new Object[0]).notMissing(validationResult.existing, "messageTemplate", new Object[] { paramMessageTemplate.id }).ifTrue((paramMessageTemplate.name != null && validationResult.existing != null), paramValidator -> paramValidator.holdMyBeer(this.messageTemplateMapper.retrieveByName(paramMessageTemplate.name)).ensure((paramValidator.barkeep(MessageTemplate.class) == null || paramValidationResult.existing.id.equals(((MessageTemplate)paramValidator.barkeep(MessageTemplate.class)).id)), "messageTemplate.name", "[duplicate]", new Object[] { paramValidationResult.existing.name })).done();
    return validationResult;
  }
  
  private Errors commonValidation(MessageTemplate paramMessageTemplate) {
    return (new Validator()).notBlank(paramMessageTemplate.name, "messageTemplate.name", new Object[0])
      .maxLength(paramMessageTemplate.name, MapperTools.MaximumIndexedColumnLength, "messageTemplate.name", new Object[] { Integer.valueOf(MapperTools.MaximumIndexedColumnLength) }).done();
  }
  
  private void notInUse(Validator paramValidator, Application paramApplication, UUID paramUUID) {
    for (String str : ApplicationService.PhoneTemplateIdFieldNames)
      this.inUseValidator.validateOptional(paramValidator, paramUUID, "messageTemplateId", paramApplication, "application.phoneConfiguration." + str); 
    this.inUseValidator.validateOptional(paramValidator, paramUUID, "messageTemplateId", paramApplication, "application.multiFactorConfiguration.sms.templateId");
    this.inUseValidator.validateOptional(paramValidator, paramUUID, "messageTemplateId", paramApplication, "application.multiFactorConfiguration.voice.templateId");
  }
  
  private void notInUse(Validator paramValidator, Tenant paramTenant, UUID paramUUID) {
    for (String str : TenantService.PhoneTemplateIdFieldNames)
      this.inUseValidator.validateOptional(paramValidator, paramUUID, "messageTemplateId", paramTenant, "tenant.phoneConfiguration." + str); 
    this.inUseValidator.validateOptional(paramValidator, paramUUID, "messageTemplateId", paramTenant, "tenant.multiFactorConfiguration.sms.templateId");
    this.inUseValidator.validateOptional(paramValidator, paramUUID, "messageTemplateId", paramTenant, "tenant.multiFactorConfiguration.voice.templateId");
  }
}
