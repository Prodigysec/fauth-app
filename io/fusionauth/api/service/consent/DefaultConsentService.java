package io.fusionauth.api.service.consent;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ConsentMapper;
import io.fusionauth.api.domain.EmailTemplateMapper;
import io.fusionauth.api.domain.FormMapper;
import io.fusionauth.api.domain.LockType;
import io.fusionauth.api.domain.ZonedDateTimeWrapper;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.api.service.email.EmailProxy;
import io.fusionauth.api.service.lock.LockService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.domain.Consent;
import io.fusionauth.domain.ConsentStatus;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserConsent;
import io.fusionauth.domain.search.ConsentSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.mybatis.guice.transactional.Transactional;

public class DefaultConsentService implements ConsentService {
  private final ConsentMapper consentMapper;
  
  private final EmailProxy emailProxy;
  
  private final EmailTemplateMapper emailTemplateMapper;
  
  private final FormMapper formMapper;
  
  private final LockService lockService;
  
  private final TenantReaderService tenantReader;
  
  private final UserReaderService userReader;
  
  @Inject
  public DefaultConsentService(ConsentMapper paramConsentMapper, EmailProxy paramEmailProxy, EmailTemplateMapper paramEmailTemplateMapper, FormMapper paramFormMapper, LockService paramLockService, TenantReaderService paramTenantReaderService, UserReaderService paramUserReaderService) {
    this.consentMapper = paramConsentMapper;
    this.emailProxy = paramEmailProxy;
    this.emailTemplateMapper = paramEmailTemplateMapper;
    this.formMapper = paramFormMapper;
    this.lockService = paramLockService;
    this.tenantReader = paramTenantReaderService;
    this.userReader = paramUserReaderService;
  }
  
  public void createConsent(Consent paramConsent) {
    if (paramConsent.id == null)
      paramConsent.id = UUID.randomUUID(); 
    paramConsent.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramConsent.lastUpdateInstant = paramConsent.insertInstant;
    this.consentMapper.createConsent(paramConsent);
  }
  
  public void createUserConsent(UserConsent paramUserConsent, boolean paramBoolean) {
    if (paramUserConsent.status == null)
      paramUserConsent.status = ConsentStatus.Active; 
    createUserConsentWithStatus(paramUserConsent, paramBoolean);
  }
  
  public void createUserConsentWithStatus(UserConsent paramUserConsent, boolean paramBoolean) {
    if (paramUserConsent.id == null)
      paramUserConsent.id = UUID.randomUUID(); 
    paramUserConsent.consent = this.consentMapper.retrieveConsentById(paramUserConsent.consentId);
    paramUserConsent.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramUserConsent.lastUpdateInstant = paramUserConsent.insertInstant;
    this.consentMapper.createUserConsent(paramUserConsent);
    boolean bool = paramUserConsent.giverUserId.equals(paramUserConsent.userId);
    if (bool)
      return; 
    if (paramBoolean)
      return; 
    if (paramUserConsent.consent.consentEmailTemplateId != null) {
      User user = this.userReader.retrieveById(null, paramUserConsent.giverUserId);
      Tenant tenant = this.tenantReader.retrieveById(user.tenantId);
      this.emailProxy.sendConsentNotification(tenant, paramUserConsent, user);
    } 
    if (paramUserConsent.consent.emailPlus.enabled) {
      long l1 = ZonedDateTimeWrapper.now(ZoneOffset.UTC).toInstant().toEpochMilli();
      long l2 = l1 + TimeUnit.HOURS.toMillis(paramUserConsent.consent.emailPlus.minimumTimeToSendEmailInHours);
      long l3 = l1 + TimeUnit.HOURS.toMillis(paramUserConsent.consent.emailPlus.maximumTimeToSendEmailInHours);
      long l4 = ThreadLocalRandom.current().nextLong(l2, l3 + 1L);
      this.consentMapper.createEmailPlus(l4, paramUserConsent.id);
    } 
  }
  
  @Transactional
  public boolean deleteConsent(Consent paramConsent) {
    this.consentMapper.deleteUserConsentsByConsentId(paramConsent.id);
    return (this.consentMapper.deleteConsent(paramConsent.id) == 1);
  }
  
  @Transactional
  public void handleEmailPlusFollowup() {
    this.lockService.acquireLock(LockType.EmailPlus);
    ZonedDateTime zonedDateTime = ZonedDateTimeWrapper.now(ZoneOffset.UTC);
    List<ConsentMapper.EmailPlusConsent> list = this.consentMapper.retrieveEmailPlusPastCutoff(zonedDateTime, 10);
    HashMap<Object, Object> hashMap = new HashMap<>();
    for (ConsentMapper.EmailPlusConsent emailPlusConsent : list) {
      UserConsent userConsent = this.consentMapper.retrieveUserConsentById(emailPlusConsent.userConsentId);
      Consent consent = (Consent)hashMap.get(userConsent.consentId);
      if (consent == null) {
        consent = this.consentMapper.retrieveConsentById(userConsent.consentId);
        hashMap.put(consent.id, consent);
      } 
      User user = this.userReader.retrieveById(null, userConsent.giverUserId);
      Tenant tenant = this.tenantReader.retrieveById(user.tenantId);
      boolean bool = this.emailProxy.sendEmailPlusNotification(tenant, user, userConsent);
      if (bool) {
        this.consentMapper.deleteEmailPlus(emailPlusConsent.id);
        continue;
      } 
      EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to email [" + user.getLogin() + "] to complete Email+, see previous email error."));
    } 
  }
  
  public List<Consent> retrieveAllConsents() {
    return this.consentMapper.retrieveAllConsents();
  }
  
  public Consent retrieveConsentById(UUID paramUUID) {
    return this.consentMapper.retrieveConsentById(paramUUID);
  }
  
  public UserConsent retrieveUserConsentById(UUID paramUUID1, UUID paramUUID2) {
    UserConsent userConsent = this.consentMapper.retrieveUserConsentById(paramUUID2);
    if (userConsent == null)
      return null; 
    if (paramUUID1 != null && 
      this.userReader.retrieveById(paramUUID1, userConsent.userId) == null)
      return null; 
    return userConsent;
  }
  
  public List<UserConsent> retrieveUserConsentByUserId(UUID paramUUID1, UUID paramUUID2) {
    if (this.userReader.retrieveById(paramUUID1, paramUUID2) == null)
      throw new NotFoundException(); 
    return this.consentMapper.retrieveUserConsentByUserId(paramUUID2);
  }
  
  public void revokeUserConsent(UUID paramUUID1, UUID paramUUID2) {
    UserConsent userConsent = retrieveUserConsentById(paramUUID1, paramUUID2);
    if (userConsent == null)
      throw new NotFoundException(); 
    userConsent.status = ConsentStatus.Revoked;
    this.consentMapper.updateUserConsent(userConsent);
  }
  
  public SearchResults<Consent> search(ConsentSearchCriteria paramConsentSearchCriteria) {
    int i = this.consentMapper.retrieveConsentCountByCriteria(paramConsentSearchCriteria);
    if (i == 0)
      return new SearchResults<>(Collections.emptyList(), i); 
    List<Consent> list = this.consentMapper.retrieveConsentsByCriteria(paramConsentSearchCriteria);
    return new SearchResults<>(list, i);
  }
  
  public void updateConsent(Consent paramConsent1, Consent paramConsent2) {
    paramConsent1.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    this.consentMapper.updateConsent(paramConsent1);
  }
  
  public UserConsent updateUserConsent(UUID paramUUID, UserConsent paramUserConsent) {
    UserConsent userConsent = this.consentMapper.retrieveUserConsentById(paramUserConsent.id);
    if (userConsent == null)
      return null; 
    if (paramUUID != null && this.userReader.retrieveById(paramUUID, userConsent.userId) == null)
      return null; 
    userConsent.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    userConsent.status = paramUserConsent.status;
    userConsent.values = paramUserConsent.values;
    this.consentMapper.updateUserConsent(userConsent);
    return userConsent;
  }
  
  public ConsentService.ValidationResult validateConsent(Consent paramConsent, boolean paramBoolean) {
    ConsentService.ValidationResult validationResult = new ConsentService.ValidationResult();
    validationResult.existing = paramBoolean ? null : this.consentMapper.retrieveConsentById(paramConsent.id);
    validationResult





































      
      .errors = (new Validator()).ifTrue(paramBoolean, paramValidator -> paramValidator.notDuplicate((paramConsent.id != null) ? this.consentMapper.retrieveConsentById(paramConsent.id) : null, "consentId", new Object[] { paramConsent.id }).notDuplicate((paramConsent.name != null) ? this.consentMapper.retrieveConsentByName(paramConsent.name) : null, "consent.name", new Object[] { paramConsent.name })).ifFalse(paramBoolean, paramValidator -> paramValidator.notMissing(paramConsent.id, "consentId", new Object[0]).ifLastCheckHadNoError(())).notBlank(paramConsent.name, "consent.name", new Object[0]).ifTrue((paramConsent.emailPlus != null && paramConsent.emailPlus.enabled), paramValidator -> paramValidator.notMissing(paramConsent.consentEmailTemplateId, "consent.consentEmailTemplateId", new Object[0]).notMissing(paramConsent.emailPlus.emailTemplateId, "consent.emailPlus.emailTemplateId", new Object[0]).ifLastCheckHadNoError(()).ensure((paramConsent.emailPlus.minimumTimeToSendEmailInHours > 0), "consent.emailPlus.minimumTimeToSendEmailInHours", "[invalid]", new Object[0]).ensure((paramConsent.emailPlus.maximumTimeToSendEmailInHours > 0), "consent.emailPlus.maximumTimeToSendEmailInHours", "[invalid]", new Object[0]).ifTrue((paramConsent.emailPlus.minimumTimeToSendEmailInHours > 0 && paramConsent.emailPlus.maximumTimeToSendEmailInHours > 0), ())).notMissing(paramConsent.defaultMinimumAgeForSelfConsent, "consent.defaultMinimumAgeForSelfConsent", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.ensure((paramConsent.defaultMinimumAgeForSelfConsent.intValue() >= 0), "consent.defaultMinimumAgeForSelfConsent", "[invalid]", new Object[0])).ifTrue((paramConsent.consentEmailTemplateId != null), paramValidator -> paramValidator.validObject(this.emailTemplateMapper.retrieveById(paramConsent.consentEmailTemplateId), "consent.consentEmailTemplateId", new Object[] { paramConsent.consentEmailTemplateId })).done();
    return validationResult;
  }
  
  public ConsentService.ValidationResult validateDelete(UUID paramUUID) {
    ConsentService.ValidationResult validationResult = new ConsentService.ValidationResult();
    validationResult.existing = this.consentMapper.retrieveConsentById(paramUUID);
    if (validationResult.existing != null) {
      List<UUID> list = this.formMapper.retrieveFieldIdByConsentId(paramUUID);
      validationResult
        
        .errors = (new Validator()).ensure((list.size() == 0), "consentId", "[inUse]", new Object[] { paramUUID, list.stream().map(UUID::toString).collect(Collectors.joining(", ")) }).done();
    } 
    return validationResult;
  }
  
  public Errors validateUserConsent(UUID paramUUID, UserConsent paramUserConsent, boolean paramBoolean) {
    User user1 = (paramUserConsent.userId != null) ? this.userReader.retrieveById(paramUUID, paramUserConsent.userId) : null;
    User user2 = (paramUserConsent.giverUserId != null) ? this.userReader.retrieveById(paramUUID, paramUserConsent.giverUserId) : null;
    Consent consent = this.consentMapper.retrieveConsentById(paramUserConsent.consentId);
    return (new Validator())

      
      .ifTrue(paramBoolean, paramValidator -> paramValidator.notDuplicate((paramUserConsent.id != null) ? this.consentMapper.retrieveUserConsentById(paramUserConsent.id) : null, "userConsentId", new Object[] { paramUserConsent.id }).notMissing(paramUserConsent.consentId, "userConsent.consentId", new Object[0]).ifLastCheckHadNoError(()).notMissing(paramUserConsent.userId, "userConsent.userId", new Object[0]).ifLastCheckHadNoError(()).notMissing(paramUserConsent.giverUserId, "userConsent.giverUserId", new Object[0]).ifLastCheckHadNoError(()).ifTrue((paramUser1 != null && paramUser2 != null), ()))





















      
      .ifFalse(paramBoolean, paramValidator -> paramValidator.notMissing(paramUserConsent.id, "userConsentId", new Object[0]).notMissing(paramUserConsent.status, "userConsent.status", new Object[0]))




      
      .ifTrue((consent != null), paramValidator -> paramValidator.ifTrue((paramUserConsent.values.size() > 1), ()).ensure(paramConsent.values.containsAll(paramUserConsent.values), "userConsent.values", "[invalid]", new Object[] { String.join(", ", (Iterable)paramUserConsent.values), String.join(", ", (Iterable)paramConsent.values) })).done();
  }
}
