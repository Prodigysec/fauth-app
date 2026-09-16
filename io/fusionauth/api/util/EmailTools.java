package io.fusionauth.api.util;

import com.inversoft.error.Errors;
import freemarker.core.ParseException;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import io.fusionauth.api.domain.api.reactor.BreachResult;
import io.fusionauth.domain.BreachedPasswordStatus;
import io.fusionauth.domain.ExternalIdentifierConfiguration;
import io.fusionauth.domain.SecureGeneratorConfiguration;
import io.fusionauth.domain.SecureGeneratorType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.email.Attachment;
import io.fusionauth.domain.email.Email;
import io.fusionauth.domain.email.EmailAddress;
import io.fusionauth.domain.email.EmailTemplate;
import io.fusionauth.domain.event.UserActionEvent;
import io.fusionauth.domain.event.UserActionPhase;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.primeframework.email.domain.Attachment;
import org.primeframework.email.domain.BaseResult;
import org.primeframework.email.domain.Email;
import org.primeframework.email.domain.EmailAddress;
import org.primeframework.email.domain.RawEmailTemplates;

public class EmailTools {
  public static final Map<String, String> FIELD_NAME_MAPPING = new HashMap<>();
  
  public static final Map<String, Object> MOCK_PARAMETERS = new HashMap<>();
  
  public static Email convert(Email paramEmail) {
    if (paramEmail == null)
      return null; 
    Email email = new Email();
    paramEmail.attachments.forEach(paramAttachment -> paramEmail.attachments.add(new Attachment(paramAttachment.name, paramAttachment.mime, paramAttachment.attachment)));
    paramEmail.bcc.forEach(paramEmailAddress -> paramEmail.bcc.add(new EmailAddress(paramEmailAddress.address, paramEmailAddress.display)));
    paramEmail.cc.forEach(paramEmailAddress -> paramEmail.cc.add(new EmailAddress(paramEmailAddress.address, paramEmailAddress.display)));
    if (paramEmail.from != null)
      email.from = new EmailAddress(paramEmail.from.address, paramEmail.from.display); 
    email.html = paramEmail.html;
    if (paramEmail.replyTo != null)
      email.replyTo = new EmailAddress(paramEmail.replyTo.address, paramEmail.replyTo.display); 
    email.subject = paramEmail.subject;
    email.text = paramEmail.text;
    paramEmail.to.forEach(paramEmailAddress -> paramEmail.to.add(new EmailAddress(paramEmailAddress.address, paramEmailAddress.display)));
    return email;
  }
  
  public static String getEmailDomain(String paramString) {
    if (paramString != null) {
      int i = paramString.indexOf('@');
      if (i != -1)
        paramString = paramString.substring(i + 1); 
      paramString = paramString.toLowerCase().trim();
    } 
    return paramString;
  }
  
  public static String loadTemplate(Class<?> paramClass, String paramString) {
    return URITools.loadResourceByURI(paramClass, paramString);
  }
  
  public static RawEmailTemplates toRawEmailTemplates(EmailTemplate paramEmailTemplate, Locale paramLocale) {
    RawEmailTemplates rawEmailTemplates = new RawEmailTemplates();
    rawEmailTemplates.fromDisplay = paramEmailTemplate.defaultFromName;
    rawEmailTemplates.html = paramEmailTemplate.defaultHtmlTemplate;
    rawEmailTemplates.subject = paramEmailTemplate.defaultSubject;
    rawEmailTemplates.text = paramEmailTemplate.defaultTextTemplate;
    if (paramLocale != null) {
      if (paramEmailTemplate.localizedFromNames.containsKey(paramLocale))
        rawEmailTemplates.fromDisplay = paramEmailTemplate.localizedFromNames.get(paramLocale); 
      if (paramEmailTemplate.localizedHtmlTemplates.containsKey(paramLocale))
        rawEmailTemplates.html = paramEmailTemplate.localizedHtmlTemplates.get(paramLocale); 
      if (paramEmailTemplate.localizedSubjects.containsKey(paramLocale))
        rawEmailTemplates.subject = paramEmailTemplate.localizedSubjects.get(paramLocale); 
      if (paramEmailTemplate.localizedTextTemplates.containsKey(paramLocale))
        rawEmailTemplates.text = paramEmailTemplate.localizedTextTemplates.get(paramLocale); 
    } 
    return rawEmailTemplates;
  }
  
  public static Errors translateErrors(BaseResult paramBaseResult) {
    Errors errors = new Errors();
    if (paramBaseResult.renderErrors.isEmpty() && paramBaseResult.parseErrors.isEmpty())
      return errors; 
    paramBaseResult.parseErrors.forEach((paramString, paramParseException) -> paramErrors.addFieldError(FIELD_NAME_MAPPING.get(paramString), "[invalidTemplate]" + (String)FIELD_NAME_MAPPING.get(paramString), null, new Object[] { paramParseException.getMessage() }));
    paramBaseResult.renderErrors.forEach((paramString, paramTemplateException) -> paramErrors.addFieldError(FIELD_NAME_MAPPING.get(paramString), "[invalidTemplate]" + (String)FIELD_NAME_MAPPING.get(paramString), null, new Object[] { paramTemplateException.getMessage() }));
    return errors;
  }
  
  private static UserActionEvent buildMockEvent(UUID paramUUID) {
    UserActionEvent userActionEvent = new UserActionEvent();
    userActionEvent.applicationIds.add(UUID.randomUUID());
    userActionEvent.action = "{{event.action}}";
    userActionEvent.actionId = UUID.randomUUID();
    userActionEvent.actioneeUserId = paramUUID;
    userActionEvent.actionerUserId = UUID.randomUUID();
    userActionEvent.comment = "{{event.comment}}";
    userActionEvent.createInstant = ZonedDateTime.now(ZoneOffset.UTC).minusWeeks(1L);
    userActionEvent.expiry = ZonedDateTime.now(ZoneOffset.UTC).plusWeeks(1L);
    userActionEvent.localizedAction = "{{event.localizedAction}}";
    userActionEvent.localizedDuration = "{{event.localizedDuration}}";
    userActionEvent.localizedOption = "{{event.localizedOption}}";
    userActionEvent.localizedReason = "{{event.localizedReason}}";
    userActionEvent.notifyUser = true;
    userActionEvent.option = "{{event.option}}";
    userActionEvent.emailedUser = false;
    userActionEvent.phase = UserActionPhase.start;
    userActionEvent.reason = "{{event.reason}}";
    userActionEvent.reasonCode = "{{event.reasonCode}}";
    return userActionEvent;
  }
  
  private static Tenant buildTenant(UUID paramUUID) {
    return (new Tenant()).with(paramTenant -> paramTenant.id = paramUUID)
      .with(paramTenant -> paramTenant.issuer = "{{tenant.issuer}}")
      .with(paramTenant -> paramTenant.externalIdentifierConfiguration.with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(()));
  }
  
  private static UserAction buildUserAction() {
    return (new UserAction()).with(paramUserAction -> paramUserAction.id = UUID.randomUUID())
      .with(paramUserAction -> paramUserAction.name = "{{action.name}}");
  }
  
  private static class MockRequestData implements TemplateHashModelEx, TemplateScalarModel {
    public String name;
    
    public MockRequestData(String param1String) {
      this.name = param1String;
    }
    
    public TemplateModel get(String param1String) throws TemplateModelException {
      return (TemplateModel)new MockRequestData(this.name + "." + this.name);
    }
    
    public String getAsString() {
      return "{{" + this.name + "}}";
    }
    
    public boolean isEmpty() {
      return false;
    }
    
    public TemplateCollectionModel keys() {
      return null;
    }
    
    public int size() {
      return 0;
    }
    
    public TemplateCollectionModel values() {
      return null;
    }
  }
  
  private static class MockUserData extends LinkedHashMap<String, Object> {
    public String name;
    
    public MockUserData(String param1String) {
      this.name = param1String;
    }
    
    public Object get(Object param1Object) {
      return new MockUserData(this.name + "." + this.name);
    }
  }
  
  static {
    UUID uUID = UUID.randomUUID();
    User user1 = MessageTools.buildMockUser("user", UUID.fromString("00000000-0000-0000-0000-000000000001"), uUID);
    User user2 = MessageTools.buildMockUser("parent", UUID.fromString("00000000-0000-0000-0000-000000000002"), uUID);
    User user3 = MessageTools.buildMockUser("child", UUID.fromString("00000000-0000-0000-0000-000000000003"), uUID);
    MOCK_PARAMETERS.put("child", user3.secure());
    MOCK_PARAMETERS.put("parent", user2.secure());
    MOCK_PARAMETERS.put("user", user1.secure());
    MOCK_PARAMETERS.put("tenant", buildTenant(uUID));
    MOCK_PARAMETERS.put("action", buildUserAction());
    MOCK_PARAMETERS.put("baseUrl", "https://example.com");
    MOCK_PARAMETERS.put("breachResult", (new BreachResult()).with(paramBreachResult -> paramBreachResult.match = BreachedPasswordStatus.None));
    MOCK_PARAMETERS.put("changePasswordId", "{{changePasswordId}}");
    MOCK_PARAMETERS.put("code", "{{code}}");
    MOCK_PARAMETERS.put("event", buildMockEvent(user1.id));
    MOCK_PARAMETERS.put("phase", UserActionPhase.start);
    MOCK_PARAMETERS.put("registration", user1.getRegistrations().get(0));
    MOCK_PARAMETERS.put("requestData", new MockRequestData("requestData"));
    MOCK_PARAMETERS.put("state", Map.of("client_id", "ab1f10dc-37a6-4f5b-9ebf-07d2118f9914"));
    MOCK_PARAMETERS.put("verificationId", "{{verificationId}}");
    MOCK_PARAMETERS.put("method", user1.twoFactor.methods.get(1));
    FIELD_NAME_MAPPING.put("from", "emailTemplate.defaultFromName");
    FIELD_NAME_MAPPING.put("html", "emailTemplate.defaultHtmlTemplate");
    FIELD_NAME_MAPPING.put("subject", "emailTemplate.defaultSubject");
    FIELD_NAME_MAPPING.put("text", "emailTemplate.defaultTextTemplate");
  }
}
