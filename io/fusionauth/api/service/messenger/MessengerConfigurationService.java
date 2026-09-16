package io.fusionauth.api.service.messenger;

import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import io.fusionauth.domain.messenger.MessengerType;
import java.util.List;
import java.util.UUID;

public interface MessengerConfigurationService {
  void create(BaseMessengerConfiguration paramBaseMessengerConfiguration);
  
  void delete(BaseMessengerConfiguration paramBaseMessengerConfiguration);
  
  List<BaseMessengerConfiguration> retrieveAll();
  
  BaseMessengerConfiguration retrieveById(UUID paramUUID);
  
  List<BaseMessengerConfiguration> retrieveByType(MessengerType paramMessengerType);
  
  void update(BaseMessengerConfiguration paramBaseMessengerConfiguration1, BaseMessengerConfiguration paramBaseMessengerConfiguration2);
  
  ValidationResult validateCreate(BaseMessengerConfiguration paramBaseMessengerConfiguration);
  
  ValidationResult validateDelete(UUID paramUUID);
  
  ValidationResult validateUpdate(BaseMessengerConfiguration paramBaseMessengerConfiguration);
  
  public static class ValidationResult extends BaseValidationResult {
    public BaseMessengerConfiguration existing;
    
    public BaseMessengerConfiguration messenger;
  }
}
