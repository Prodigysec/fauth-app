package io.fusionauth.app.service.license;

import com.google.inject.Inject;
import io.fusionauth.api.service.event.EventService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.api.service.tenant.RateLimitHelper;
import io.fusionauth.api.util.MFATools;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventConfiguration;
import io.fusionauth.domain.MultiFactorLoginPolicy;
import io.fusionauth.domain.RateLimitedRequestType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.TenantWebAuthnConfiguration;
import io.fusionauth.domain.UnverifiedBehavior;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.connector.ConnectorPolicy;
import io.fusionauth.domain.event.EventType;
import io.fusionauth.domain.oauth2.OAuthApplicationRelationship;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import io.fusionauth.domain.webauthn.AuthenticatorAttachmentPreference;
import java.util.List;

public class DefaultFrontEndLicensedFeaturesService implements FrontEndLicensedFeaturesService {
  private final ReactorStatusService reactorStatusService;
  
  @Inject
  public DefaultFrontEndLicensedFeaturesService(ReactorStatusService paramReactorStatusService) {
    this.reactorStatusService = paramReactorStatusService;
  }
  
  public void disableLicensedFeatures(Tenant paramTenant) {
    ReactorStatus reactorStatus = this.reactorStatusService.retrieveStatus();
    if (ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.advancedRegistration)) {
      paramTenant.emailConfiguration.unverified.behavior = UnverifiedBehavior.Allow;
      paramTenant.phoneConfiguration.unverified.behavior = UnverifiedBehavior.Allow;
      paramTenant.usernameConfiguration.unique.enabled = false;
    } 
    if (ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.breachedPasswordDetection)) {
      paramTenant.passwordValidationRules.breachDetection.enabled = false;
      for (EventType eventType : EventService.BreachPasswordLicensedEvents) {
        if (paramTenant.eventConfiguration.events.containsKey(eventType))
          ((EventConfiguration.EventConfigurationData)paramTenant.eventConfiguration.events.get(eventType)).enabled = false; 
      } 
    } 
    if (ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.connectors))
      paramTenant.connectorPolicies = List.of((new ConnectorPolicy()).with(paramConnectorPolicy -> paramConnectorPolicy.connectorId = BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID)
          .with(paramConnectorPolicy -> paramConnectorPolicy.domains.add("*"))); 
    if (ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.advancedMultiFactorAuthentication)) {
      paramTenant.multiFactorConfiguration.email.enabled = false;
      paramTenant.multiFactorConfiguration.email.templateId = null;
      paramTenant.multiFactorConfiguration.sms.enabled = false;
      paramTenant.multiFactorConfiguration.sms.messengerId = null;
      paramTenant.multiFactorConfiguration.sms.templateId = null;
      paramTenant.multiFactorConfiguration.voice.enabled = false;
      paramTenant.multiFactorConfiguration.voice.messengerId = null;
      paramTenant.multiFactorConfiguration.voice.templateId = null;
    } 
    if (ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.multiFactorLambdas))
      paramTenant.lambdaConfiguration.multiFactorRequirementId = null; 
    if (ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.intelligentMFA) && 
      MFATools.isIntelligentLoginPolicy(paramTenant.multiFactorConfiguration.loginPolicy))
      paramTenant.multiFactorConfiguration.loginPolicy = MultiFactorLoginPolicy.Enabled; 
    if (ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.imfaWebhooks))
      for (EventType eventType : EventService.IMFAWebhookLicensedEvents) {
        if (paramTenant.eventConfiguration.events.containsKey(eventType))
          ((EventConfiguration.EventConfigurationData)paramTenant.eventConfiguration.events.get(eventType)).enabled = false; 
      }  
    if (ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.clientRiskConfiguration))
      paramTenant.clientRiskConfiguration.enabled = false; 
    if (ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.threatDetection)) {
      paramTenant.accessControlConfiguration.uiIPAccessControlListId = null;
      paramTenant.captchaConfiguration.enabled = false;
      paramTenant.registrationConfiguration.blockedDomains.clear();
      for (EventType eventType : EventService.ThreatDetectionLicensedEvents) {
        if (paramTenant.eventConfiguration.events.containsKey(eventType))
          ((EventConfiguration.EventConfigurationData)paramTenant.eventConfiguration.events.get(eventType)).enabled = false; 
      } 
      paramTenant.emailConfiguration.loginIdInUseOnCreateEmailTemplateId = null;
      paramTenant.emailConfiguration.loginIdInUseOnUpdateEmailTemplateId = null;
      paramTenant.emailConfiguration.loginNewDeviceEmailTemplateId = null;
      paramTenant.emailConfiguration.loginSuspiciousEmailTemplateId = null;
      paramTenant.emailConfiguration.passwordResetSuccessEmailTemplateId = null;
      paramTenant.emailConfiguration.passwordUpdateEmailTemplateId = null;
      paramTenant.emailConfiguration.twoFactorMethodAddEmailTemplateId = null;
      paramTenant.emailConfiguration.twoFactorMethodRemoveEmailTemplateId = null;
      for (RateLimitedRequestType rateLimitedRequestType : RateLimitedRequestType.values())
        (RateLimitHelper.getConfiguration(paramTenant.rateLimitConfiguration, rateLimitedRequestType)).enabled = false; 
    } 
    boolean bool = false;
    TenantWebAuthnConfiguration tenantWebAuthnConfiguration = paramTenant.webAuthnConfiguration;
    if (ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.webAuthn)) {
      tenantWebAuthnConfiguration.enabled = false;
      bool = true;
    } 
    if (bool || ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.webAuthnRoamingAuthenticators)) {
      tenantWebAuthnConfiguration.bootstrapWorkflow.authenticatorAttachmentPreference = AuthenticatorAttachmentPreference.platform;
      tenantWebAuthnConfiguration.reauthenticationWorkflow.authenticatorAttachmentPreference = AuthenticatorAttachmentPreference.platform;
    } 
  }
  
  public void disableLicensedFeatures(Application paramApplication) {
    ReactorStatus reactorStatus = this.reactorStatusService.retrieveStatus();
    if (ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.advancedRegistration)) {
      paramApplication.formConfiguration.selfServiceFormId = null;
      paramApplication.registrationConfiguration.type = Application.RegistrationConfiguration.RegistrationType.basic;
      paramApplication.registrationConfiguration.formId = null;
      paramApplication.unverified.behavior = UnverifiedBehavior.Allow;
    } 
    if (ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.advancedOAuthScopesCustomScopes))
      paramApplication.scopes.clear(); 
    if (ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.advancedOAuthScopesThirdPartyApplications))
      paramApplication.oauthConfiguration.relationship = OAuthApplicationRelationship.FirstParty; 
    if (ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.advancedRegistration)) {
      paramApplication.formConfiguration.adminRegistrationFormId = null;
      paramApplication.formConfiguration.selfServiceFormId = null;
      paramApplication.registrationConfiguration.type = Application.RegistrationConfiguration.RegistrationType.basic;
      paramApplication.registrationConfiguration.formId = null;
      paramApplication.unverified.behavior = UnverifiedBehavior.Allow;
    } 
    if (ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.applicationThemes))
      paramApplication.themeId = null; 
    if (ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.threatDetection)) {
      paramApplication.accessControlConfiguration.uiIPAccessControlListId = null;
      paramApplication.emailConfiguration.loginIdInUseOnCreateEmailTemplateId = null;
      paramApplication.emailConfiguration.loginIdInUseOnUpdateEmailTemplateId = null;
      paramApplication.emailConfiguration.loginNewDeviceEmailTemplateId = null;
      paramApplication.emailConfiguration.loginSuspiciousEmailTemplateId = null;
      paramApplication.emailConfiguration.passwordResetSuccessEmailTemplateId = null;
      paramApplication.emailConfiguration.passwordUpdateEmailTemplateId = null;
      paramApplication.emailConfiguration.twoFactorMethodAddEmailTemplateId = null;
      paramApplication.emailConfiguration.twoFactorMethodRemoveEmailTemplateId = null;
    } 
    if (ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.applicationMultiFactorAuthentication)) {
      paramApplication.externalIdentifierConfiguration.twoFactorTrustIdTimeToLiveInSeconds = null;
      paramApplication.multiFactorConfiguration.loginPolicy = null;
      paramApplication.multiFactorConfiguration.trustPolicy = null;
      paramApplication.multiFactorConfiguration.email.templateId = null;
      paramApplication.multiFactorConfiguration.sms.templateId = null;
      paramApplication.multiFactorConfiguration.voice.templateId = null;
    } 
    if (ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.webAuthn))
      paramApplication.webAuthnConfiguration.enabled = false; 
  }
}
