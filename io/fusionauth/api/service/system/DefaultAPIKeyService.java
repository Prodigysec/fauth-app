package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import com.inversoft.authentication.api.domain.AuthenticationKey;
import com.inversoft.authentication.api.service.AuthenticationKeyService;
import com.inversoft.error.Error;
import com.inversoft.error.Errors;
import com.inversoft.util.StringTools;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.KickstartRequest;
import io.fusionauth.api.domain.api.APIKeyBridge;
import io.fusionauth.api.domain.guice.FusionAuthInternalAPIKey;
import io.fusionauth.api.service.ip.IPAccessControlListReaderService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.domain.APIKey;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class DefaultAPIKeyService implements APIKeyService {
  private final Map<String, APIKeyService.APIEndpointScope> apiEndpointScopes;
  
  private final AuthenticationKeyService authenticationKeyService;
  
  private final APIKey internalAPIKey;
  
  private final IPAccessControlListReaderService ipAccessControlListReader;
  
  private final ReactorStatusService reactorStatusService;
  
  private final TenantReaderService tenantReader;
  
  @Inject
  public DefaultAPIKeyService(AuthenticationKeyService paramAuthenticationKeyService, IPAccessControlListReaderService paramIPAccessControlListReaderService, ReactorStatusService paramReactorStatusService, TenantReaderService paramTenantReaderService, @FusionAuthInternalAPIKey APIKey paramAPIKey, Map<String, APIKeyService.APIEndpointScope> paramMap) {
    this.apiEndpointScopes = paramMap;
    this.authenticationKeyService = paramAuthenticationKeyService;
    this.ipAccessControlListReader = paramIPAccessControlListReaderService;
    this.reactorStatusService = paramReactorStatusService;
    this.tenantReader = paramTenantReaderService;
    this.internalAPIKey = paramAPIKey;
  }
  
  public void create(APIKey paramAPIKey) {
    AuthenticationKey authenticationKey = APIKeyBridge.convert(paramAPIKey);
    this.authenticationKeyService.create(authenticationKey);
    APIKeyBridge.copyFromTo(authenticationKey, paramAPIKey);
  }
  
  public boolean delete(APIKey paramAPIKey) {
    return this.authenticationKeyService.delete(APIKeyBridge.convert(paramAPIKey));
  }
  
  public boolean deleteById(UUID paramUUID) {
    return this.authenticationKeyService.deleteById(paramUUID);
  }
  
  public boolean deleteByKey(String paramString) {
    return this.authenticationKeyService.deleteByKey(paramString);
  }
  
  public void deleteByTenantId(UUID paramUUID) {
    this.authenticationKeyService.deleteByTenantId(paramUUID);
  }
  
  public void update(APIKey paramAPIKey1, APIKey paramAPIKey2) {
    paramAPIKey2.tenantId = paramAPIKey1.tenantId;
    AuthenticationKey authenticationKey1 = APIKeyBridge.convert(paramAPIKey1);
    AuthenticationKey authenticationKey2 = APIKeyBridge.convert(paramAPIKey2);
    this.authenticationKeyService.update(authenticationKey1, authenticationKey2);
    APIKeyBridge.copyFromTo(authenticationKey2, paramAPIKey2);
  }
  
  public APIKeyService.ValidationResult validateCopy(Tenant paramTenant, boolean paramBoolean, APIKey paramAPIKey1, UUID paramUUID, APIKey paramAPIKey2) {
    APIKeyService.ValidationResult validationResult = new APIKeyService.ValidationResult();
    UUID uUID = paramBoolean ? paramTenant.id : null;
    AuthenticationKeyService.ValidationResult validationResult1 = this.authenticationKeyService.validateCopy(uUID, paramUUID);
    validationResult.errors = mapErrors(validationResult1.errors);
    if (paramAPIKey1.name != null)
      validationResult.errors.add((new Validator())
          
          .notDuplicate(this.authenticationKeyService.retrieveByName(paramAPIKey1.name), "apiKey.name", new Object[0])
          .maxLength(paramAPIKey1.name, 191, "apiKey.name", new Object[] { Integer.valueOf(191) }).done()); 
    validationResult.apiKey = APIKeyBridge.convert(validationResult1.existing);
    if (isInternalKey(validationResult.apiKey)) {
      validationResult.apiKey = null;
      validationResult.errors.addFieldError("sourceKeyId", "[invalid]sourceKeyId", null, new Object[] { paramUUID });
    } 
    if (validationResult.apiKey != null) {
      validationResult.apiKey.id = paramAPIKey1.id;
      validationResult.apiKey.key = paramAPIKey1.key;
      validationResult.apiKey.name = paramAPIKey1.name;
      additionalCreateUpdateValidation(validationResult, paramTenant, paramBoolean, validationResult.apiKey, null);
      if (validationResult.apiKey.keyManager)
        validationResult.errors.addFieldError("keyManager", "[restricted]keyManager", null, new Object[0]); 
    } 
    return validationResult;
  }
  
  public APIKeyService.ValidationResult validateCreate(Tenant paramTenant, boolean paramBoolean, APIKey paramAPIKey1, APIKey paramAPIKey2) {
    APIKeyService.ValidationResult validationResult = validateCreateForAdminUI(paramTenant, paramBoolean, paramAPIKey1);
    if (paramAPIKey1.keyManager)
      validationResult.errors.addFieldError("keyManager", "[restricted]keyManager", null, new Object[0]); 
    checkPermission(validationResult, paramAPIKey2.permissions, paramAPIKey1.permissions);
    return validationResult;
  }
  
  public APIKeyService.ValidationResult validateCreateForAdminUI(Tenant paramTenant, boolean paramBoolean, APIKey paramAPIKey) {
    APIKeyService.ValidationResult validationResult = new APIKeyService.ValidationResult();
    validationResult.apiKey = paramAPIKey;
    UUID uUID = paramBoolean ? paramTenant.id : null;
    AuthenticationKeyService.ValidationResult validationResult1 = this.authenticationKeyService.validateCreate(uUID, APIKeyBridge.convert(paramAPIKey));
    validationResult.errors = mapErrors(validationResult1.errors);
    additionalCreateUpdateValidation(validationResult, paramTenant, paramBoolean, paramAPIKey, null);
    return validationResult;
  }
  
  public APIKeyService.ValidationResult validateCreateForKickstart(List<KickstartRequest.KickstartAPIKey> paramList) {
    APIKeyService.ValidationResult validationResult = new APIKeyService.ValidationResult();
    validationResult





















      
      .errors = (new Validator()).forEach(paramList, (paramValidator, paramKickstartAPIKey, paramInteger) -> {
          boolean bool = (paramKickstartAPIKey.tenantId != null) ? true : false;
          validatePermissionEndpoints(paramValidator, bool, Optional.<KickstartRequest.APIKeyPermissions>ofNullable(paramKickstartAPIKey.permissions).map(()).orElse(null), "apiKeys[%d].permissions".formatted(new Object[] { paramInteger }, ), "apiKeys.permissions").minLengthWithCode(paramKickstartAPIKey.key, 12, "apiKeys[" + paramInteger + "].key", "[tooShort]apiKeys.key", new Object[] { Integer.valueOf(12) }).maxLengthWithCode(paramKickstartAPIKey.key, 191, "apiKeys[" + paramInteger + "].key", "[tooLong]apiKeys.key", new Object[] { Integer.valueOf(191) }).ifTrue((paramKickstartAPIKey.ipAccessControlListId != null), ());
        }).done();
    return validationResult;
  }
  
  public APIKeyService.ValidationResult validateDelete(Tenant paramTenant, boolean paramBoolean, UUID paramUUID, APIKey paramAPIKey) {
    APIKeyService.ValidationResult validationResult = new APIKeyService.ValidationResult();
    UUID uUID = paramBoolean ? paramTenant.id : null;
    AuthenticationKeyService.ValidationResult validationResult1 = this.authenticationKeyService.validateDelete(uUID, paramUUID);
    validationResult.errors = mapErrors(validationResult1.errors);
    validationResult.existing = APIKeyBridge.convert(validationResult1.existing);
    if (isInternalKey(validationResult.existing))
      validationResult.existing = null; 
    if (validationResult.existing != null)
      checkPermission(validationResult, paramAPIKey.permissions, validationResult.existing.permissions); 
    return validationResult;
  }
  
  public APIKeyService.ValidationResult validateGet(Tenant paramTenant, boolean paramBoolean, UUID paramUUID, APIKey paramAPIKey) {
    APIKeyService.ValidationResult validationResult = new APIKeyService.ValidationResult();
    UUID uUID = paramBoolean ? paramTenant.id : null;
    AuthenticationKeyService.ValidationResult validationResult1 = this.authenticationKeyService.validateGet(uUID, paramUUID);
    validationResult.errors = mapErrors(validationResult1.errors);
    validationResult.apiKey = APIKeyBridge.convert(validationResult1.key);
    if (isInternalKey(validationResult.apiKey))
      validationResult.apiKey = null; 
    if (validationResult.apiKey != null)
      checkPermission(validationResult, paramAPIKey.permissions, validationResult.apiKey.permissions); 
    return validationResult;
  }
  
  public APIKeyService.ValidationResult validateUpdate(Tenant paramTenant, boolean paramBoolean, APIKey paramAPIKey1, APIKey paramAPIKey2, UUID paramUUID) {
    APIKeyService.ValidationResult validationResult = validateUpdateForAdminUI(paramTenant, paramBoolean, paramAPIKey1, paramAPIKey2);
    if (validationResult.existing != null && !validationResult.existing.keyManager && paramAPIKey1.keyManager)
      validationResult.errors.addFieldError("keyManager", "[restricted]keyManager", null, new Object[0]); 
    if (paramUUID != null)
      validationResult.errors.addFieldError("sourceKeyId", "[notMissing]sourceKeyId", null, new Object[0]); 
    checkPermission(validationResult, paramAPIKey2.permissions, paramAPIKey1.permissions);
    return validationResult;
  }
  
  public APIKeyService.ValidationResult validateUpdateForAdminUI(Tenant paramTenant, boolean paramBoolean, APIKey paramAPIKey1, APIKey paramAPIKey2) {
    APIKeyService.ValidationResult validationResult = new APIKeyService.ValidationResult();
    UUID uUID = paramBoolean ? paramTenant.id : null;
    AuthenticationKeyService.ValidationResult validationResult1 = this.authenticationKeyService.validateUpdate(uUID, APIKeyBridge.convert(paramAPIKey1));
    validationResult.errors = mapErrors(validationResult1.errors);
    validationResult.existing = APIKeyBridge.convert(validationResult1.existing);
    if (isInternalKey(validationResult.existing)) {
      validationResult.existing = null;
      validationResult.errors.addFieldError("apiKeyId", "[invalid]apiKeyId", null, new Object[] { paramAPIKey1.id });
    } 
    if (validationResult.existing != null) {
      additionalCreateUpdateValidation(validationResult, paramTenant, paramBoolean, paramAPIKey1, validationResult.existing);
      if (validationResult.existing.retrievable != paramAPIKey1.retrievable)
        validationResult.errors.addFieldError("apiKey.retrievable", "[invalid]apiKey.retrievable", null, new Object[0]); 
    } 
    return validationResult;
  }
  
  private void additionalCreateUpdateValidation(APIKeyService.ValidationResult paramValidationResult, Tenant paramTenant, boolean paramBoolean, APIKey paramAPIKey1, APIKey paramAPIKey2) {
    if (paramValidationResult.errors == null)
      paramValidationResult.errors = new Errors(); 
    if (paramAPIKey1.tenantId != null) {
      boolean bool1 = !this.tenantReader.existsById(paramAPIKey1.tenantId) ? true : false;
      boolean bool2 = (paramTenant != null && !paramAPIKey1.tenantId.equals(paramTenant.id)) ? true : false;
      if (bool1 || bool2)
        paramValidationResult.errors.addFieldError("tenantId", "[invalid]tenantId", null, new Object[] { paramAPIKey1.tenantId }); 
    } else if (paramBoolean) {
      paramValidationResult.errors.addFieldError("tenantId", "[required]tenantId", null, new Object[0]);
    } 
    boolean bool = (paramAPIKey1.tenantId != null || (paramAPIKey2 != null && paramAPIKey2.tenantId != null)) ? true : false;
    paramValidationResult.errors.add(validatePermissionEndpoints(new Validator(), bool, 
          Optional.<APIKey.APIKeyPermissions>ofNullable(paramAPIKey1.permissions).map(paramAPIKeyPermissions -> paramAPIKeyPermissions.endpoints).orElse(null), "apiKey.permissions", "apiKey.permissions")


        
        .ifTrue((paramAPIKey1.ipAccessControlListId != null), paramValidator -> paramValidator.validate(()).ensure((this.ipAccessControlListReader.retrieveById(paramAPIKey.ipAccessControlListId) != null), "apiKey.ipAccessControlListId", "[invalid]", new Object[] { paramAPIKey.ipAccessControlListId })).ifFalse(paramAPIKey1.retrievable, paramValidator -> paramValidator.notBlank(paramAPIKey.name, "apiKey.name", new Object[0]))
        
        .done());
  }
  
  private void checkPermission(APIKeyService.ValidationResult paramValidationResult, APIKey.APIKeyPermissions paramAPIKeyPermissions1, APIKey.APIKeyPermissions paramAPIKeyPermissions2) {
    if (paramAPIKeyPermissions1 == null)
      return; 
    paramValidationResult.errors = new Errors();
    if (paramAPIKeyPermissions2 == null) {
      paramValidationResult.errors.addFieldError("apiKeyId", "[unauthorized]apiKeyId", null, new Object[0]);
      return;
    } 
    for (String str : paramAPIKeyPermissions2.endpoints.keySet()) {
      Set set = paramAPIKeyPermissions1.endpoints.get(str);
      if (set == null || !set.containsAll(paramAPIKeyPermissions2.endpoints.get(str))) {
        paramValidationResult.errors.addFieldError("apiKeyId", "[unauthorized]apiKeyId", null, new Object[0]);
        return;
      } 
    } 
  }
  
  private boolean isInternalKey(APIKey paramAPIKey) {
    if (paramAPIKey == null)
      return false; 
    return paramAPIKey.id.equals(this.internalAPIKey.id);
  }
  
  private Errors mapErrors(Errors paramErrors) {
    if (paramErrors == null || paramErrors.empty())
      return paramErrors; 
    Errors errors = new Errors();
    errors.generalErrors.addAll(paramErrors.generalErrors);
    for (String str : paramErrors.fieldErrors.keySet()) {
      ((List)paramErrors.fieldErrors.get(str)).forEach(paramError -> paramError.code = paramError.code.replace("]authenticationKey", "]apiKey"));
      errors.fieldErrors.put(str.replace("authenticationKey", "apiKey"), (List)paramErrors.fieldErrors.get(str));
    } 
    return errors;
  }
  
  private Validator validatePermissionEndpoints(Validator paramValidator, boolean paramBoolean, Map<String, Set<String>> paramMap, String paramString1, String paramString2) {
    if (paramMap == null)
      return paramValidator; 
    paramMap.forEach((paramString3, paramSet) -> {
          paramValidator.ensureWithCode(this.apiEndpointScopes.containsKey(paramString3), paramString1, "[invalidUri]%s".formatted(new Object[] { paramString2 }, ), new Object[] { paramString3 });
          if (this.apiEndpointScopes.containsKey(paramString3) && paramBoolean)
            paramValidator.ensureWithCode((this.apiEndpointScopes.get(paramString3) != APIKeyService.APIEndpointScope.Global), paramString1, "[invalidScope]%s".formatted(new Object[] { paramString2 }, ), new Object[] { paramString3 }); 
        });
    return paramValidator;
  }
}
