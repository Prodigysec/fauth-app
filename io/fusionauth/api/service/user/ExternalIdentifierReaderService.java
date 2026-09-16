package io.fusionauth.api.service.user;

import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import java.util.List;
import java.util.UUID;

public interface ExternalIdentifierReaderService {
  List<ExternalIdentifier> retrieveAllByApplicationId(UUID paramUUID);
  
  List<ExternalIdentifier> retrieveAllByUserId(UUID paramUUID, ExternalIdentifier.ExternalIdType... paramVarArgs);
  
  ExternalIdentifier retrieveById(String paramString);
  
  ExternalIdentifier retrieveByIdForUpdate(String paramString);
  
  ExternalIdentifier retrieveByType(Tenant paramTenant, String paramString, ExternalIdentifier.ExternalIdType paramExternalIdType);
  
  ExternalIdentifier retrieveByUserId(UUID paramUUID, ExternalIdentifier.ExternalIdType paramExternalIdType);
  
  ExternalIdentifier retrieveByUserIdTypeAndApplicationId(UUID paramUUID1, ExternalIdentifier.ExternalIdType paramExternalIdType, UUID paramUUID2);
  
  ValidationResult validate(Tenant paramTenant, String paramString, ExternalIdentifier.ExternalIdType... paramVarArgs);
  
  ValidationResult validate(Tenant paramTenant, String paramString1, ExternalIdentifier.ExternalIdType paramExternalIdType1, String paramString2, ExternalIdentifier.ExternalIdType paramExternalIdType2);
  
  ValidationResult validateEmailVerification(Tenant paramTenant, String paramString1, UUID paramUUID, String paramString2);
  
  ValidationResult validateRegistrationVerification(Tenant paramTenant, String paramString1, String paramString2);
  
  ValidationResult validateResendEmailVerification(Tenant paramTenant, UUID paramUUID, String paramString);
  
  ValidationResult validateResendRegistrationVerification(Tenant paramTenant, UUID paramUUID, String paramString);
  
  public static class ValidationResult extends BaseValidationResult {
    public Application application;
    
    public ExternalIdentifier id;
    
    public ExternalIdentifier id2;
    
    public Tenant tenant;
    
    public User user;
  }
}
