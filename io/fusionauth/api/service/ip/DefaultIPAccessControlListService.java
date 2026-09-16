package io.fusionauth.api.service.ip;

import com.google.inject.Inject;
import com.inversoft.cache.CacheNotifier;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ApplicationMapper;
import io.fusionauth.api.domain.IPAccessControlListMapper;
import io.fusionauth.api.domain.TenantMapper;
import io.fusionauth.api.service.system.APIKeyReaderService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.api.util.InUseValidator;
import io.fusionauth.api.util.NetworkTools;
import io.fusionauth.domain.APIKey;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.IPAccessControlEntry;
import io.fusionauth.domain.IPAccessControlList;
import io.fusionauth.domain.Tenant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class DefaultIPAccessControlListService implements IPAccessControlListService {
  private final APIKeyReaderService apiKeyReader;
  
  private final ApplicationReaderService applicationReader;
  
  private final CacheNotifier cacheNotifier;
  
  private final InUseValidator inUseValidator;
  
  private final IPAccessControlListMapper ipAccessControlListMapper;
  
  private final TenantReaderService tenantReader;
  
  @Inject
  public DefaultIPAccessControlListService(APIKeyReaderService paramAPIKeyReaderService, ApplicationReaderService paramApplicationReaderService, CacheNotifier paramCacheNotifier, InUseValidator paramInUseValidator, IPAccessControlListMapper paramIPAccessControlListMapper, TenantReaderService paramTenantReaderService) {
    this.apiKeyReader = paramAPIKeyReaderService;
    this.applicationReader = paramApplicationReaderService;
    this.cacheNotifier = paramCacheNotifier;
    this.inUseValidator = paramInUseValidator;
    this.ipAccessControlListMapper = paramIPAccessControlListMapper;
    this.tenantReader = paramTenantReaderService;
  }
  
  public void create(IPAccessControlList paramIPAccessControlList) {
    if (paramIPAccessControlList.id == null)
      paramIPAccessControlList.id = UUID.randomUUID(); 
    paramIPAccessControlList.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramIPAccessControlList.lastUpdateInstant = paramIPAccessControlList.insertInstant;
    this.ipAccessControlListMapper.create(paramIPAccessControlList);
    this.cacheNotifier.reload("IPAddressRangeRules");
  }
  
  public void delete(IPAccessControlList paramIPAccessControlList) {
    this.ipAccessControlListMapper.delete(paramIPAccessControlList.id);
    this.cacheNotifier.reload("IPAddressRangeRules");
  }
  
  public void update(IPAccessControlList paramIPAccessControlList1, IPAccessControlList paramIPAccessControlList2) {
    paramIPAccessControlList2.id = paramIPAccessControlList1.id;
    paramIPAccessControlList2.insertInstant = paramIPAccessControlList1.insertInstant;
    paramIPAccessControlList2.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    this.ipAccessControlListMapper.update(paramIPAccessControlList2);
    this.cacheNotifier.reload("IPAddressRangeRules");
  }
  
  public IPAccessControlListService.ValidationResult validateCreate(IPAccessControlList paramIPAccessControlList) {
    IPAccessControlListService.ValidationResult validationResult = new IPAccessControlListService.ValidationResult();
    validationResult



      
      .errors = (new Validator()).ifTrue((paramIPAccessControlList.id != null), paramValidator -> paramValidator.notDuplicate(this.ipAccessControlListMapper.retrieveById(paramIPAccessControlList.id), "ipAccessControlListId", new Object[] { paramIPAccessControlList.id })).withErrors(commonValidation(paramIPAccessControlList)).done();
    return validationResult;
  }
  
  public IPAccessControlListService.ValidationResult validateDelete(UUID paramUUID) {
    IPAccessControlListService.ValidationResult validationResult = new IPAccessControlListService.ValidationResult();
    validationResult.existing = this.ipAccessControlListMapper.retrieveById(paramUUID);
    if (validationResult.existing == null)
      return validationResult; 
    List<UUID> list = this.apiKeyReader.retrieveAllUsingIPAccessControlList(paramUUID);
    List<TenantMapper.TenantId> list1 = this.tenantReader.retrieveAllUsingIPAccessControlList(paramUUID);
    List<ApplicationMapper.ApplicationId> list2 = this.applicationReader.retrieveAllUsingIPAccessControlList(paramUUID);
    validationResult











      
      .errors = (new Validator()).forEach(list, (paramValidator, paramUUID2, paramInteger) -> paramValidator.validate(())).forEach(list1, (paramValidator, paramTenantId, paramInteger) -> paramValidator.validate(())).forEach(list2, (paramValidator, paramApplicationId, paramInteger) -> paramValidator.validate(())).done();
    return validationResult;
  }
  
  public IPAccessControlListService.ValidationResult validateUpdate(IPAccessControlList paramIPAccessControlList) {
    IPAccessControlListService.ValidationResult validationResult = new IPAccessControlListService.ValidationResult();
    validationResult.existing = (paramIPAccessControlList.id != null) ? this.ipAccessControlListMapper.retrieveById(paramIPAccessControlList.id) : null;
    if (validationResult.existing == null)
      return validationResult; 
    validationResult

      
      .errors = (new Validator()).withErrors(commonValidation(paramIPAccessControlList)).done();
    return validationResult;
  }
  
  private Errors commonValidation(IPAccessControlList paramIPAccessControlList) {
    return (new Validator())
      
      .notBlank(paramIPAccessControlList.name, "ipAccessControlList.name", new Object[0])

      
      .ifLastCheckHadNoError(paramValidator -> paramValidator.notDuplicate(this.ipAccessControlListMapper.retrieveExisting(paramIPAccessControlList.id, paramIPAccessControlList.name), "ipAccessControlList.name", new Object[] { paramIPAccessControlList.name })).ensure((paramIPAccessControlList.entries.size() >= 2), "ipAccessControlList.entries", "[invalid]", new Object[0])

      
      .ifNoFieldErrors("ipAccessControlList.entries", paramValidator -> paramValidator.ensure((paramIPAccessControlList.entries.stream().filter(()).findFirst().orElse(null) != null), "ipAccessControlList.entries", "[missingDefault]", new Object[0]))






      
      .forEach(paramIPAccessControlList.entries, (paramValidator, paramIPAccessControlEntry, paramInteger) -> paramValidator.ifFalse("*".equals(paramIPAccessControlEntry.startIPAddress), ()))
























      
      .done();
  }
  
  private boolean isDuplicate(List<IPAccessControlEntry> paramList, int paramInt, IPAccessControlEntry paramIPAccessControlEntry) {
    for (byte b = 0; b < paramInt; b++) {
      IPAccessControlEntry iPAccessControlEntry = paramList.get(b);
      if (!iPAccessControlEntry.startIPAddress.equals(""))
        if (paramIPAccessControlEntry.startIPAddress.equals(iPAccessControlEntry.startIPAddress) && paramIPAccessControlEntry.endIPAddress.equals(iPAccessControlEntry.endIPAddress))
          return true;  
    } 
    return false;
  }
  
  private boolean isValidRange(IPAccessControlEntry paramIPAccessControlEntry) {
    return (NetworkTools.convertIPv4ToLong(paramIPAccessControlEntry.startIPAddress) <= NetworkTools.convertIPv4ToLong(paramIPAccessControlEntry.endIPAddress));
  }
}
