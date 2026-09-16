package io.fusionauth.api.service.family;

import com.google.inject.Inject;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.FamilyMapper;
import io.fusionauth.api.domain.LockType;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.api.service.email.EmailProxy;
import io.fusionauth.api.service.lock.LockService;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.domain.Family;
import io.fusionauth.domain.FamilyMember;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Tenantable;
import io.fusionauth.domain.User;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.mybatis.guice.transactional.Transactional;

public class DefaultFamilyService implements FamilyService {
  private final EmailProxy emailProxy;
  
  private final FamilyMapper familyMapper;
  
  private final LockService lockService;
  
  private final TenantReaderService tenantReader;
  
  private final UserReaderService userReader;
  
  private final UserService userService;
  
  @Inject
  public DefaultFamilyService(EmailProxy paramEmailProxy, FamilyMapper paramFamilyMapper, LockService paramLockService, TenantReaderService paramTenantReaderService, UserReaderService paramUserReaderService, UserService paramUserService) {
    this.emailProxy = paramEmailProxy;
    this.familyMapper = paramFamilyMapper;
    this.lockService = paramLockService;
    this.tenantReader = paramTenantReaderService;
    this.userReader = paramUserReaderService;
    this.userService = paramUserService;
  }
  
  public boolean removeMember(UUID paramUUID1, UUID paramUUID2, UUID paramUUID3) {
    if (paramUUID1 != null && 
      this.userReader.retrieveById(paramUUID1, paramUUID3) == null)
      throw new NotFoundException(); 
    int i = this.familyMapper.deleteMember(paramUUID2, paramUUID3);
    return (i > 0);
  }
  
  public Family retrieveById(UUID paramUUID1, UUID paramUUID2) {
    List<FamilyMember> list = this.familyMapper.retrieveFamilyById(paramUUID2);
    if (list.isEmpty())
      return null; 
    if (paramUUID1 != null) {
      UUID uUID = ((FamilyMember)list.get(0)).userId;
      if (this.userReader.retrieveById(paramUUID1, uUID) == null)
        return null; 
    } 
    return new Family(paramUUID2, list);
  }
  
  public List<Family> retrieveByUserId(UUID paramUUID1, UUID paramUUID2) {
    List<Family> list = this.familyMapper.retrieveByUserId(paramUUID2);
    if (paramUUID1 != null)
      if (this.userReader.retrieveById(paramUUID1, paramUUID2) == null)
        throw new NotFoundException();  
    return list;
  }
  
  public void sendFamilyRequestEmail(Tenant paramTenant, String paramString) {
    UUID uUID = paramTenant.familyConfiguration.familyRequestEmailTemplateId;
    if (!paramTenant.familyConfiguration.enabled || uUID == null)
      return; 
    User user = this.userReader.retrieveByLoginId(paramTenant.id, paramString, List.of(IdentityType.email));
    this.emailProxy.sendFamilyRequest(paramTenant, paramString, user);
  }
  
  @Transactional
  public Family upsertMember(UUID paramUUID, Family paramFamily, User paramUser, FamilyMember paramFamilyMember) {
    this.lockService.acquireLock(LockType.Family);
    if (paramUUID == null)
      paramUUID = UUID.randomUUID(); 
    if (paramFamilyMember.role == FamilyMember.FamilyRole.Adult && paramFamily == null) {
      paramFamilyMember.owner = true;
    } else if (paramFamilyMember.role == FamilyMember.FamilyRole.Child || paramFamilyMember.role == FamilyMember.FamilyRole.Teen) {
      paramFamilyMember.owner = false;
    } 
    paramFamilyMember.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramFamilyMember.lastUpdateInstant = paramFamilyMember.insertInstant;
    this.familyMapper.upsertMember(paramUUID, paramFamilyMember);
    if ((paramFamilyMember.role == FamilyMember.FamilyRole.Child || paramFamilyMember.role == FamilyMember.FamilyRole.Teen) && paramUser.parentEmail != null)
      this.userService.removeParentEmail(paramUser); 
    List<FamilyMember> list = this.familyMapper.retrieveFamilyById(paramUUID);
    return new Family(paramUUID, list);
  }
  
  public FamilyService.ValidationResult validateCreate(Tenant paramTenant, UUID paramUUID, FamilyMember paramFamilyMember) {
    FamilyService.ValidationResult validationResult = loadUserAndFamily(paramTenant, paramUUID, paramFamilyMember.userId);
    List<FamilyMember> list = (paramFamilyMember.userId != null) ? this.familyMapper.retrieveMemberByUserId(paramFamilyMember.userId) : null;
    Integer integer = (validationResult.tenant != null) ? Integer.valueOf(validationResult.tenant.familyConfiguration.minimumOwnerAge) : null;
    validationResult






      
      .errors = (new Validator()).notMissing(paramFamilyMember.role, "familyMember.role", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.ensure((paramFamilyMember.role == FamilyMember.FamilyRole.Adult), "familyMember.role", "[invalid]", new Object[0])).notMissing(paramFamilyMember.userId, "familyMember.userId", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.validObject(paramValidationResult.user, "familyMember.userId", new Object[] { paramFamilyMember.userId })).notDuplicate(validationResult.family, "familyId", new Object[] { paramUUID }).ifTrue((validationResult.user != null && list != null && integer != null), paramValidator -> paramValidator.ensure(paramList.isEmpty(), "familyMember.userId", "[alreadyInFamily]", new Object[] { paramFamilyMember.userId }).ensure((paramValidationResult.user.getAge() >= paramInteger.intValue()), "familyMember.userId", "[tooYoung]", new Object[] { paramValidationResult.user.id, paramInteger, Integer.valueOf(paramValidationResult.user.getAge()) })).done();
    return validationResult;
  }
  
  public FamilyService.ValidationResult validateUpdate(Tenant paramTenant, UUID paramUUID, FamilyMember paramFamilyMember) {
    FamilyService.ValidationResult validationResult = loadUserAndFamily(paramTenant, paramUUID, paramFamilyMember.userId);
    List<FamilyMember> list = (paramFamilyMember.userId != null) ? this.familyMapper.retrieveMemberByUserId(paramFamilyMember.userId) : null;
    validationResult




      
      .errors = (new Validator()).notMissing(paramUUID, "familyId", new Object[0]).notMissing(paramFamilyMember.role, "familyMember.role", new Object[0]).notMissing(paramFamilyMember.userId, "familyMember.userId", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.validObject(paramValidationResult.user, "familyMember.userId", new Object[] { paramFamilyMember.userId })).ifTrue((paramFamilyMember.role == FamilyMember.FamilyRole.Adult && list != null), paramValidator -> paramValidator.ensure(paramList.isEmpty(), "familyMember.userId", "[alreadyInFamily]", new Object[] { paramFamilyMember.userId })).done();
    return validationResult;
  }
  
  FamilyService.ValidationResult loadUserAndFamily(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2) {
    FamilyService.ValidationResult validationResult = new FamilyService.ValidationResult();
    validationResult.user = (paramUUID2 != null) ? this.userReader.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID2) : null;
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.user });
    validationResult.family = (paramUUID1 != null) ? retrieveById((validationResult.tenant != null) ? validationResult.tenant.id : null, paramUUID1) : null;
    return validationResult;
  }
}
