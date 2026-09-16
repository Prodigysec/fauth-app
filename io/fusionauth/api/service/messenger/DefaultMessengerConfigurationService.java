package io.fusionauth.api.service.messenger;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.MessengerConfigurationMapper;
import io.fusionauth.api.domain.TenantMapper;
import io.fusionauth.domain.message.MessageType;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import io.fusionauth.domain.messenger.MessengerType;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class DefaultMessengerConfigurationService implements MessengerConfigurationService {
  private final MessengerConfigurationMapper messengerConfigurationMapper;
  
  private final TenantMapper tenantMapper;
  
  @Inject
  public DefaultMessengerConfigurationService(MessengerConfigurationMapper paramMessengerConfigurationMapper, TenantMapper paramTenantMapper) {
    this.messengerConfigurationMapper = paramMessengerConfigurationMapper;
    this.tenantMapper = paramTenantMapper;
  }
  
  public void create(BaseMessengerConfiguration paramBaseMessengerConfiguration) {
    if (paramBaseMessengerConfiguration.id == null)
      paramBaseMessengerConfiguration.id = UUID.randomUUID(); 
    paramBaseMessengerConfiguration.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramBaseMessengerConfiguration.lastUpdateInstant = paramBaseMessengerConfiguration.insertInstant;
    this.messengerConfigurationMapper.create(paramBaseMessengerConfiguration);
  }
  
  public void delete(BaseMessengerConfiguration paramBaseMessengerConfiguration) {
    this.messengerConfigurationMapper.delete(paramBaseMessengerConfiguration.id);
  }
  
  public List<BaseMessengerConfiguration> retrieveAll() {
    return this.messengerConfigurationMapper.retrieveAll();
  }
  
  public BaseMessengerConfiguration retrieveById(UUID paramUUID) {
    return this.messengerConfigurationMapper.retrieveById(paramUUID);
  }
  
  public List<BaseMessengerConfiguration> retrieveByType(MessengerType paramMessengerType) {
    return this.messengerConfigurationMapper.retrieveByType(paramMessengerType);
  }
  
  public void update(BaseMessengerConfiguration paramBaseMessengerConfiguration1, BaseMessengerConfiguration paramBaseMessengerConfiguration2) {
    paramBaseMessengerConfiguration2.insertInstant = paramBaseMessengerConfiguration1.insertInstant;
    paramBaseMessengerConfiguration2.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    this.messengerConfigurationMapper.update(paramBaseMessengerConfiguration2);
  }
  
  public MessengerConfigurationService.ValidationResult validateCreate(BaseMessengerConfiguration paramBaseMessengerConfiguration) {
    MessengerConfigurationService.ValidationResult validationResult = new MessengerConfigurationService.ValidationResult();
    validationResult.messenger = paramBaseMessengerConfiguration;
    validationResult.existing = (paramBaseMessengerConfiguration.id == null) ? null : this.messengerConfigurationMapper.retrieveById(paramBaseMessengerConfiguration.id);
    validationResult





      
      .errors = (new Validator()).notDuplicate(validationResult.existing, "messengerId", new Object[] { paramBaseMessengerConfiguration.id }).withErrors(commonValidation(paramBaseMessengerConfiguration)).done();
    return validationResult;
  }
  
  public MessengerConfigurationService.ValidationResult validateDelete(UUID paramUUID) {
    MessengerConfigurationService.ValidationResult validationResult = new MessengerConfigurationService.ValidationResult();
    validationResult.existing = retrieveById(paramUUID);
    if (validationResult.existing == null)
      return validationResult; 
    validationResult




      
      .errors = (new Validator()).emptyWithCode(this.tenantMapper.retrieveTenantIdsUsingMessengerById(paramUUID), "messengerId", "[inUseByTenant]messengerId", paramCollection -> (String)paramCollection.stream().map(UUID::toString).collect(Collectors.joining(", "))).done();
    return validationResult;
  }
  
  public MessengerConfigurationService.ValidationResult validateUpdate(BaseMessengerConfiguration paramBaseMessengerConfiguration) {
    MessengerConfigurationService.ValidationResult validationResult = new MessengerConfigurationService.ValidationResult();
    validationResult.messenger = paramBaseMessengerConfiguration;
    validationResult.existing = (paramBaseMessengerConfiguration.id == null) ? null : this.messengerConfigurationMapper.retrieveById(paramBaseMessengerConfiguration.id);
    List<UUID> list1 = List.of();
    List<UUID> list2 = List.of();
    if (validationResult.existing != null) {
      Set<?> set1 = (validationResult.existing.messageTypes == null) ? Set.of() : validationResult.existing.messageTypes;
      Set<?> set2 = (validationResult.messenger.messageTypes == null) ? Set.of() : validationResult.messenger.messageTypes;
      LinkedHashSet linkedHashSet = new LinkedHashSet(set1);
      linkedHashSet.removeAll(set2);
      if (linkedHashSet.contains(MessageType.SMS))
        list1 = this.tenantMapper.retrieveTenantIdsUsingSMSMessengerById(paramBaseMessengerConfiguration.id); 
      if (linkedHashSet.contains(MessageType.Voice))
        list2 = this.tenantMapper.retrieveTenantIdsUsingVoiceMessengerById(paramBaseMessengerConfiguration.id); 
    } 
    validationResult

















      
      .errors = (new Validator()).ensure((paramBaseMessengerConfiguration.id != null), "messengerId", "[missing]", new Object[0]).withErrors(commonValidation(paramBaseMessengerConfiguration)).emptyWithCode(list1, "messenger.messageTypes", "[inUseByTenant]messenger.messageTypes", new Object[] { MessageType.SMS, list1.stream().map(UUID::toString).collect(Collectors.joining(", ")) }).emptyWithCode(list2, "messenger.messageTypes", "[inUseByTenant]messenger.messageTypes", new Object[] { MessageType.Voice, list2.stream().map(UUID::toString).collect(Collectors.joining(", ")) }).done();
    return validationResult;
  }
  
  private Errors commonValidation(BaseMessengerConfiguration paramBaseMessengerConfiguration) {
    return (new Validator())
      
      .notBlank(paramBaseMessengerConfiguration.name, "messenger.name", new Object[0])
      
      .ifLastCheckHadNoError(paramValidator -> paramValidator.notDuplicate(this.messengerConfigurationMapper.retrieveExistingByName(paramBaseMessengerConfiguration.name, paramBaseMessengerConfiguration.id), "messenger.name", new Object[] { paramBaseMessengerConfiguration.name })).notEmpty(paramBaseMessengerConfiguration.messageTypes, "messenger.messageTypes", new Object[0])
      
      .done();
  }
}
