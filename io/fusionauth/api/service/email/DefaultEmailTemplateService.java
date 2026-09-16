package io.fusionauth.api.service.email;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import freemarker.core.ParseException;
import freemarker.template.TemplateException;
import io.fusionauth.api.domain.ApplicationMapper;
import io.fusionauth.api.domain.ConsentMapper;
import io.fusionauth.api.domain.EmailTemplateMapper;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.api.service.system.TenantService;
import io.fusionauth.api.service.useraction.UserActionService;
import io.fusionauth.api.util.InUseValidator;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Consent;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.email.EmailTemplate;
import io.fusionauth.domain.search.EmailTemplateSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.mybatis.guice.transactional.Transactional;
import org.primeframework.email.domain.RawEmailTemplates;
import org.primeframework.email.domain.ValidateResult;
import org.primeframework.email.service.EmailService;

public class DefaultEmailTemplateService implements EmailTemplateService {
  private final ApplicationMapper applicationMapper;
  
  private final ConsentMapper consentMapper;
  
  private final EmailService emailService;
  
  private final EmailTemplateMapper emailTemplateMapper;
  
  private final InUseValidator inUseValidator;
  
  private final TenantReaderService tenantReader;
  
  private final UserActionService userActionService;
  
  @Inject
  public DefaultEmailTemplateService(ApplicationMapper paramApplicationMapper, ConsentMapper paramConsentMapper, EmailTemplateMapper paramEmailTemplateMapper, EmailService paramEmailService, InUseValidator paramInUseValidator, TenantReaderService paramTenantReaderService, UserActionService paramUserActionService) {
    this.applicationMapper = paramApplicationMapper;
    this.consentMapper = paramConsentMapper;
    this.emailTemplateMapper = paramEmailTemplateMapper;
    this.emailService = paramEmailService;
    this.inUseValidator = paramInUseValidator;
    this.tenantReader = paramTenantReaderService;
    this.userActionService = paramUserActionService;
  }
  
  @Transactional
  public void create(EmailTemplate paramEmailTemplate) {
    if (paramEmailTemplate.id == null)
      paramEmailTemplate.id = UUID.randomUUID(); 
    paramEmailTemplate.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramEmailTemplate.lastUpdateInstant = paramEmailTemplate.insertInstant;
    this.emailTemplateMapper.create(paramEmailTemplate);
  }
  
  @Transactional
  public boolean delete(UUID paramUUID) {
    return (this.emailTemplateMapper.delete(paramUUID) == 1);
  }
  
  public List<EmailTemplate> retrieveAll() {
    return this.emailTemplateMapper.retrieveAll();
  }
  
  public EmailTemplate retrieveById(UUID paramUUID) {
    return this.emailTemplateMapper.retrieveById(paramUUID);
  }
  
  public EmailTemplate retrieveByName(String paramString) {
    return this.emailTemplateMapper.retrieveByName(paramString);
  }
  
  public SearchResults<EmailTemplate> search(EmailTemplateSearchCriteria paramEmailTemplateSearchCriteria) {
    int i = this.emailTemplateMapper.retrieveCountByCriteria(paramEmailTemplateSearchCriteria);
    List<EmailTemplate> list = (i > 0) ? this.emailTemplateMapper.retrieveByCriteria(paramEmailTemplateSearchCriteria) : List.of();
    return new SearchResults<>(list, i);
  }
  
  @Transactional
  public boolean update(EmailTemplate paramEmailTemplate) {
    paramEmailTemplate.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    return (this.emailTemplateMapper.update(paramEmailTemplate) == 1);
  }
  
  public boolean usesEmailTemplate(Application paramApplication, UUID paramUUID) {
    return (paramApplication.verificationEmailTemplateId != null && paramApplication.verificationEmailTemplateId.equals(paramUUID));
  }
  
  public boolean usesEmailTemplate(UserAction paramUserAction, UUID paramUUID) {
    return ((paramUserAction.startEmailTemplateId != null && paramUserAction.startEmailTemplateId.equals(paramUUID)) || (paramUserAction.modifyEmailTemplateId != null && paramUserAction.modifyEmailTemplateId
      .equals(paramUUID)) || (paramUserAction.cancelEmailTemplateId != null && paramUserAction.cancelEmailTemplateId
      .equals(paramUUID)) || (paramUserAction.endEmailTemplateId != null && paramUserAction.endEmailTemplateId
      .equals(paramUUID)));
  }
  
  public Errors validate(EmailTemplate paramEmailTemplate, boolean paramBoolean) {
    Validator validator = (new Validator()).notBlank(paramEmailTemplate.name, "emailTemplate.name", new Object[0]).notBlank(paramEmailTemplate.defaultTextTemplate, "emailTemplate.defaultTextTemplate", new Object[0]).notBlank(paramEmailTemplate.defaultHtmlTemplate, "emailTemplate.defaultHtmlTemplate", new Object[0]).notBlank(paramEmailTemplate.defaultSubject, "emailTemplate.defaultSubject", new Object[0]).ifTrue((paramEmailTemplate.fromEmail != null), paramValidator -> paramValidator.email(paramEmailTemplate.fromEmail, "emailTemplate.fromEmail", new Object[0])).ifTrue((paramEmailTemplate.defaultSubject != null), paramValidator -> validateTemplate(paramValidator, paramEmailTemplate.defaultSubject, "emailTemplate.defaultSubject", "emailTemplate.defaultSubject")).ifTrue((paramEmailTemplate.defaultFromName != null), paramValidator -> validateTemplate(paramValidator, paramEmailTemplate.defaultFromName, "emailTemplate.defaultFromName", "emailTemplate.defaultFromName")).ifTrue((paramEmailTemplate.defaultHtmlTemplate != null), paramValidator -> validateTemplate(paramValidator, paramEmailTemplate.defaultHtmlTemplate, "emailTemplate.defaultHtmlTemplate", "emailTemplate.defaultHtmlTemplate")).ifTrue((paramEmailTemplate.defaultTextTemplate != null), paramValidator -> validateTemplate(paramValidator, paramEmailTemplate.defaultTextTemplate, "emailTemplate.defaultTextTemplate", "emailTemplate.defaultTextTemplate")).ifTrue((paramEmailTemplate.localizedFromNames != null), paramValidator -> paramEmailTemplate.localizedFromNames.forEach(())).ifTrue((paramEmailTemplate.localizedHtmlTemplates != null), paramValidator -> paramEmailTemplate.localizedHtmlTemplates.forEach(())).ifTrue((paramEmailTemplate.localizedTextTemplates != null), paramValidator -> paramEmailTemplate.localizedTextTemplates.forEach(())).ifTrue((paramEmailTemplate.localizedSubjects != null), paramValidator -> paramEmailTemplate.localizedSubjects.forEach(()));
    if (paramBoolean) {
      EmailTemplate emailTemplate1 = (paramEmailTemplate.id != null) ? this.emailTemplateMapper.retrieveById(paramEmailTemplate.id) : null;
      EmailTemplate emailTemplate2 = (paramEmailTemplate.name != null) ? this.emailTemplateMapper.retrieveByName(paramEmailTemplate.name) : null;
      validator.notDuplicate(emailTemplate1, "emailTemplateId", new Object[] { paramEmailTemplate.id }).notDuplicate(emailTemplate2, "emailTemplate.name", new Object[] { paramEmailTemplate.name });
    } else {
      validator.notMissing(paramEmailTemplate.id, "emailTemplateId", new Object[0])
        .ifTrue((paramEmailTemplate.name != null), paramValidator -> {
            Objects.requireNonNull(this.emailTemplateMapper);
            paramValidator.notDuplicate(paramEmailTemplate, this.emailTemplateMapper::retrieveExisting, "emailTemplate.name", new Object[] { paramEmailTemplate.name });
          });
    } 
    return validator.done();
  }
  
  public EmailTemplateService.ValidationResult validateDelete(UUID paramUUID) {
    EmailTemplateService.ValidationResult validationResult = new EmailTemplateService.ValidationResult();
    validationResult.existing = this.emailTemplateMapper.retrieveById(paramUUID);
    if (validationResult.existing == null)
      return validationResult; 
    List<Application> list = this.applicationMapper.retrieveAllIgnoreActive(null);
    List<Tenant> list1 = this.tenantReader.retrieveAll();
    List<UserAction> list2 = this.userActionService.retrieveAll();
    validationResult











      
      .errors = (new Validator()).forEach(list, (paramValidator, paramApplication, paramInteger) -> notInUse(paramValidator, paramApplication, paramUUID)).forEach(this.consentMapper.retrieveAllConsents(), (paramValidator, paramConsent, paramInteger) -> paramValidator.validate(()).validate(())).forEach(list1, (paramValidator, paramTenant, paramInteger) -> notInUse(paramValidator, paramTenant, paramUUID)).forEach(list2, (paramValidator, paramUserAction, paramInteger) -> paramValidator.notInUse(!usesEmailTemplate(paramUserAction, paramUUID), "emailTemplateId", new Object[] { paramUUID })).done();
    return validationResult;
  }
  
  private void notInUse(Validator paramValidator, Application paramApplication, UUID paramUUID) {
    for (String str : Application.ApplicationEmailConfiguration.EmailTemplateIdFieldNames)
      this.inUseValidator.validateOptional(paramValidator, paramUUID, "emailTemplateId", paramApplication, "application.emailConfiguration." + str); 
    this.inUseValidator.validateOptional(paramValidator, paramUUID, "emailTemplateId", paramApplication, "application.multiFactorConfiguration.email.templateId");
    this.inUseValidator.validateOptional(paramValidator, paramUUID, "emailTemplateId", paramApplication, "application.verificationEmailTemplateId");
  }
  
  private void notInUse(Validator paramValidator, Tenant paramTenant, UUID paramUUID) {
    for (String str : TenantService.EmailTemplateIdFieldNames)
      this.inUseValidator.validateOptional(paramValidator, paramUUID, "emailTemplateId", paramTenant, "tenant.emailConfiguration." + str); 
    this.inUseValidator.validateOptional(paramValidator, paramUUID, "emailTemplateId", paramTenant, "tenant.familyConfiguration.confirmChildEmailTemplateId");
    this.inUseValidator.validateOptional(paramValidator, paramUUID, "emailTemplateId", paramTenant, "tenant.familyConfiguration.familyRequestEmailTemplateId");
    this.inUseValidator.validateOptional(paramValidator, paramUUID, "emailTemplateId", paramTenant, "tenant.familyConfiguration.parentRegistrationEmailTemplateId");
    this.inUseValidator.validateOptional(paramValidator, paramUUID, "emailTemplateId", paramTenant, "tenant.passwordValidationRules.breachDetection.notifyUserEmailTemplateId");
    this.inUseValidator.validateOptional(paramValidator, paramUUID, "emailTemplateId", paramTenant, "tenant.multiFactorConfiguration.email.templateId");
  }
  
  private void validateTemplate(Validator paramValidator, String paramString1, String paramString2, String paramString3) {
    RawEmailTemplates rawEmailTemplates = new RawEmailTemplates();
    rawEmailTemplates.text = paramString1;
    ValidateResult validateResult = this.emailService.validate(null, rawEmailTemplates, Collections.emptyMap());
    if (!validateResult.wasSuccessful()) {
      String str = validateResult.parseErrors.containsKey("text") ? ((ParseException)validateResult.parseErrors.get("text")).getMessage() : ((TemplateException)validateResult.renderErrors.get("text")).getMessage();
      Errors errors = new Errors();
      errors.addFieldError(paramString2, "[invalidTemplate]" + paramString3, null, new Object[] { str });
      paramValidator.withErrors(errors);
    } 
  }
}
