package io.fusionauth.api.service.message;

import com.inversoft.error.Errors;
import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.domain.message.MessageTemplate;
import java.util.List;
import java.util.UUID;

public interface MessageTemplateService {
  void create(MessageTemplate paramMessageTemplate);
  
  boolean delete(MessageTemplate paramMessageTemplate);
  
  List<MessageTemplate> retrieveAll();
  
  MessageTemplate retrieveById(UUID paramUUID);
  
  MessageTemplate retrieveByName(String paramString);
  
  boolean update(MessageTemplate paramMessageTemplate1, MessageTemplate paramMessageTemplate2);
  
  ValidationResult validateCreate(MessageTemplate paramMessageTemplate);
  
  ValidationResult validateDelete(UUID paramUUID);
  
  Errors validateTemplate(String paramString);
  
  ValidationResult validateUpdate(MessageTemplate paramMessageTemplate);
  
  public static class ValidationResult extends BaseValidationResult {
    public MessageTemplate existing;
    
    public MessageTemplate template;
  }
}
