package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.fusionauth.api.domain.SCIMServerPermissions;
import io.fusionauth.api.service.email.EmailTemplateService;
import io.fusionauth.api.service.entity.EntityService;
import io.fusionauth.api.service.lambda.LambdaService;
import io.fusionauth.api.service.message.MessageTemplateService;
import io.fusionauth.api.util.EmailTools;
import io.fusionauth.api.util.PhoneMessageTools;
import io.fusionauth.domain.EntityType;
import io.fusionauth.domain.Lambda;
import io.fusionauth.domain.LambdaType;
import io.fusionauth.domain.email.EmailTemplate;
import io.fusionauth.domain.message.MessageTemplate;
import io.fusionauth.domain.message.sms.SMSMessageTemplate;
import io.fusionauth.domain.message.voice.VoiceMessageTemplate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.savantbuild.domain.Version;

@Singleton
public class SystemDefaultsSingleton {
  public static final Map<Version, List<EmailTemplate>> EmailTemplates;
  
  public static final Map<Version, List<EntityType>> EntityTypes;
  
  public static final Map<Version, List<Lambda>> Lambdas;
  
  public static final Map<Version, List<MessageTemplate>> MessageTemplates;
  
  private final EmailTemplateService emailTemplateService;
  
  private final EntityService entityService;
  
  private final LambdaService lambdaService;
  
  private final MessageTemplateService messageTemplateService;
  
  static {
    EmailTemplates = Map.of(new Version("1.0.0"), 
        List.of((Object[])new EmailTemplate[] { 
            (new EmailTemplate()).with(paramEmailTemplate -> paramEmailTemplate.defaultHtmlTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/breached-password.html.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.defaultSubject = "Your password is not secure")
            .with(paramEmailTemplate -> paramEmailTemplate.defaultTextTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/breached-password.txt.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.id = UUID.randomUUID())
            .with(paramEmailTemplate -> paramEmailTemplate.name = "[FusionAuth Default] Breached Password Notification"), (new EmailTemplate())
            
            .with(paramEmailTemplate -> paramEmailTemplate.defaultHtmlTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/change-password.html.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.defaultSubject = "Reset your password")
            .with(paramEmailTemplate -> paramEmailTemplate.defaultTextTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/change-password.txt.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.id = UUID.randomUUID())
            .with(paramEmailTemplate -> paramEmailTemplate.name = "[FusionAuth Default] Forgot Password"), (new EmailTemplate())
            
            .with(paramEmailTemplate -> paramEmailTemplate.defaultHtmlTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/confirm-child.html.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.defaultSubject = "Confirm your child's account")
            .with(paramEmailTemplate -> paramEmailTemplate.defaultTextTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/confirm-child.txt.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.id = UUID.randomUUID())
            .with(paramEmailTemplate -> paramEmailTemplate.name = "[FusionAuth Default] Confirm Child Account"), (new EmailTemplate())
            
            .with(paramEmailTemplate -> paramEmailTemplate.defaultHtmlTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/coppa-email-plus-notice.html.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.defaultSubject = "Reminder: Notice of your consent")
            .with(paramEmailTemplate -> paramEmailTemplate.defaultTextTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/coppa-email-plus-notice.txt.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.id = UUID.randomUUID())
            .with(paramEmailTemplate -> paramEmailTemplate.name = "[FusionAuth Default] COPPA Notice Reminder"), (new EmailTemplate())
            
            .with(paramEmailTemplate -> paramEmailTemplate.defaultHtmlTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/coppa-notice.html.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.defaultSubject = "Notice of your consent")
            .with(paramEmailTemplate -> paramEmailTemplate.defaultTextTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/coppa-notice.txt.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.id = UUID.randomUUID())
            .with(paramEmailTemplate -> paramEmailTemplate.name = "[FusionAuth Default] COPPA Notice"), (new EmailTemplate())
            
            .with(paramEmailTemplate -> paramEmailTemplate.defaultHtmlTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/email-verification.html.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.defaultSubject = "Verify your FusionAuth email address")
            .with(paramEmailTemplate -> paramEmailTemplate.defaultTextTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/email-verification.txt.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.id = UUID.randomUUID())
            .with(paramEmailTemplate -> paramEmailTemplate.name = "[FusionAuth Default] Email Verification"), (new EmailTemplate())
            
            .with(paramEmailTemplate -> paramEmailTemplate.defaultHtmlTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/parent-registration.html.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.defaultSubject = "Create your parental account")
            .with(paramEmailTemplate -> paramEmailTemplate.defaultTextTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/parent-registration.txt.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.id = UUID.randomUUID())
            .with(paramEmailTemplate -> paramEmailTemplate.name = "[FusionAuth Default] Parent Registration"), (new EmailTemplate())
            
            .with(paramEmailTemplate -> paramEmailTemplate.defaultHtmlTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/passwordless-login.html.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.defaultSubject = "Log into FusionAuth")
            .with(paramEmailTemplate -> paramEmailTemplate.defaultTextTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/passwordless-login.txt.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.id = UUID.randomUUID())
            .with(paramEmailTemplate -> paramEmailTemplate.name = "[FusionAuth Default] Passwordless Login"), (new EmailTemplate())
            
            .with(paramEmailTemplate -> paramEmailTemplate.defaultHtmlTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/registration-verification.html.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.defaultSubject = "Verify your registration")
            .with(paramEmailTemplate -> paramEmailTemplate.defaultTextTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/registration-verification.txt.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.id = UUID.randomUUID())
            .with(paramEmailTemplate -> paramEmailTemplate.name = "[FusionAuth Default] Registration Verification"), (new EmailTemplate())
            
            .with(paramEmailTemplate -> paramEmailTemplate.defaultHtmlTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/setup-password.html.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.defaultSubject = "Set up your password")
            .with(paramEmailTemplate -> paramEmailTemplate.defaultTextTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/setup-password.txt.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.id = UUID.randomUUID())
            .with(paramEmailTemplate -> paramEmailTemplate.name = "[FusionAuth Default] Set up Password"), 
            (new EmailTemplate())
            
            .with(paramEmailTemplate -> paramEmailTemplate.defaultHtmlTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/two-factor-login.html.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.defaultSubject = "Your second factor code")
            .with(paramEmailTemplate -> paramEmailTemplate.defaultTextTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/two-factor-login.txt.ftl"))
            .with(paramEmailTemplate -> paramEmailTemplate.id = UUID.randomUUID())
            .with(paramEmailTemplate -> paramEmailTemplate.name = "[FusionAuth Default] Two Factor Authentication") }), new Version("1.30.0"), List.of((new EmailTemplate())
          .with(paramEmailTemplate -> paramEmailTemplate.defaultHtmlTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/threat-detected.html.ftl"))
          .with(paramEmailTemplate -> paramEmailTemplate.defaultSubject = "Threat Detected")
          .with(paramEmailTemplate -> paramEmailTemplate.defaultTextTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/threat-detected.txt.ftl"))
          .with(paramEmailTemplate -> paramEmailTemplate.id = UUID.randomUUID())
          .with(paramEmailTemplate -> paramEmailTemplate.name = "[FusionAuth Default] Threat Detected")), new Version("1.42.0"), 
        
        List.of((new EmailTemplate())
          .with(paramEmailTemplate -> paramEmailTemplate.defaultHtmlTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/two-factor-add.html.ftl"))
          .with(paramEmailTemplate -> paramEmailTemplate.defaultSubject = "A second factor was added")
          .with(paramEmailTemplate -> paramEmailTemplate.defaultTextTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/two-factor-add.txt.ftl"))
          .with(paramEmailTemplate -> paramEmailTemplate.id = UUID.randomUUID())
          .with(paramEmailTemplate -> paramEmailTemplate.name = "[FusionAuth Default] Two Factor Authentication Method Added"), (new EmailTemplate())
          
          .with(paramEmailTemplate -> paramEmailTemplate.defaultHtmlTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/two-factor-remove.html.ftl"))
          .with(paramEmailTemplate -> paramEmailTemplate.defaultSubject = "A second factor was removed")
          .with(paramEmailTemplate -> paramEmailTemplate.defaultTextTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/two-factor-remove.txt.ftl"))
          .with(paramEmailTemplate -> paramEmailTemplate.id = UUID.randomUUID())
          .with(paramEmailTemplate -> paramEmailTemplate.name = "[FusionAuth Default] Two Factor Authentication Method Removed")), new Version("1.68.0"), 
        
        List.of((new EmailTemplate())
          .with(paramEmailTemplate -> paramEmailTemplate.defaultHtmlTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/admin-two-factor-remove.html.ftl"))
          .with(paramEmailTemplate -> paramEmailTemplate.defaultSubject = "Your two-factor authentication method was removed")
          .with(paramEmailTemplate -> paramEmailTemplate.defaultTextTemplate = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/emails/admin-two-factor-remove.txt.ftl"))
          .with(paramEmailTemplate -> paramEmailTemplate.id = UUID.randomUUID())
          .with(paramEmailTemplate -> paramEmailTemplate.name = "[FusionAuth Default] Admin Two Factor Method Removed")));
    EntityTypes = Map.of(new Version("1.36.0"), 
        List.of((new EntityType())
          
          .with(paramEntityType -> paramEntityType.name = "[FusionAuth Default] SCIM Server")
          .with(paramEntityType -> paramEntityType.permissions = SCIMServerPermissions.SCIMServerEntityPermissions), (new EntityType())

          
          .with(paramEntityType -> paramEntityType.name = "[FusionAuth Default] SCIM Client")));
    Lambdas = Map.of(new Version("1.0.0"), 
        List.of((new Lambda())
          .with(paramLambda -> paramLambda.body = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/lambdas/apple-reconcile.js"))
          .with(paramLambda -> paramLambda.name = "[FusionAuth Default] Apple Reconcile")
          .with(paramLambda -> paramLambda.type = LambdaType.AppleReconcile), (new Lambda())
          
          .with(paramLambda -> paramLambda.body = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/lambdas/facebook-reconcile.js"))
          .with(paramLambda -> paramLambda.name = "[FusionAuth Default] Facebook Reconcile")
          .with(paramLambda -> paramLambda.type = LambdaType.FacebookReconcile), (new Lambda())
          
          .with(paramLambda -> paramLambda.body = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/lambdas/google-reconcile.js"))
          .with(paramLambda -> paramLambda.name = "[FusionAuth Default] Google Reconcile")
          .with(paramLambda -> paramLambda.type = LambdaType.GoogleReconcile), (new Lambda())
          
          .with(paramLambda -> paramLambda.body = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/lambdas/linkedin-reconcile.js"))
          .with(paramLambda -> paramLambda.name = "[FusionAuth Default] LinkedIn Reconcile")
          .with(paramLambda -> paramLambda.type = LambdaType.LinkedInReconcile), (new Lambda())
          
          .with(paramLambda -> paramLambda.body = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/lambdas/openid-connect-reconcile.js"))
          .with(paramLambda -> paramLambda.name = "[FusionAuth Default] OpenID Connect Reconcile")
          .with(paramLambda -> paramLambda.type = LambdaType.OpenIDReconcile), (new Lambda())
          
          .with(paramLambda -> paramLambda.body = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/lambdas/samlv2-reconcile.js"))
          .with(paramLambda -> paramLambda.name = "[FusionAuth Default] SAML v2 Reconcile")
          .with(paramLambda -> paramLambda.type = LambdaType.SAMLv2Reconcile), (new Lambda())
          
          .with(paramLambda -> paramLambda.body = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/lambdas/twitter-reconcile.js"))
          .with(paramLambda -> paramLambda.name = "[FusionAuth Default] Twitter Reconcile")
          .with(paramLambda -> paramLambda.type = LambdaType.TwitterReconcile)), new Version("1.36.0"), 
        
        List.of((new Lambda())
          .with(paramLambda -> paramLambda.body = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/lambdas/scim-group-request-converter.js"))
          .with(paramLambda -> paramLambda.name = "[FusionAuth Default] SCIM Group Request Converter")
          .with(paramLambda -> paramLambda.type = LambdaType.SCIMServerGroupRequestConverter), (new Lambda())
          
          .with(paramLambda -> paramLambda.body = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/lambdas/scim-group-response-converter.js"))
          .with(paramLambda -> paramLambda.name = "[FusionAuth Default] SCIM Group Response Converter")
          .with(paramLambda -> paramLambda.type = LambdaType.SCIMServerGroupResponseConverter), (new Lambda())
          
          .with(paramLambda -> paramLambda.body = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/lambdas/scim-user-request-converter.js"))
          .with(paramLambda -> paramLambda.name = "[FusionAuth Default] SCIM User Request Converter")
          .with(paramLambda -> paramLambda.type = LambdaType.SCIMServerUserRequestConverter), (new Lambda())
          
          .with(paramLambda -> paramLambda.body = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/lambdas/scim-user-response-converter.js"))
          .with(paramLambda -> paramLambda.name = "[FusionAuth Default] SCIM User Response Converter")
          .with(paramLambda -> paramLambda.type = LambdaType.SCIMServerUserResponseConverter)), new Version("1.68.0"), 

        
        List.of((new Lambda())
          .with(paramLambda -> paramLambda.body = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/lambdas/mfa-requirement-high-risk.js"))
          .with(paramLambda -> paramLambda.name = "[FusionAuth Default] MFA challenge when high risk")
          .with(paramLambda -> paramLambda.type = LambdaType.MFARequirement), (new Lambda())
          
          .with(paramLambda -> paramLambda.body = EmailTools.loadTemplate(SystemDefaultsSingleton.class, "/lambdas/mfa-requirement-medium-risk.js"))
          .with(paramLambda -> paramLambda.name = "[FusionAuth Default] MFA challenge when medium risk")
          .with(paramLambda -> paramLambda.type = LambdaType.MFARequirement)));
    MessageTemplates = Map.of(new Version("1.59.0"), 
        List.of((new SMSMessageTemplate())
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.id = UUID.randomUUID())
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.name = "[FusionAuth Default] Phone Forgot Password")
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.defaultTemplate = PhoneMessageTools.loadTemplate(SystemDefaultsSingleton.class, "/messages/phone-forgot-password.txt.ftl")), (new SMSMessageTemplate())
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.id = UUID.randomUUID())
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.name = "[FusionAuth Default] Phone Passwordless Login")
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.defaultTemplate = PhoneMessageTools.loadTemplate(SystemDefaultsSingleton.class, "/messages/phone-passwordless-login.txt.ftl")), (new SMSMessageTemplate())
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.id = UUID.randomUUID())
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.name = "[FusionAuth Default] Phone Set up Password")
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.defaultTemplate = PhoneMessageTools.loadTemplate(SystemDefaultsSingleton.class, "/messages/phone-setup-password.txt.ftl")), (new SMSMessageTemplate())
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.id = UUID.randomUUID())
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.name = "[FusionAuth Default] Phone Verification")
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.defaultTemplate = PhoneMessageTools.loadTemplate(SystemDefaultsSingleton.class, "/messages/phone-verification.txt.ftl")), (new SMSMessageTemplate())
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.id = UUID.randomUUID())
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.name = "[FusionAuth Default] Phone Threat Detected")
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.defaultTemplate = PhoneMessageTools.loadTemplate(SystemDefaultsSingleton.class, "/messages/phone-threat-detected.txt.ftl")), (new SMSMessageTemplate())
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.id = UUID.randomUUID())
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.name = "[FusionAuth Default] Phone Two Factor Add")
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.defaultTemplate = PhoneMessageTools.loadTemplate(SystemDefaultsSingleton.class, "/messages/phone-two-factor-add.txt.ftl")), (new SMSMessageTemplate())
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.id = UUID.randomUUID())
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.name = "[FusionAuth Default] Phone Two Factor Remove")
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.defaultTemplate = PhoneMessageTools.loadTemplate(SystemDefaultsSingleton.class, "/messages/phone-two-factor-remove.txt.ftl"))), new Version("1.68.0"), 
        
        List.of((new SMSMessageTemplate())
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.id = UUID.randomUUID())
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.name = "[FusionAuth Default] SMS Two Factor Request")
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.defaultTemplate = PhoneMessageTools.loadTemplate(SystemDefaultsSingleton.class, "/messages/sms-two-factor-request.txt.ftl")), (new VoiceMessageTemplate())
          .with(paramVoiceMessageTemplate -> paramVoiceMessageTemplate.id = UUID.randomUUID())
          .with(paramVoiceMessageTemplate -> paramVoiceMessageTemplate.name = "[FusionAuth Default] Voice Two Factor Request")
          .with(paramVoiceMessageTemplate -> paramVoiceMessageTemplate.defaultTemplate = PhoneMessageTools.loadTemplate(SystemDefaultsSingleton.class, "/messages/voice-two-factor-request.txt.ftl")), (new SMSMessageTemplate())
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.id = UUID.randomUUID())
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.name = "[FusionAuth Default] Phone Admin Two Factor Remove")
          .with(paramSMSMessageTemplate -> paramSMSMessageTemplate.defaultTemplate = PhoneMessageTools.loadTemplate(SystemDefaultsSingleton.class, "/messages/phone-admin-two-factor-remove.txt.ftl"))));
  }
  
  @Inject
  public SystemDefaultsSingleton(EmailTemplateService paramEmailTemplateService, EntityService paramEntityService, LambdaService paramLambdaService, MessageTemplateService paramMessageTemplateService) {
    this.emailTemplateService = paramEmailTemplateService;
    this.entityService = paramEntityService;
    this.lambdaService = paramLambdaService;
    this.messageTemplateService = paramMessageTemplateService;
  }
  
  public void set(Version paramVersion) {
    List list1 = EmailTemplates.get(paramVersion);
    if (list1 != null) {
      Objects.requireNonNull(this.emailTemplateService);
      list1.stream().filter(paramEmailTemplate -> (this.emailTemplateService.retrieveByName(paramEmailTemplate.name) == null)).forEach(this.emailTemplateService::create);
    } 
    List list2 = MessageTemplates.get(paramVersion);
    if (list2 != null) {
      Objects.requireNonNull(this.messageTemplateService);
      list2.stream().filter(paramMessageTemplate -> (this.messageTemplateService.retrieveByName(paramMessageTemplate.name) == null)).forEach(this.messageTemplateService::create);
    } 
    List list3 = Lambdas.get(paramVersion);
    if (list3 != null) {
      Objects.requireNonNull(this.lambdaService);
      list3.forEach(this.lambdaService::create);
    } 
    List list4 = EntityTypes.get(paramVersion);
    if (list4 != null) {
      Objects.requireNonNull(this.entityService);
      list4.forEach(this.entityService::createType);
    } 
  }
}
