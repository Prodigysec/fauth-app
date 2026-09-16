package io.fusionauth.app.action.admin.theme;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.CachedTheme;
import io.fusionauth.api.domain.guice.FusionAuthTenantId;
import io.fusionauth.api.service.FusionAuthAccessDeniedException;
import io.fusionauth.api.service.system.ThemeService;
import io.fusionauth.app.action.BaseThemedAction;
import io.fusionauth.app.domain.FormStepInfo;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.primeframework.error.FusionAuthAccessDeniedExceptionHandler;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationOAuthScope;
import io.fusionauth.domain.Theme;
import io.fusionauth.domain.ThemeType;
import io.fusionauth.domain.User;
import io.fusionauth.domain.WebAuthnCredential;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.FormFieldResponse;
import io.fusionauth.domain.api.FormResponse;
import io.fusionauth.domain.api.ThemeResponse;
import io.fusionauth.domain.form.Form;
import io.fusionauth.domain.form.FormControl;
import io.fusionauth.domain.form.FormField;
import io.fusionauth.domain.form.FormStep;
import io.fusionauth.domain.form.FormStepType;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.provider.PendingIdPLink;
import io.fusionauth.domain.webauthn.AttestationType;
import io.fusionauth.domain.webauthn.CoseAlgorithmIdentifier;
import io.fusionauth.http.server.HTTPContext;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleMessage;

@Action(requiresAuthentication = true, constraints = {"admin", "theme_manager"})
@ThemedForward(code = "success", page = "/${templatePath}")
public class PreviewAction extends BaseViewAction {
  private final HTTPContext context;
  
  public String defaultMessages;
  
  public Map<String, String> localizedMessages;
  
  public boolean showAlerts;
  
  @FTLVariable
  public BaseThemedAction.LocaleResolvedCachedTheme theme;
  
  @Inject
  public PreviewAction(FrontEndSupport paramFrontEndSupport, HTTPContext paramHTTPContext, @FusionAuthTenantId UUID paramUUID) {
    super(paramFrontEndSupport);
    this.tenantId = paramUUID;
    this.context = paramHTTPContext;
  }
  
  public String get() {
    this.templatePath = ThemeService.TemplateMapping.get(this.template);
    setup();
    return "success";
  }
  
  public String post() {
    return get();
  }
  
  private void handleAdvancedRegistrationFormFields(Form paramForm) {
    List<FormField> list = (List)this.fields;
    FormStep formStep = paramForm.steps.get(this.step - 1);
    this.advancedRegistration = true;
    switch (formStep.type) {
      default:
        throw new MatchException(null, null);
      case collectData:
        if (this.step == this.totalSteps);
      case verifyEmail:
      case verifyPhoneNumber:
        break;
    } 
    this.readyToRegister = false;
    switch (formStep.type) {
      default:
        throw new MatchException(null, null);
      case collectData:
      
      case verifyEmail:
      
      case verifyPhoneNumber:
        break;
    } 
    this.formStep = 



      
      new FormStepInfo(this.theme.message("{description}phone-verification-required", new Object[0]), this.theme
        .message("phone-verification-required-send-another", new Object[0]));
    switch (formStep.type) {
      default:
        throw new MatchException(null, null);
      case collectData:
      
      case verifyEmail:
      
      case verifyPhoneNumber:
        break;
    } 
    String str = 


      
      "[PhoneVerificationSent]";
    if (str != null) {
      String str1 = this.theme.message(str, new Object[0]);
      this.frontEndSupport.messageStore.add((Message)new SimpleMessage(MessageType.INFO, str, str1));
    } 
    formStep.fields.forEach(paramUUID -> paramList.add(((FormFieldResponse)superDelegate().execute(())).field));
    switch (formStep.type) {
      default:
        throw new MatchException(null, null);
      case collectData:
      
      case verifyEmail:
      case verifyPhoneNumber:
        break;
    } 
    FormField formField = (

      
      new FormField())
      .with(paramFormField -> paramFormField.key = "verificationCode")
      .with(paramFormField -> paramFormField.required = true)
      .with(paramFormField -> paramFormField.control = FormControl.text)
      .with(FormField::normalize);
    if (formField != null)
      list.add(formField); 
  }
  
  private void setup() {
    if (this.applicationId != null)
      this.application = ((ApplicationResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveApplication(this.applicationId))).application; 
    if (this.application == null) {
      this.application = new Application();
      this.application.id = UUID.randomUUID();
      this.client_id = this.application.id.toString();
      this.application.name = "My Application";
      this.application.tenantId = this.tenantId;
    } else {
      this.client_id = this.application.oauthConfiguration.clientId;
    } 
    this.metaData.device.type = "Browser";
    this.metaData.device.name = "Chrome";
    this.response_type = "code";
    if (this.application != null && this.application.tenantId != null) {
      this.tenant = this.tenants.get(this.application.tenantId);
    } else {
      this.tenant = this.tenants.get(this.tenantId);
    } 
    this.timezone = "America/Denver";
    this.currentUser = (new User(this.codeCurrentUser)).secure();
    this.user = this.currentUser;
    this.passwordValidationRules = this.tenant.passwordValidationRules;
    if (this.showPasswordValidationRules)
      this.frontEndSupport.addFieldError("user.password", "[blank]user.password", new Object[0]); 
    if (this.hasDomainBasedIdentityProviders)
      this.loginId = "richard@piedpiper.com"; 
    this.frontEndSupport.request.setAttribute("incidentId", FusionAuthAccessDeniedExceptionHandler.generateIncidentId(this.frontEndSupport.request, this.frontEndSupport.configuration, this.frontEndSupport.systemConfiguration));
    this.frontEndSupport.request.setAttribute("incidentCause", FusionAuthAccessDeniedException.class.getSimpleName());
    this














      
      .credential = (new WebAuthnCredential()).with(paramWebAuthnCredential -> paramWebAuthnCredential.id = UUID.randomUUID()).with(paramWebAuthnCredential -> paramWebAuthnCredential.tenantId = this.tenant.id).with(paramWebAuthnCredential -> paramWebAuthnCredential.userId = this.user.id).with(paramWebAuthnCredential -> paramWebAuthnCredential.credentialId = "credential_id").with(paramWebAuthnCredential -> paramWebAuthnCredential.displayName = "Unnamed").with(paramWebAuthnCredential -> paramWebAuthnCredential.name = "credential_name").with(paramWebAuthnCredential -> paramWebAuthnCredential.algorithm = CoseAlgorithmIdentifier.ES256).with(paramWebAuthnCredential -> paramWebAuthnCredential.attestationType = AttestationType.none).with(paramWebAuthnCredential -> paramWebAuthnCredential.discoverable = true).with(paramWebAuthnCredential -> paramWebAuthnCredential.authenticatorSupportsUserVerification = true).with(paramWebAuthnCredential -> paramWebAuthnCredential.relyingPartyId = "fusionauth.io").with(paramWebAuthnCredential -> paramWebAuthnCredential.signCount = 22).with(paramWebAuthnCredential -> paramWebAuthnCredential.transports = Collections.singletonList("internal")).with(paramWebAuthnCredential -> paramWebAuthnCredential.publicKey = "public_key").with(paramWebAuthnCredential -> paramWebAuthnCredential.insertInstant = ZonedDateTime.now(ZoneOffset.UTC)).with(paramWebAuthnCredential -> paramWebAuthnCredential.lastUseInstant = ZonedDateTime.now(ZoneOffset.UTC));
    if (this.template != null && this.template.equals("oauth2StartIdPLink"))
      this



        
        .pendingIdPLink = (new PendingIdPLink()).with(paramPendingIdPLink -> paramPendingIdPLink.identityProviderId = IdentityProviderType.Xbox.id).with(paramPendingIdPLink -> paramPendingIdPLink.identityProviderType = IdentityProviderType.Xbox).with(paramPendingIdPLink -> paramPendingIdPLink.identityProviderName = IdentityProviderType.Xbox.displayName()).with(paramPendingIdPLink -> paramPendingIdPLink.identityProviderUserId = "2439411927148027").with(paramPendingIdPLink -> paramPendingIdPLink.displayName = "gamer42").with(paramPendingIdPLink -> paramPendingIdPLink.username = "gamer42"); 
    if (this.template != null && this.template.equals("oauth2Consent")) {
      this.requiredScopes = List.of((new ApplicationOAuthScope()).with(paramApplicationOAuthScope -> paramApplicationOAuthScope.name = "required")
          .with(paramApplicationOAuthScope -> paramApplicationOAuthScope.defaultConsentMessage = "Required scope consent message")
          .with(paramApplicationOAuthScope -> paramApplicationOAuthScope.defaultConsentDetail = "Required scope consent detail")
          .with(paramApplicationOAuthScope -> paramApplicationOAuthScope.required = true));
      this.optionalScopes = List.of((new ApplicationOAuthScope()).with(paramApplicationOAuthScope -> paramApplicationOAuthScope.name = "optional")
          .with(paramApplicationOAuthScope -> paramApplicationOAuthScope.defaultConsentMessage = "Optional scope consent message")
          .with(paramApplicationOAuthScope -> paramApplicationOAuthScope.defaultConsentDetail = "Optional scope consent detail"));
      this.scopeConsents = Map.of("required", Boolean.valueOf(true), "optional", 
          Boolean.valueOf(true));
    } 
    Theme theme1 = ((ThemeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTheme(Theme.FUSIONAUTH_THEME_ID))).theme;
    Theme theme2 = ((ThemeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTheme(this.themeId))).theme;
    if (ThemeType.simple.equals(theme2.type))
      ThemeService.populateSimpleTheme(theme2, this.context.resolve("/")); 
    if (this.defaultMessages != null)
      theme2.defaultMessages = this.defaultMessages; 
    if (this.localizedMessages != null)
      this.localizedMessages.forEach((paramString1, paramString2) -> paramTheme.localizedMessages.put(new Locale(paramString1), paramString2)); 
    this.locale = (Locale)this.frontEndSupport.localeProvider.get();
    this.theme = new BaseThemedAction.LocaleResolvedCachedTheme(this.locale, new CachedTheme(theme2), (new CachedTheme(theme1)).defaultProperties, this.frontEndSupport.messageProvider);
    this.fields = new ArrayList();
    if (this.application != null)
      if ("accountEdit".equals(this.template)) {
        this.step = 0;
        this.fields = new LinkedHashMap<>();
        if (this.application.formConfiguration.selfServiceFormId != null) {
          Form form = ((FormResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveForm(this.application.formConfiguration.selfServiceFormId))).form;
          Map<Integer, List> map = (Map)this.fields;
          for (byte b = 0; b < form.steps.size(); b++)
            map.put(Integer.valueOf(b), (List)((FormStep)form.steps.get(b)).fields.stream().map(paramUUID -> ((FormFieldResponse)superDelegate().execute(())).field).collect(Collectors.toList())); 
        } 
      } else if (this.application.registrationConfiguration.type == Application.RegistrationConfiguration.RegistrationType.advanced) {
        if (this.step == 0)
          this.step++; 
        Form form = ((FormResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveForm(this.application.registrationConfiguration.formId))).form;
        this.totalSteps = form.steps.size();
        handleAdvancedRegistrationFormFields(form);
      } else {
        this.readyToRegister = true;
        this.step = 0;
      }  
    if (this.showAlerts) {
      this.frontEndSupport.messageStore.add((Message)new SimpleMessage(MessageType.INFO, "[Invalid]", "This is an info alert."));
      this.frontEndSupport.messageStore.add((Message)new SimpleMessage(MessageType.ERROR, "[Invalid]", "This is an error alert."));
    } 
  }
}
