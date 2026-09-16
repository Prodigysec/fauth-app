package io.fusionauth.app.service.user;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.api.service.ImmutableLambdaArgument;
import io.fusionauth.api.domain.api.service.LambdaArgument;
import io.fusionauth.api.domain.api.service.MutableLambdaArgument;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.security.PhoneNumberValidator;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.api.service.consent.ConsentService;
import io.fusionauth.api.service.form.FormService;
import io.fusionauth.api.service.lambda.LambdaInvocationService;
import io.fusionauth.api.service.mfa.MFAService;
import io.fusionauth.api.service.system.CipherService;
import io.fusionauth.api.service.system.EventHelper;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.system.LambdaInvocationException;
import io.fusionauth.api.service.user.DefaultUserService;
import io.fusionauth.api.service.user.ExternalIdentifierService;
import io.fusionauth.api.service.user.PasswordService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.api.util.EmailTools;
import io.fusionauth.api.util.OAuthTools;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.client.LambdaDelegate;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.Consent;
import io.fusionauth.domain.ConsentStatus;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.PasswordType;
import io.fusionauth.domain.Requirable;
import io.fusionauth.domain.SendSetPasswordIdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserConsent;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.VerificationStrategy;
import io.fusionauth.domain.api.identity.verify.ExistingUserStrategy;
import io.fusionauth.domain.api.identity.verify.VerifyCompleteRequest;
import io.fusionauth.domain.api.identity.verify.VerifySendRequest;
import io.fusionauth.domain.api.identity.verify.VerifyStartRequest;
import io.fusionauth.domain.api.identity.verify.VerifyStartResponse;
import io.fusionauth.domain.api.user.RegistrationRequest;
import io.fusionauth.domain.api.user.RegistrationResponse;
import io.fusionauth.domain.event.UserLoginIdDuplicateOnCreateEvent;
import io.fusionauth.domain.event.UserLoginIdDuplicateOnUpdateEvent;
import io.fusionauth.domain.form.Form;
import io.fusionauth.domain.form.FormControl;
import io.fusionauth.domain.form.FormDataType;
import io.fusionauth.domain.form.FormField;
import io.fusionauth.domain.form.FormStep;
import io.fusionauth.domain.form.FormStepType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.security.UnauthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRegistrationFrontendService extends BaseFrontendFormService implements RegistrationFrontendService {
  public static final String PRE_VERIFIED_FORM_FIELD_NAME = "verificationCode";
  
  private static final Logger logger = LoggerFactory.getLogger(DefaultRegistrationFrontendService.class);
  
  private final CipherService cipherService;
  
  private final ConsentService consentService;
  
  private final ExpressionEvaluator expressionEvaluator;
  
  private final ExternalIdentifierService externalIdentifierService;
  
  private final FormService formService;
  
  private final FusionAuthClientProvider fusionAuthClientProvider;
  
  private final LambdaInvocationService lambdaInvocationService;
  
  private final MFAService mfaService;
  
  private final PasswordService passwordService;
  
  private final UserReaderService userReader;
  
  private final UserService userService;
  
  @Inject
  public DefaultRegistrationFrontendService(ExpressionEvaluator paramExpressionEvaluator, PasswordService paramPasswordService, FormService paramFormService, ConsentService paramConsentService, CipherService paramCipherService, LambdaInvocationService paramLambdaInvocationService, UserReaderService paramUserReaderService, UserService paramUserService, ExternalIdentifierService paramExternalIdentifierService, MFAService paramMFAService, FusionAuthClientProvider paramFusionAuthClientProvider) {
    this.expressionEvaluator = paramExpressionEvaluator;
    this.passwordService = paramPasswordService;
    this.formService = paramFormService;
    this.consentService = paramConsentService;
    this.cipherService = paramCipherService;
    this.lambdaInvocationService = paramLambdaInvocationService;
    this.userReader = paramUserReaderService;
    this.userService = paramUserService;
    this.externalIdentifierService = paramExternalIdentifierService;
    this.mfaService = paramMFAService;
    this.fusionAuthClientProvider = paramFusionAuthClientProvider;
  }
  
  private static void handleClientException(ClientResponse<?, ?> paramClientResponse) {
    if (paramClientResponse.status == 401)
      throw new UnauthenticatedException(); 
    if (paramClientResponse.status == 404)
      throw new NotFoundException(); 
    if (paramClientResponse.exception != null)
      throw new ErrorException("api-error", false); 
    Object object = paramClientResponse.errorResponse;
    if (object instanceof Errors) {
      Errors errors = (Errors)object;
      throw new RegistrationException(errors);
    } 
  }
  
  private static boolean isAnonymousUser(User paramUser) {
    return paramUser.identities.isEmpty();
  }
  
  public void completeVerification(String paramString1, String paramString2, EventInfo paramEventInfo) {
    VerifyCompleteRequest verifyCompleteRequest = (new VerifyCompleteRequest()).with(paramVerifyCompleteRequest -> paramVerifyCompleteRequest.verificationId = paramString).with(paramVerifyCompleteRequest -> paramVerifyCompleteRequest.oneTimeCode = paramString).with(paramVerifyCompleteRequest -> paramVerifyCompleteRequest.eventInfo = paramEventInfo);
    delegate().execute(paramFusionAuthClient -> paramFusionAuthClient.completeVerifyIdentity(paramVerifyCompleteRequest));
  }
  
  public RegistrationState decryptLegacyRegistrationState(Tenant paramTenant, Application paramApplication, String paramString, int paramInt, boolean paramBoolean) {
    LegacyRegistrationState legacyRegistrationState;
    try {
      legacyRegistrationState = this.cipherService.<LegacyRegistrationState>decrypt(paramString, LegacyRegistrationState.class);
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    } 
    User user = this.userReader.retrieveById(paramTenant.id, legacyRegistrationState.user.id);
    RegistrationState registrationState = registrationBegin(paramTenant, paramApplication, user, false);
    registrationState.user = legacyRegistrationState.user;
    registrationState.registration = legacyRegistrationState.registration;
    registrationState.consents = legacyRegistrationState.consents;
    if (registrationState.isBasicRegistration()) {
      registrationState.setStepIndex(0);
    } else {
      int i = paramInt - 1;
      if (i < 0 || i >= registrationState.getTotalSteps()) {
        registrationState.setStepIndex(0);
      } else {
        registrationState.setStepIndex(i);
      } 
    } 
    if (paramBoolean)
      registrationState.birthDateCollected(true); 
    return registrationState;
  }
  
  public RegistrationState decryptState(String paramString) {
    try {
      return this.cipherService.<RegistrationState>decrypt(paramString, RegistrationState.class);
    } catch (Exception exception) {
      throw new ErrorException(exception, new Object[] { "error" });
    } 
  }
  
  public String encryptState(RegistrationState paramRegistrationState) {
    try {
      return this.cipherService.encrypt(paramRegistrationState);
    } catch (Exception exception) {
      EventLogHelper.create(new EventLog(EventLogType.Error, "Failed encrypt registration state.", exception));
      throw new ErrorException("error");
    } 
  }
  
  public RegistrationFrontendService.ValidationResult normalizeAndValidateToCompleteRegistration(Tenant paramTenant, Application paramApplication, RegistrationState paramRegistrationState, User paramUser, UserRegistration paramUserRegistration, EventInfo paramEventInfo) {
    FormStepViewModel formStepViewModel = paramRegistrationState.getCurrentStep();
    List<FormField> list = formStepViewModel.fieldObjects;
    RegistrationFrontendService.ValidationResult validationResult = normalizeAndValidateFormFields(paramTenant, paramApplication, paramRegistrationState.user, paramRegistrationState.registration, paramUser, paramUserRegistration, paramRegistrationState.consents, list, paramEventInfo, false, (String)null);
    applySelfServiceRegistrationValidationLambda(validationResult, paramApplication, paramRegistrationState);
    return validationResult;
  }
  
  public RegistrationFrontendService.ValidationResult normalizeValidateAdvanced(Tenant paramTenant, Application paramApplication, RegistrationState paramRegistrationState, User paramUser, UserRegistration paramUserRegistration, String paramString, EventInfo paramEventInfo) {
    FormStepViewModel formStepViewModel = paramRegistrationState.getCurrentStep();
    List<FormField> list = formStepViewModel.fieldObjects;
    DefaultUserService.handleUserPrimaryIdentities(paramRegistrationState.user);
    RegistrationFrontendService.ValidationResult validationResult = normalizeAndValidateFormFields(paramTenant, paramApplication, paramRegistrationState.user, paramRegistrationState.registration, paramUser, paramUserRegistration, paramRegistrationState.consents, list, paramEventInfo, true, paramString);
    applySelfServiceRegistrationValidationLambda(validationResult, paramApplication, paramRegistrationState);
    return validationResult;
  }
  
  public RegistrationFrontendService.ComboRegistrationCompleteResult registerUser(Tenant paramTenant, Application paramApplication, User paramUser, UserRegistration paramUserRegistration, Map<UUID, List<String>> paramMap, RegistrationState paramRegistrationState, EventInfo paramEventInfo) {
    if (paramApplication.registrationConfiguration.type != Application.RegistrationConfiguration.RegistrationType.advanced) {
      paramUser.data.clear();
      paramUserRegistration = new UserRegistration();
    } 
    List<FormField> list = (paramApplication.registrationConfiguration.type == Application.RegistrationConfiguration.RegistrationType.advanced) ? paramRegistrationState.getForm().fieldsList().stream().filter(paramFormField -> !paramFormField.key.equals("verificationCode")).toList() : buildFormFieldsFromBasicConfiguration(paramApplication, false, paramRegistrationState
        .isParentEmailRequired());
    User user = merge(this.expressionEvaluator, paramUser, new User(), list);
    RegistrationRequest registrationRequest = new RegistrationRequest();
    registrationRequest.user = user;
    registrationRequest.sendSetPasswordIdentityType = SendSetPasswordIdentityType.doNotSend;
    registrationRequest.sendSetPasswordEmail = !paramApplication.verifyRegistration;
    registrationRequest.eventInfo = paramEventInfo;
    registrationRequest.registration = merge(this.expressionEvaluator, paramUserRegistration, new UserRegistration(), list);
    registrationRequest.registration.applicationId = paramApplication.id;
    registrationRequest.verificationIds = paramRegistrationState.getVerificationExternalIds();
    RegistrationResponse registrationResponse = delegate(paramTenant.id).execute(paramFusionAuthClient -> paramFusionAuthClient.register(paramUser.id, paramRegistrationRequest));
    RegistrationFrontendService.ComboRegistrationCompleteResult comboRegistrationCompleteResult = new RegistrationFrontendService.ComboRegistrationCompleteResult();
    comboRegistrationCompleteResult.user = registrationResponse.user;
    comboRegistrationCompleteResult.verificationIds = registrationResponse.verificationIds;
    if (paramApplication.registrationConfiguration.type == Application.RegistrationConfiguration.RegistrationType.advanced)
      handleConsents(paramApplication, comboRegistrationCompleteResult.user, paramRegistrationState.getForm(), paramMap); 
    comboRegistrationCompleteResult.user.getRegistrations().add(registrationResponse.registration);
    if (this.mfaService.isMethodRequired(paramTenant, paramApplication))
      comboRegistrationCompleteResult.twoFactorId = this.externalIdentifierService.createTwoFactor(paramTenant, paramApplication.id, comboRegistrationCompleteResult.user.id, null); 
    return comboRegistrationCompleteResult;
  }
  
  public RegistrationState registrationBegin(Tenant paramTenant, Application paramApplication, User paramUser, boolean paramBoolean) {
    Form form = null;
    if (paramApplication != null && paramApplication.registrationConfiguration.type == Application.RegistrationConfiguration.RegistrationType.advanced) {
      boolean bool1 = (paramApplication.registrationConfiguration.enabled || (paramBoolean && OAuthTools.isCompleteRegistrationAllowed(paramApplication, paramUser))) ? true : false;
      if (bool1) {
        form = this.formService.retrieveById(paramApplication.registrationConfiguration.formId);
        form.steps.removeIf(paramFormStep -> ((paramFormStep.type == FormStepType.verifyEmail && !paramTenant.emailConfiguration.verifyEmail) || (paramFormStep.type == FormStepType.verifyPhoneNumber && !paramTenant.phoneConfiguration.verifyPhoneNumber)));
      } 
    } 
    boolean bool = (paramTenant.familyConfiguration.enabled && (!paramTenant.familyConfiguration.allowChildRegistrations || paramTenant.familyConfiguration.parentEmailRequired)) ? true : false;
    if (paramUser == null || form == null) {
      FormViewModel formViewModel = Optional.<Form>ofNullable(form).map(FormViewModel::new).orElse((FormViewModel)null);
      return new RegistrationState(bool, formViewModel, paramTenant.familyConfiguration.allowChildRegistrations, paramTenant.familyConfiguration.maximumChildAge);
    } 
    Set set = (Set)this.consentService.retrieveUserConsentByUserId(paramUser.tenantId, paramUser.id).stream().map(paramUserConsent -> paramUserConsent.consentId).collect(Collectors.toSet());
    UserRegistration userRegistration = paramUser.getRegistrations().stream().filter(paramUserRegistration -> paramUserRegistration.applicationId.equals(paramApplication.id)).findFirst().orElse(new UserRegistration());
    form.steps.forEach(paramFormStep -> {
          paramFormStep.fieldObjects = paramFormStep.fieldObjects.stream().filter(()).filter(()).filter(()).toList();
          paramFormStep.fields = paramFormStep.fieldObjects.stream().map(()).toList();
        });
    RegistrationState registrationState = new RegistrationState(bool, new FormViewModel(form), paramTenant.familyConfiguration.allowChildRegistrations, paramTenant.familyConfiguration.maximumChildAge);
    registrationState.filterSteps(paramFormStepViewModel -> paramFormStepViewModel.fieldObjects.stream().filter(()).anyMatch(()));
    return registrationState;
  }
  
  public RegistrationState registrationBeginNewUser(Tenant paramTenant, Application paramApplication) {
    RegistrationState registrationState = registrationBegin(paramTenant, paramApplication, (User)null, false);
    if (registrationState.isBasicRegistration())
      return registrationState; 
    (registrationState.getForm()).steps
      
      .stream()
      
      .filter(paramFormStepViewModel -> ((Boolean)paramFormStepViewModel.getVerificationStrategy(paramTenant).<Boolean>map(()).orElse(Boolean.valueOf(false))).booleanValue())

      
      .forEach(paramFormStepViewModel -> {
          FormField formField = (new FormField()).with(()).with(()).with(());
          formField.normalize();
          paramFormStepViewModel.fieldObjects.add(formField);
        });
    return registrationState;
  }
  
  public RegistrationFrontendService.RegistrationCompleteResult registrationComplete(Tenant paramTenant, Application paramApplication, UUID paramUUID, RegistrationState paramRegistrationState) {
    RegistrationFrontendService.RegistrationCompleteResult registrationCompleteResult = new RegistrationFrontendService.RegistrationCompleteResult();
    User user = this.userReader.retrieveById(paramTenant.id, paramUUID);
    registrationCompleteResult.fullUser = user;
    registrationCompleteResult.user = (new User(user)).secure();
    registrationCompleteResult.user.getRegistrations().clear();
    registrationCompleteResult


      
      .registration = user.getRegistrations().stream().filter(paramUserRegistration -> paramUserRegistration.applicationId.equals(paramApplication.id)).findFirst().orElse(new UserRegistration());
    registrationCompleteResult.registered = (registrationCompleteResult.registration.applicationId != null);
    if (paramApplication.registrationConfiguration.type == Application.RegistrationConfiguration.RegistrationType.advanced) {
      Set set = (Set)this.consentService.retrieveUserConsentByUserId(user.tenantId, user.id).stream().map(paramUserConsent -> paramUserConsent.consentId).collect(Collectors.toSet());
      registrationCompleteResult



        
        .registrationComplete = paramRegistrationState.getForm().fieldsList().stream().filter(paramFormField -> missingFieldValue(paramUser, paramRegistrationCompleteResult.registration, paramFormField, paramSet)).noneMatch(paramFormField -> paramFormField.required);
    } else {
      BiFunction<Requirable, LocalDate, Boolean> biFunction = (paramRequirable, paramObject) -> Boolean.valueOf((paramRequirable.enabled && paramRequirable.required && (paramObject == null || (paramObject instanceof Collection && ((Collection)paramObject).isEmpty()))));
      boolean bool = (((Boolean)biFunction.apply(paramApplication.registrationConfiguration.birthDate, user.birthDate)).booleanValue() || ((Boolean)biFunction.apply(paramApplication.registrationConfiguration.firstName, user.firstName)).booleanValue() || ((Boolean)biFunction.apply(paramApplication.registrationConfiguration.fullName, user.fullName)).booleanValue() || ((Boolean)biFunction.apply(paramApplication.registrationConfiguration.lastName, user.lastName)).booleanValue() || ((Boolean)biFunction.apply(paramApplication.registrationConfiguration.middleName, user.middleName)).booleanValue() || ((Boolean)biFunction.apply(paramApplication.registrationConfiguration.mobilePhone, user.mobilePhone)).booleanValue() || ((Boolean)biFunction.apply(paramApplication.registrationConfiguration.preferredLanguages, user.preferredLanguages)).booleanValue()) ? true : false;
      registrationCompleteResult.registrationComplete = !bool;
    } 
    return registrationCompleteResult;
  }
  
  public IdentityVerificationState startAndSendVerification(Tenant paramTenant, Application paramApplication, RegistrationState paramRegistrationState, Map<String, Object> paramMap) {
    FormStepType formStepType = (paramRegistrationState.getCurrentStep()).type;
    Objects.requireNonNull(formStepType);
    UserIdentity userIdentity = (UserIdentity)paramRegistrationState.getUserIdentity().orElseThrow();
    VerifyStartRequest verifyStartRequest = (new VerifyStartRequest()).with(paramVerifyStartRequest -> paramVerifyStartRequest.applicationId = paramApplication.id).with(paramVerifyStartRequest -> paramVerifyStartRequest.loginId = paramUserIdentity.value).with(paramVerifyStartRequest -> paramVerifyStartRequest.loginIdType = paramUserIdentity.type.name).with(paramVerifyStartRequest -> paramVerifyStartRequest.state = paramMap).with(paramVerifyStartRequest -> paramVerifyStartRequest.existingUserStrategy = ExistingUserStrategy.mustNotExist);
    LambdaDelegate lambdaDelegate = delegate(paramTenant.id);
    VerifyStartResponse verifyStartResponse = lambdaDelegate.execute(paramFusionAuthClient -> paramFusionAuthClient.startVerifyIdentity(paramVerifyStartRequest));
    VerifySendRequest verifySendRequest = (new VerifySendRequest()).with(paramVerifySendRequest -> paramVerifySendRequest.verificationId = paramVerifyStartResponse.verificationId);
    FusionAuthClient fusionAuthClient = this.fusionAuthClientProvider.get(paramTenant.id);
    ClientResponse<Void, Errors> clientResponse = fusionAuthClient.sendVerifyIdentity(verifySendRequest);
    if (!clientResponse.wasSuccessful())
      if (clientResponse.status != 429)
        handleClientException(clientResponse);  
    IdentityVerificationState identityVerificationState = new IdentityVerificationState(verifyStartResponse.verificationId, userIdentity, (verifyStartResponse.oneTimeCode != null) ? VerificationStrategy.FormField : VerificationStrategy.ClickableLink, false);
    paramRegistrationState.identityVerificationStarted(formStepType, identityVerificationState);
    return identityVerificationState;
  }
  
  public UserService.UserResult updateUserForCompleteRegistration(Tenant paramTenant, Application paramApplication, User paramUser, UserRegistration paramUserRegistration, FormViewModel paramFormViewModel, Map<UUID, List<String>> paramMap, EventInfo paramEventInfo) {
    List<FormField> list = (paramApplication.registrationConfiguration.type == Application.RegistrationConfiguration.RegistrationType.advanced) ? paramFormViewModel.fieldsList() : buildFormFieldsFromBasicConfiguration(paramApplication, true, false);
    User user1 = this.userReader.retrieveById(paramTenant.id, paramUser.id);
    User user2 = merge(this.expressionEvaluator, paramUser, (new User(user1)).secure(), list);
    user2.email = (user1.email == null) ? user2.email : user1.email;
    user2.phoneNumber = (user1.phoneNumber == null) ? user2.phoneNumber : user1.phoneNumber;
    user2.username = (user1.username == null) ? user2.username : user1.username;
    DefaultUserService.handleUserPrimaryIdentities(user2);
    this.userService.update(paramTenant, paramApplication, user1, user2, true, PasswordType.PLAINTEXT, paramEventInfo);
    UserService.UserResult userResult = new UserService.UserResult();
    userResult.user = user2;
    if (paramApplication.registrationConfiguration.type == Application.RegistrationConfiguration.RegistrationType.advanced) {
      UserRegistration userRegistration = this.userReader.retrieveRegistration(paramTenant.id, paramUser.id, paramApplication.id);
      if (userRegistration == null) {
        UserRegistration userRegistration1 = merge(this.expressionEvaluator, paramUserRegistration, new UserRegistration(), list);
        userRegistration1.applicationId = paramApplication.id;
        this.userService.createRegistration(paramTenant, paramApplication, user2, userRegistration1, Collections.emptyList(), false, false, false, paramEventInfo);
        user2.getRegistrations().add(userRegistration1);
        userResult.registration = userRegistration1;
      } else {
        UserRegistration userRegistration1 = merge(this.expressionEvaluator, paramUserRegistration, new UserRegistration(userRegistration), list);
        Map map = (Map)paramApplication.roles.stream().collect(Collectors.toMap(paramApplicationRole -> paramApplicationRole.name, paramApplicationRole -> paramApplicationRole));
        Objects.requireNonNull(map);
        Set<ApplicationRole> set = (Set)userRegistration.roles.stream().map(map::get).collect(Collectors.toSet());
        this.userService.updateRegistration(paramTenant, paramApplication, user2, userRegistration, userRegistration1, set, false, paramEventInfo);
        userResult.registration = userRegistration1;
      } 
      if (!paramMap.isEmpty())
        handleConsents(paramApplication, user2, paramFormViewModel, paramMap); 
    } else {
      user2.data.clear();
      boolean bool = (user1.getRegistrationForApplication(paramApplication.id) != null) ? true : false;
      if (!bool) {
        UserRegistration userRegistration = (new UserRegistration()).with(paramUserRegistration -> paramUserRegistration.applicationId = paramApplication.id);
        this.userService.createRegistration(paramTenant, paramApplication, user2, userRegistration, Collections.emptyList(), false, false, false, paramEventInfo);
        user2.getRegistrations().add(userRegistration);
        userResult.registration = userRegistration;
      } 
    } 
    return userResult;
  }
  
  private void addFieldToList(List<FormField> paramList, String paramString, Requirable paramRequirable, boolean paramBoolean) {
    if (!paramRequirable.enabled)
      return; 
    if (!paramBoolean || paramRequirable.required)
      paramList.add((new FormField()).with(paramFormField -> paramFormField.key = paramString)); 
  }
  
  private void allowedDomain(Validator paramValidator, Tenant paramTenant, String paramString1, String paramString2) {
    if (!paramTenant.registrationConfiguration.blockedDomains.isEmpty())
      paramValidator.ensure(!paramTenant.registrationConfiguration.blockedDomains.contains(EmailTools.getEmailDomain(paramString1)), paramString2, "[blocked]", new Object[0]); 
  }
  
  private void applySelfServiceRegistrationValidationLambda(RegistrationFrontendService.ValidationResult paramValidationResult, Application paramApplication, RegistrationState paramRegistrationState) {
    if (paramApplication.lambdaConfiguration.selfServiceRegistrationValidationId != null) {
      Form form = this.formService.retrieveById(paramApplication.registrationConfiguration.formId);
      int i = paramRegistrationState.getStepOriginal();
      RegistrationFrontendService.FormContext formContext = (new RegistrationFrontendService.FormContext()).with(paramFormContext -> paramFormContext.form = paramForm).with(paramFormContext -> paramFormContext.fields = ((FormStep)paramForm.steps.get(paramInt)).fieldObjects).with(paramFormContext -> paramFormContext.step = Integer.valueOf(paramInt + 1)).with(paramFormContext -> paramFormContext.stepIndex = Integer.valueOf(paramInt)).with(paramFormContext -> paramFormContext.totalSteps = Integer.valueOf(paramForm.steps.size()));
      RegistrationFrontendService.FormValidationResult formValidationResult = new RegistrationFrontendService.FormValidationResult();
      try {
        this.lambdaInvocationService.invoke(paramApplication.lambdaConfiguration.selfServiceRegistrationValidationId, new LambdaArgument[] { new MutableLambdaArgument(formValidationResult), new ImmutableLambdaArgument((new User(paramRegistrationState.user))
                
                .secure()), new ImmutableLambdaArgument(paramRegistrationState.registration), new ImmutableLambdaArgument(formContext, true) });
      } catch (LambdaInvocationException lambdaInvocationException) {
        logger.debug("Error while applying lambda for self-service registration validation.", (Throwable)lambdaInvocationException);
        formValidationResult.errors.addGeneralError("[SelfServiceCustomValidationException]", null, new Object[0]);
      } 
      paramValidationResult.errors.add(formValidationResult.errors);
    } 
  }
  
  private List<FormField> buildFormFieldsFromBasicConfiguration(Application paramApplication, boolean paramBoolean1, boolean paramBoolean2) {
    ArrayList<FormField> arrayList = new ArrayList();
    addFieldToList(arrayList, "user.birthDate", paramApplication.registrationConfiguration.birthDate, paramBoolean1);
    addFieldToList(arrayList, "user.firstName", paramApplication.registrationConfiguration.firstName, paramBoolean1);
    addFieldToList(arrayList, "user.fullName", paramApplication.registrationConfiguration.fullName, paramBoolean1);
    addFieldToList(arrayList, "user.lastName", paramApplication.registrationConfiguration.lastName, paramBoolean1);
    addFieldToList(arrayList, "user.middleName", paramApplication.registrationConfiguration.middleName, paramBoolean1);
    addFieldToList(arrayList, "user.mobilePhone", paramApplication.registrationConfiguration.mobilePhone, paramBoolean1);
    addFieldToList(arrayList, "user.preferredLanguages", paramApplication.registrationConfiguration.preferredLanguages, paramBoolean1);
    if (!paramBoolean1) {
      arrayList.add((new FormField()).with(paramFormField -> paramFormField.key = "user." + paramApplication.registrationConfiguration.loginIdType.name()));
      arrayList.add((new FormField()).with(paramFormField -> paramFormField.key = "user.password"));
    } 
    if (paramBoolean2)
      arrayList.add((new FormField()).with(paramFormField -> paramFormField.key = "user.parentEmail")); 
    return arrayList;
  }
  
  private boolean customValidate(FormField paramFormField, Object paramObject) {
    if (paramFormField.validator.expression != null) {
      if (paramObject instanceof String) {
        String str = (String)paramObject;
        return str.matches(paramFormField.validator.expression);
      } 
      return false;
    } 
    return true;
  }
  
  private LambdaDelegate delegate() {
    return delegate((UUID)null);
  }
  
  private LambdaDelegate delegate(UUID paramUUID) {
    FusionAuthClient fusionAuthClient = this.fusionAuthClientProvider.get(paramUUID);
    return new LambdaDelegate(fusionAuthClient, paramClientResponse -> paramClientResponse.successResponse, DefaultRegistrationFrontendService::handleClientException);
  }
  
  private Object getValue(ExpressionEvaluator paramExpressionEvaluator, User paramUser, UserRegistration paramUserRegistration, FormField paramFormField) {
    Object object = paramFormField.key.startsWith("user.") ? paramUser : paramUserRegistration;
    String str = paramFormField.key.startsWith("user.") ? paramFormField.key.substring("user.".length()) : paramFormField.key.substring("registration.".length());
    return (object == null) ? null : paramExpressionEvaluator.getValue(str, object);
  }
  
  private void handleConsents(Application paramApplication, User paramUser, FormViewModel paramFormViewModel, Map<UUID, List<String>> paramMap) {
    List<UserConsent> list = this.consentService.retrieveUserConsentByUserId(paramApplication.tenantId, paramUser.id);
    List list1 = paramFormViewModel.steps.stream().flatMap(paramFormStepViewModel -> paramFormStepViewModel.fieldObjects.stream()).filter(paramFormField -> (paramFormField.type == FormDataType.consent)).filter(paramFormField -> paramList.stream().noneMatch(())).toList();
    for (FormField formField : list1) {
      Consent consent = this.consentService.retrieveConsentById(formField.consentId);
      List list2 = consent.values.isEmpty() ? Collections.emptyList() : paramMap.getOrDefault(formField.consentId, Collections.emptyList());
      List list3 = (List)list2.stream().filter(paramString -> paramConsent.values.contains(paramString)).collect(Collectors.toList());
      this.consentService.createUserConsentWithStatus((new UserConsent()).with(paramUserConsent -> paramUserConsent.consentId = paramFormField.consentId)
          .with(paramUserConsent -> paramUserConsent.giverUserId = paramUser.id)
          .with(paramUserConsent -> paramUserConsent.status = paramMap.containsKey(paramFormField.consentId) ? ConsentStatus.Active : ConsentStatus.Revoked)
          .with(paramUserConsent -> paramUserConsent.values = paramList)
          .with(paramUserConsent -> paramUserConsent.userId = paramUser.id), false);
    } 
  }
  
  private boolean isNullOrEmptyString(Object paramObject) {
    return (paramObject == null || (paramObject instanceof String && paramObject.toString().equals("")));
  }
  
  private boolean missingFieldValue(User paramUser, UserRegistration paramUserRegistration, FormField paramFormField, Set<UUID> paramSet) {
    if (paramFormField.key.startsWith("consents"))
      return !paramSet.contains(paramFormField.consentId); 
    Object object = getValue(this.expressionEvaluator, paramUser, paramUserRegistration, paramFormField);
    if (object == null)
      return true; 
    if (object instanceof String) {
      String str = (String)object;
      return (str.trim().length() == 0);
    } 
    if (object instanceof Collection) {
      Collection collection = (Collection)object;
      return collection.isEmpty();
    } 
    return false;
  }
  
  private RegistrationFrontendService.ValidationResult normalizeAndValidateFormFields(Tenant paramTenant, Application paramApplication, User paramUser1, UserRegistration paramUserRegistration1, User paramUser2, UserRegistration paramUserRegistration2, Map<UUID, List<String>> paramMap, List<FormField> paramList, EventInfo paramEventInfo, boolean paramBoolean, String paramString) {
    Validator validator = new Validator();
    for (FormField formField : paramList) {
      Object object1, object2 = null;
      if (formField.key.equals("verificationCode")) {
        object1 = paramString;
      } else {
        object1 = (formField.type == FormDataType.consent) ? paramMap.get(formField.consentId) : getValue(this.expressionEvaluator, paramUser1, paramUserRegistration1, formField);
        object2 = object1;
      } 
      Object object = formField.confirm ? getValue(this.expressionEvaluator, paramUser2, paramUserRegistration2, formField) : null;
      String str = formField.key;
      if (object1 != null && object1.toString().equals(""))
        object1 = null; 
      Errors errors = validateField(paramTenant, paramApplication, paramUser1, paramUserRegistration1, formField, str, object1, object, paramEventInfo, paramBoolean);
      if (errors.empty()) {
        validator.ensure(normalizeField(paramUser1, paramUserRegistration1, formField, object1, object2), str, "[invalid]", new Object[] { "The value could not be converted to [" + String.valueOf(formField.type) + "]." });
        continue;
      } 
      validator.withErrors(errors);
    } 
    RegistrationFrontendService.ValidationResult validationResult = new RegistrationFrontendService.ValidationResult();
    validationResult.errors = validator.done();
    return validationResult;
  }
  
  private boolean normalizeField(User paramUser, UserRegistration paramUserRegistration, FormField paramFormField, Object paramObject1, Object paramObject2) {
    if (paramObject2 == null)
      return true; 
    Object object = paramFormField.key.startsWith("user.") ? paramUser : paramUserRegistration;
    String str = paramFormField.key.startsWith("user.") ? paramFormField.key.substring("user.".length()) : paramFormField.key.substring("registration.".length());
    if (isNullOrEmptyString(paramObject1)) {
      this.expressionEvaluator.setValue(str, object, null);
      return true;
    } 
    return handleConversion(this.expressionEvaluator, str, paramFormField, object, paramObject1);
  }
  
  private Errors validateField(Tenant paramTenant, Application paramApplication, User paramUser, UserRegistration paramUserRegistration, FormField paramFormField, String paramString, Object paramObject1, Object paramObject2, EventInfo paramEventInfo, boolean paramBoolean) {
    boolean bool = (paramFormField.type == FormDataType.email || paramFormField.type == FormDataType.string) ? true : false;
    return (new Validator())

      
      .ifTrue(paramFormField.required, paramValidator -> paramValidator.ifTrue(paramBoolean, ()).ifFalse(paramBoolean, ()).ifTrue((paramFormField.type != FormDataType.consent && paramObject instanceof Collection), ()))






      
      .ifTrue((paramFormField.type != FormDataType.consent), paramValidator -> paramValidator.ifLastCheckHadNoError(()))




      
      .ifNoErrors(paramValidator -> paramValidator.ifTrue(paramFormField.confirm, ()))

      
      .ifTrue((paramFormField.type == FormDataType.email && paramObject1 != null), paramValidator -> paramValidator.email((String)paramObject, paramString, new Object[0]))

      
      .ifTrue((paramFormField.type == FormDataType.phoneNumber && paramObject1 != null && !paramObject1.equals("")), paramValidator -> paramValidator.ensure(PhoneNumberValidator.validateE164format((String)paramObject), paramString, "[invalid]", new Object[0]))

      
      .ifTrue(paramFormField.key.equals("user.email"), paramValidator -> paramValidator.ifLastCheckHadNoError(()).ifLastCheckHadNoError(()))





























      
      .ifTrue(paramFormField.key.equals("user.username"), paramValidator -> paramValidator.ifFalse(paramTenant.usernameConfiguration.unique.enabled, ()).ifNoFieldErrors("user.username", ()))



























      
      .ifTrue(paramFormField.key.equals("user.phoneNumber"), paramValidator -> paramValidator.holdMyBeer(this.userReader.retrieveByLoginId(paramTenant.id, (String)paramObject, List.of(IdentityType.phoneNumber))).notDuplicate(paramValidator.barkeep(), "user.phoneNumber", new Object[0]).ifLastCheckHadError(()))



























      
      .ifTrue(paramFormField.key.equals("registration.username"), paramValidator -> paramValidator.validate(()))

      
      .ifTrue((paramFormField.control == FormControl.password && paramObject1 != null), paramValidator -> paramValidator.withErrors(this.passwordService.validatePasswordOnCreate(paramTenant, paramUser, "user.password", (String)paramObject)))


      
      .ifTrue((paramFormField.key.equals("user.mobilePhone") && paramObject1 != null && !paramObject1.equals("")), paramValidator -> paramValidator.ensure(PhoneNumberValidator.validateE164format(paramUser.mobilePhone), "user.mobilePhone", "[invalid]", new Object[0]))

      
      .ifNoErrors(paramValidator -> paramValidator.ifTrue((paramFormField.validator.enabled && paramObject != null), ()))
      
      .done();
  }
}
