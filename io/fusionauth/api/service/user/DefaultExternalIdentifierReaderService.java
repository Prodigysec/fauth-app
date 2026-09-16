package io.fusionauth.api.service.user;

import com.google.inject.Inject;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.ExternalIdentifierMapper;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Tenantable;
import java.util.List;
import java.util.UUID;

public class DefaultExternalIdentifierReaderService implements ExternalIdentifierReaderService {
  private final ApplicationReaderService applicationReader;
  
  private final ExternalIdentifierMapper externalIdentifierMapper;
  
  private final TenantReaderService tenantReader;
  
  private final UserReaderService userReader;
  
  @Inject
  public DefaultExternalIdentifierReaderService(ApplicationReaderService paramApplicationReaderService, ExternalIdentifierMapper paramExternalIdentifierMapper, TenantReaderService paramTenantReaderService, UserReaderService paramUserReaderService) {
    this.applicationReader = paramApplicationReaderService;
    this.externalIdentifierMapper = paramExternalIdentifierMapper;
    this.tenantReader = paramTenantReaderService;
    this.userReader = paramUserReaderService;
  }
  
  public List<ExternalIdentifier> retrieveAllByApplicationId(UUID paramUUID) {
    return this.externalIdentifierMapper.retrieveByApplicationId(paramUUID);
  }
  
  public List<ExternalIdentifier> retrieveAllByUserId(UUID paramUUID, ExternalIdentifier.ExternalIdType... paramVarArgs) {
    return this.externalIdentifierMapper.retrieveByUserIdAndTypes(paramUUID, paramVarArgs);
  }
  
  public ExternalIdentifier retrieveById(String paramString) {
    return this.externalIdentifierMapper.retrieveById(paramString);
  }
  
  public ExternalIdentifier retrieveByIdForUpdate(String paramString) {
    return this.externalIdentifierMapper.retrieveByIdForUpdate(paramString);
  }
  
  public ExternalIdentifier retrieveByType(Tenant paramTenant, String paramString, ExternalIdentifier.ExternalIdType paramExternalIdType) {
    return this.externalIdentifierMapper.retrieveByType((paramTenant != null) ? paramTenant.id : null, paramString, paramExternalIdType);
  }
  
  public ExternalIdentifier retrieveByUserId(UUID paramUUID, ExternalIdentifier.ExternalIdType paramExternalIdType) {
    List<ExternalIdentifier> list = this.externalIdentifierMapper.retrieveByUserIdAndTypes(paramUUID, new ExternalIdentifier.ExternalIdType[] { paramExternalIdType });
    if (list.isEmpty())
      return null; 
    return list.get(0);
  }
  
  public ExternalIdentifier retrieveByUserIdTypeAndApplicationId(UUID paramUUID1, ExternalIdentifier.ExternalIdType paramExternalIdType, UUID paramUUID2) {
    List<ExternalIdentifier> list = this.externalIdentifierMapper.retrieveByUserIdTypeAndApplicationId(paramUUID1, paramUUID2, paramExternalIdType);
    if (list.isEmpty())
      return null; 
    return list.get(0);
  }
  
  public ExternalIdentifierReaderService.ValidationResult validate(Tenant paramTenant, String paramString1, ExternalIdentifier.ExternalIdType paramExternalIdType1, String paramString2, ExternalIdentifier.ExternalIdType paramExternalIdType2) {
    ExternalIdentifierReaderService.ValidationResult validationResult1 = validate(paramTenant, paramString1, new ExternalIdentifier.ExternalIdType[] { paramExternalIdType1 });
    ExternalIdentifierReaderService.ValidationResult validationResult2 = (paramString2 == null) ? new ExternalIdentifierReaderService.ValidationResult() : validate(paramTenant, paramString2, new ExternalIdentifier.ExternalIdType[] { paramExternalIdType2 });
    if (validationResult1.tenant != null && validationResult2.tenant != null && 
      validationResult1.tenant.id.equals(validationResult2.tenant.id))
      validationResult1.id2 = validationResult2.id; 
    return validationResult1;
  }
  
  public ExternalIdentifierReaderService.ValidationResult validate(Tenant paramTenant, String paramString, ExternalIdentifier.ExternalIdType... paramVarArgs) {
    ExternalIdentifierReaderService.ValidationResult validationResult = new ExternalIdentifierReaderService.ValidationResult();
    for (ExternalIdentifier.ExternalIdType externalIdType : paramVarArgs) {
      validationResult.id = retrieveByType(paramTenant, paramString, externalIdType);
      if (validationResult.id != null)
        break; 
    } 
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.id });
    if (validationResult.id != null && validationResult.tenant != null && validationResult.id.isExpired(validationResult.tenant))
      validationResult.id = null; 
    if (validationResult.id != null && validationResult.id.applicationId != null)
      validationResult.application = this.applicationReader.retrieveById((validationResult.tenant != null) ? validationResult.tenant.id : null, validationResult.id.applicationId); 
    return validationResult;
  }
  
  public ExternalIdentifierReaderService.ValidationResult validateEmailVerification(Tenant paramTenant, String paramString1, UUID paramUUID, String paramString2) {
    if (paramUUID != null) {
      ExternalIdentifierReaderService.ValidationResult validationResult = new ExternalIdentifierReaderService.ValidationResult();
      validationResult.user = this.userReader.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID);
      validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.user });
      validationResult

        
        .errors = (new Validator()).notMissing(paramUUID, "userId", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.validObject(paramValidationResult.user, "userId", new Object[] { paramUUID })).done();
      return validationResult;
    } 
    return validateVerificationWithOneTimeCode(paramTenant, paramString1, paramString2, ExternalIdentifier.ExternalIdType.EmailVerification);
  }
  
  public ExternalIdentifierReaderService.ValidationResult validateRegistrationVerification(Tenant paramTenant, String paramString1, String paramString2) {
    return validateVerificationWithOneTimeCode(paramTenant, paramString1, paramString2, ExternalIdentifier.ExternalIdType.RegistrationVerification);
  }
  
  public ExternalIdentifierReaderService.ValidationResult validateResendEmailVerification(Tenant paramTenant, UUID paramUUID, String paramString) {
    ExternalIdentifierReaderService.ValidationResult validationResult = new ExternalIdentifierReaderService.ValidationResult();
    validationResult.application = (paramUUID != null) ? this.applicationReader.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID) : null;
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.application });
    validationResult
      
      .errors = (new Validator()).notBlank(paramString, "email", new Object[0]).ifTrue((paramUUID != null), paramValidator -> paramValidator.validObject(paramValidationResult.application, "applicationId", new Object[] { paramUUID })).done();
    return validationResult;
  }
  
  public ExternalIdentifierReaderService.ValidationResult validateResendRegistrationVerification(Tenant paramTenant, UUID paramUUID, String paramString) {
    ExternalIdentifierReaderService.ValidationResult validationResult = new ExternalIdentifierReaderService.ValidationResult();
    validationResult.application = (paramUUID != null) ? this.applicationReader.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID) : null;
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.application });
    validationResult

      
      .errors = (new Validator()).notMissing(paramUUID, "applicationId", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.valid((paramValidationResult.application != null), "applicationId", new Object[] { paramUUID })).notBlank(paramString, "email", new Object[0]).done();
    return validationResult;
  }
  
  private ExternalIdentifierReaderService.ValidationResult validateVerificationWithOneTimeCode(Tenant paramTenant, String paramString1, String paramString2, ExternalIdentifier.ExternalIdType paramExternalIdType) {
    ExternalIdentifierReaderService.ValidationResult validationResult = new ExternalIdentifierReaderService.ValidationResult();
    validationResult
      
      .errors = (new Validator()).notBlank(paramString2, "verificationId", new Object[0]).done();
    if (!validationResult.errors.empty())
      return validationResult; 
    validationResult = validate(paramTenant, paramString2, new ExternalIdentifier.ExternalIdType[] { paramExternalIdType });
    if (validationResult.id != null) {
      String str = validationResult.id.getAttribute("otp");
      if (str != null)
        if (paramString1 == null) {
          validationResult.errors.addFieldError("oneTimeCode", "[blank]oneTimeCode", null, new Object[0]);
        } else if (!str.equals(paramString1)) {
          validationResult.errors.addFieldError("verificationId", "[invalid]verificationId", null, new Object[0]);
        }  
      if (validationResult.errors.empty() && validationResult.id.userId != null)
        validationResult.user = this.userReader.retrieveById(validationResult.tenant.id, validationResult.id.userId); 
    } 
    return validationResult;
  }
}
