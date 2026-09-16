package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import com.inversoft.util.SecurityTools;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.IdentityExternalIdHelper;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.api.service.cache.IdentityProviderCache;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.app.domain.FormStepInfo;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.app.service.user.FormStepViewModel;
import io.fusionauth.app.service.user.IdentityVerificationState;
import io.fusionauth.app.service.user.RegistrationException;
import io.fusionauth.app.service.user.RegistrationFrontendService;
import io.fusionauth.app.service.user.RegistrationState;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.PasswordValidationRules;
import io.fusionauth.domain.User;
import io.fusionauth.domain.VerificationStrategy;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.form.FormStepType;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.BaseIdentityProviderApplicationConfiguration;
import io.fusionauth.http.Cookie;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.parameter.annotation.PreRenderMethod;
import org.primeframework.mvc.scope.annotation.ManagedSessionCookie;
import org.primeframework.mvc.util.QueryStringBuilder;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.annotation.PostValidationMethod;
import org.primeframework.mvc.validation.annotation.PreValidationMethod;

@Action
@Redirect(code = "child-registration-not-allowed", uri = "/oauth2/child-registration-not-allowed?client_id=${client_id}&tenantId=${tenantId}")
public class RegisterAction extends BaseRegisterAction {
  public static final String PREVERIFY_STATE_KEY_REDIRECT_DESTINATION = "redirectDestination";
  
  private static final String PRE_VERIFY_STATE_KEY_REGISTRATION_STATE = "registrationState";
  
  private final IdentityProviderCache identityProviderCache;
  
  public String externalId;
  
  @ManagedSessionCookie(name = "federated.csrf", encrypt = false)
  public Cookie federatedCSRF;
  
  @FTLVariable
  public String federatedCSRFToken;
  
  @FTLVariable
  public FormStepInfo formStep;
  
  public boolean hideBirthDate;
  
  public Map<String, List<BaseIdentityProvider<?>>> identityProviders;
  
  public String passwordConfirm;
  
  public PasswordValidationRules passwordValidationRules;
  
  public boolean rememberDevice = true;
  
  public boolean resend;
  
  @FTLVariable
  public String verificationCode;
  
  private boolean writeTrustCookie;
  
  @Inject
  public RegisterAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, IdentityProviderCache paramIdentityProviderCache, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, RegistrationFrontendService paramRegistrationFrontendService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramOAuthService, paramRegistrationFrontendService, paramLoginIntentService, paramSSOService, paramThreatDetectionService, paramUserReaderService, paramUserService);
    this.identityProviderCache = paramIdentityProviderCache;
  }
  
  public String get() {
    if (((Boolean)this.registrationState.getVerificationStateForCurrentStep()
      .<Boolean>map(paramIdentityVerificationState -> Boolean.valueOf((paramIdentityVerificationState.verificationStrategy() == VerificationStrategy.ClickableLink)))
      .orElse(Boolean.valueOf(false))).booleanValue())
      return nextStep(); 
    return "input";
  }
  
  @PostParameterMethod
  public void initialize() {
    persistRememberDeviceChoice(this.rememberDevice, this.frontEndSupport.isPOST());
    resolveUserAndZoneId();
  }
  
  @PreValidationMethod
  public void initializeRegistrationState() {
    if (this.registrationState == null)
      this.registrationState = this.registrationFrontendService.registrationBeginNewUser(this.codeTenant, this.codeApplication); 
  }
  
  @FTLVariable
  public boolean isAdvancedRegistration() {
    return !this.registrationState.isBasicRegistration();
  }
  
  @FTLVariable
  public boolean isCollectBirthDate() {
    return this.registrationState.isCollectBirthDate();
  }
  
  @FTLVariable
  public boolean isDeadEnd() {
    return ((Boolean)this.registrationState.getVerificationStateForCurrentStep()
      .<Boolean>map(paramIdentityVerificationState -> Boolean.valueOf((paramIdentityVerificationState.verificationStrategy() == VerificationStrategy.ClickableLink)))
      .orElse(Boolean.valueOf(false))).booleanValue();
  }
  
  @FTLVariable
  public boolean isParentEmailRequired() {
    return this.registrationState.isParentEmailRequired();
  }
  
  @FTLVariable
  public boolean isReadyToRegister() {
    return (this.registrationState.isBasicRegistration() || (this.registrationState

      
      .isLastActiveStep() && getCurrentStep().isGeneralMessagesSeen()));
  }
  
  public String post() {
    RegistrationState.ChildStatus childStatus = this.registrationState.getChildStatus();
    switch (childStatus) {
      case ClickableLink:
        return "child-registration-not-allowed";
      case FormField:
        this.hideBirthDate = true;
      case null:
        if (this.codeApplication.registrationConfiguration.type == Application.RegistrationConfiguration.RegistrationType.basic)
          return "input"; 
        break;
    } 
    if (!this.registrationState.isBasicRegistration() && getCurrentStep().isVerificationStep()) {
      if (this.resend) {
        startPreVerification();
        return "input";
      } 
      if (!completeFormFieldVerification())
        return "input"; 
    } 
    return nextStep();
  }
  
  @PreRenderMethod({ThemedForward.class})
  public void prePageRenderSetup() {
    this.rememberDevice = getRememberDeviceCookieValue(this.rememberDevice, this.frontEndSupport.isGET());
    if (this.codeTenant == null)
      return; 
    this.showCaptcha = this.threatDetectionService.isCaptchaEnabled(this.codeTenant, (this.frontEndSupport.getReactorStatus()).threatDetection);
    if (this.registrationState.isBasicRegistration())
      return; 
    if (getCurrentStep().isVerificationStep())
      addPreVerifiedPrompts(); 
  }
  
  @PostValidationMethod
  public void setupPasswordValidationRules() {
    if (this.codeTenant != null)
      this.passwordValidationRules = this.codeTenant.passwordValidationRules; 
    this






      
      .identityProviders = (this.pendingIdPLink == null) ? (Map<String, List<BaseIdentityProvider<?>>>)this.identityProviderCache.getByApplicationId((this.codeTenant != null) ? this.codeTenant.id : null, this.application.id).stream().filter(paramBaseIdentityProvider -> paramBaseIdentityProvider.isEnabledForApplicationId(this.application.id)).filter(paramBaseIdentityProvider -> ((BaseIdentityProviderApplicationConfiguration)paramBaseIdentityProvider.applicationConfiguration.get(this.application.id)).createRegistration).filter(paramBaseIdentityProvider -> !isDomainBasedOrPasswordless(paramBaseIdentityProvider)).filter(paramBaseIdentityProvider -> (this.devicePendingIdPLink == null || !this.devicePendingIdPLink.identityProviderId.equals(paramBaseIdentityProvider.id))).collect(Collectors.groupingBy(paramBaseIdentityProvider -> paramBaseIdentityProvider.getType().toString())) : Collections.<String, List<BaseIdentityProvider<?>>>emptyMap();
    this.federatedCSRFToken = SecurityTools.secureRandom(12);
    if (this.identityProviders.size() > 0)
      this.federatedCSRF.value = this.federatedCSRFToken; 
    this.idpRedirectState = this.oauthService.encodeStateForRedirect(paramQueryStringBuilder -> addBaseParameters(paramQueryStringBuilder).with("csrf", this.federatedCSRF.value));
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    commonValidation();
    if (this.externalId != null)
      resumeFromClickableLink(); 
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    commonValidation();
    if (isReadyToRegister())
      this.writeTrustCookie = validateCaptcha(this.codeTenant, null, null, this.captcha_token, false); 
    if (this.registrationState.isCollectBirthDate() && this.codeApplication.registrationConfiguration.type == Application.RegistrationConfiguration.RegistrationType.basic) {
      if ((getUser()).birthDate == null)
        addFieldError("user.birthDate", "[missing]user.birthDate", new Object[0]); 
      return;
    } 
    clearUser();
    User user = getUser();
    user.twoFactor.methods.clear();
    user.normalize();
    if (!this.registrationState.isBasicRegistration()) {
      if (getCurrentStep().isVerificationStep() && this.resend)
        return; 
      RegistrationFrontendService.ValidationResult validationResult = this.registrationFrontendService.normalizeValidateAdvanced(this.codeTenant, this.codeApplication, this.registrationState, this.confirm.user, this.confirm.registration, this.verificationCode, this.frontEndSupport

          
          .buildEventInfo(this.metaData));
      transferErrors(validationResult.errors);
      if (!validationResult.errors.empty())
        return; 
    } 
    if (!this.registrationState.isLastStep())
      return; 
    normalizeAndValidateWithRegistrationConfiguration(getUser(), true, this.passwordConfirm, this.registrationState

        
        .isParentEmailRequired());
  }
  
  private void addPreVerifiedPrompts() {
    this.registrationState.getVerificationStateForCurrentStep()
      .map(IdentityVerificationState::verificationStrategy)
      .ifPresent(paramVerificationStrategy -> {
          FormStepType formStepType = (getCurrentStep()).type;
          boolean bool = ((IdentityVerificationState)this.registrationState.getVerificationStateForCurrentStep().get()).completed();
          switch (formStepType) {
            default:
              throw new MatchException(null, null);
            case ClickableLink:
              throw new IllegalArgumentException("collectData is not a pre-verification step");
            case FormField:
            
            case null:
              break;
          } 
          String str = "phone";
          if (bool) {
            this.formStep = new FormStepInfo(this.theme.message("{description}-registration-ready", new Object[0]), null);
            return;
          } 
          switch (paramVerificationStrategy) {
            default:
              throw new MatchException(null, null);
            case ClickableLink:
            
            case FormField:
              break;
          } 
          this.formStep = new FormStepInfo(this.theme.message("{description}%s-verification-required".formatted(new Object[] { str }, ), new Object[0]), this.theme.message("%s-verification-required-send-another".formatted(new Object[] { str }, ), new Object[0]));
        });
  }
  
  private void commonValidation() {
    String str = validateAndHandleErrors(true);
    if (str != null)
      throw new ErrorException(str, false); 
    if (!this.codeApplication.registrationConfiguration.enabled || this.ssoSession.user != null) {
      buildRedirectToAuthorizeURI();
      throw new ErrorException("redirect-to-authorize", false);
    } 
    if (this.devicePendingIdPLink != null && this.identityProviders != null)
      this.identityProviders.remove(this.devicePendingIdPLink.identityProviderType.toString()); 
  }
  
  private boolean completeFormFieldVerification() {
    FormStepViewModel formStepViewModel = getCurrentStep();
    FormStepType formStepType = formStepViewModel.type;
    IdentityVerificationState identityVerificationState = (IdentityVerificationState)this.registrationState.getVerificationState(formStepType).orElseThrow();
    if (identityVerificationState.completed())
      return true; 
    if (identityVerificationState.verificationStrategy() != VerificationStrategy.FormField)
      return false; 
    try {
      this.registrationFrontendService.completeVerification(identityVerificationState.externalId(), this.verificationCode, this.frontEndSupport
          
          .buildEventInfo(this.metaData));
    } catch (RegistrationException registrationException) {
      if (registrationException.errors.fieldErrors.containsKey("oneTimeCode")) {
        addFieldError("verificationCode", "[invalid]code", new Object[0]);
      } else {
        transferErrors(registrationException.errors);
      } 
      return false;
    } catch (NotFoundException notFoundException) {
      addFieldError("verificationCode", "[invalid]code", new Object[0]);
      return false;
    } 
    this.verificationCode = null;
    addGeneralInfo((formStepType == FormStepType.verifyEmail) ? "email-verification-complete" : "phone-verification-complete", new Object[0]);
    this.registrationState.identityVerificationCompleted(formStepType, identityVerificationState);
    return true;
  }
  
  private String doRegistration() {
    RegistrationFrontendService.ComboRegistrationCompleteResult comboRegistrationCompleteResult;
    try {
      comboRegistrationCompleteResult = this.registrationFrontendService.registerUser(this.codeTenant, this.codeApplication, getUser(), getRegistration(), getConsents(), this.registrationState, this.frontEndSupport
          .buildEventInfo(this.metaData));
    } catch (RegistrationException registrationException) {
      if (registrationException.errors.fieldErrors.containsKey("verificationIds")) {
        addGeneralInfo("[VerificationTimeout]", new Object[0]);
      } else {
        transferErrors(registrationException.errors);
      } 
      return "input";
    } 
    User user = comboRegistrationCompleteResult.user;
    if (comboRegistrationCompleteResult.verificationIds != null)
      comboRegistrationCompleteResult.verificationIds.stream()
        
        .filter(paramVerificationId -> (paramVerificationId.type.is(IdentityType.email) && paramVerificationId.value.equals(paramUser.email)))
        .findFirst()

        
        .ifPresent(paramVerificationId -> allowConfirmationBypass()); 
    if (this.writeTrustCookie) {
      SSOService.CookieValue cookieValue = this.ssoService.buildTrustedDeviceCookie(this.tenant, null, user.id, true);
      this.frontEndSupport.addHttpOnlyPersistentCookie(cookieValue.name, cookieValue.value);
    } 
    SSOService.NewDeviceResult newDeviceResult = this.ssoService.handleNewDevice(this.frontEndSupport.request.getCookies(), this.codeTenant, user.id);
    this.frontEndSupport.addHttpOnlyPersistentCookie(newDeviceResult.cookieName, newDeviceResult.cookieValue);
    if (comboRegistrationCompleteResult.twoFactorId != null) {
      this.twoFactorId = comboRegistrationCompleteResult.twoFactorId;
      buildRedirectToTwoFactorEnableURI();
      return "redirect-to-two-factor-enable";
    } 
    return (handlePostAuthenticationRedirect(user, getRememberDeviceState())).step.getResultCode();
  }
  
  private String nextStep() {
    if (this.registrationState.isLastActiveStep() && !this.registrationState.isBasicRegistration() && !getCurrentStep().isGeneralMessagesSeen()) {
      getCurrentStep().acknowledge();
      return "input";
    } 
    while (!this.registrationState.isLastStep()) {
      this.registrationState.nextStep();
      FormStepViewModel formStepViewModel = getCurrentStep();
      if (formStepViewModel.skippable)
        continue; 
      if (formStepViewModel.isVerificationStep())
        if (!((Boolean)this.registrationState.getVerificationStateForCurrentStep().<Boolean>map(IdentityVerificationState::completed).orElse(Boolean.valueOf(false))).booleanValue()) {
          startPreVerification();
          return "input";
        }  
      return "input";
    } 
    return doRegistration();
  }
  
  private void resumeFromClickableLink() {
    ExternalIdentifier externalIdentifier = this.frontEndSupport.getExternalId(this.tenantId, this.externalId, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.EmailVerification, ExternalIdentifier.ExternalIdType.PhoneVerification });
    this.registrationState = (RegistrationState)this.frontEndSupport.objectMapper.convertValue(externalIdentifier.getStateHelper().get("registrationState"), RegistrationState.class);
    FormStepType formStepType = (getCurrentStep()).type;
    IdentityVerificationState identityVerificationState = new IdentityVerificationState(this.externalId, IdentityExternalIdHelper.getLoginId(externalIdentifier), VerificationStrategy.ClickableLink, false);
    this.registrationState.identityVerificationStarted(formStepType, identityVerificationState);
    this.registrationState.identityVerificationCompleted(formStepType, identityVerificationState);
    addGeneralInfo(((getCurrentStep()).type == FormStepType.verifyEmail) ? "email-verification-complete" : "phone-verification-complete", new Object[0]);
  }
  
  private void startPreVerification() {
    Map<String, Object> map = null;
    boolean bool = (getCurrentStep().getVerificationStrategy(this.codeTenant).orElseThrow() == VerificationStrategy.ClickableLink) ? true : false;
    if (bool) {
      String str = baseQueryBuilder("/oauth2/register").build();
      map = Map.of("redirectDestination", str, "registrationState", this.registrationState);
    } 
    this.registrationFrontendService.startAndSendVerification(this.codeTenant, this.codeApplication, this.registrationState, map);
    if (bool)
      allowConfirmationBypass(); 
    FormStepType formStepType = (getCurrentStep()).type;
    addGeneralInfo((formStepType == FormStepType.verifyEmail) ? "[EmailVerificationSent]" : "[PhoneVerificationSent]", new Object[0]);
  }
}
