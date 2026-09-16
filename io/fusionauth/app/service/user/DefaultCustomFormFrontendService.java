package io.fusionauth.app.service.user;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.Pair;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.consent.ConsentService;
import io.fusionauth.api.service.form.FormService;
import io.fusionauth.api.service.form.FormTools;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.client.LambdaDelegate;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ChangePasswordReason;
import io.fusionauth.domain.Consent;
import io.fusionauth.domain.ConsentStatus;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserConsent;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.UserConsentRequest;
import io.fusionauth.domain.api.UserConsentResponse;
import io.fusionauth.domain.api.UserRequest;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.api.user.RegistrationRequest;
import io.fusionauth.domain.api.user.RegistrationResponse;
import io.fusionauth.domain.form.FormDataType;
import io.fusionauth.domain.form.FormField;
import io.fusionauth.domain.form.FormStep;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

public class DefaultCustomFormFrontendService extends BaseFrontendFormService implements CustomFormFrontendService {
  protected final ApplicationReaderService applicationReader;
  
  protected final FusionAuthClient client;
  
  protected final ConsentService consentService;
  
  protected final ExpressionEvaluator expressionEvaluator;
  
  protected final FormService formService;
  
  @Inject
  public DefaultCustomFormFrontendService(ApplicationReaderService paramApplicationReaderService, FusionAuthClient paramFusionAuthClient, ConsentService paramConsentService, ExpressionEvaluator paramExpressionEvaluator, FormService paramFormService) {
    this.applicationReader = paramApplicationReaderService;
    this.client = paramFusionAuthClient;
    this.consentService = paramConsentService;
    this.expressionEvaluator = paramExpressionEvaluator;
    this.formService = paramFormService;
  }
  
  public boolean checkIfUserCanEditRoles(UUID paramUUID1, User paramUser, UUID paramUUID2) {
    SortedSet<String> sortedSet = (paramUser.getRegistrationForApplication(Application.FUSIONAUTH_APP_ID)).roles;
    boolean bool1 = sortedSet.contains("admin");
    boolean bool2 = sortedSet.contains("user_manager");
    if (!bool1 && !bool2)
      return false; 
    if (!Application.FUSIONAUTH_APP_ID.equals(paramUUID1))
      return true; 
    return (bool1 || !paramUUID2.equals(paramUser.id));
  }
  
  public CustomFormFrontendService.ValidationResult<User> normalizeAndValidate(Tenant paramTenant, User paramUser1, User paramUser2, User paramUser3, CustomFormFrontendService.EditPasswordOption paramEditPasswordOption) {
    ArrayList<FormField> arrayList = new ArrayList<>(this.formService.retrieveFieldsByFormId(paramTenant.formConfiguration.adminUserFormId));
    User user1 = retrieveUser(paramTenant, paramUser1.id);
    boolean bool = true;
    if (user1.id != null) {
      SortedSet<String> sortedSet = (paramUser3.getRegistrationForApplication(Application.FUSIONAUTH_APP_ID)).roles;
      bool = (sortedSet.contains("admin") || sortedSet.contains("user_manager") || (sortedSet.contains("user_support_manager") && user1.id.equals(paramUser3.id))) ? true : false;
      if (!bool)
        arrayList.removeIf(paramFormField -> (paramFormField.key.equals("user.email") || paramFormField.key.equals("user.username") || paramFormField.key.equals("user.phoneNumber"))); 
    } 
    arrayList.removeIf(paramFormField -> paramFormField.key.equals("user.twoFactorEnabled"));
    User user2 = merge(this.expressionEvaluator, paramUser1, user1, arrayList);
    handleEditPasswordOptions(paramEditPasswordOption, bool, arrayList, user2);
    return normalizeAndValidateFormFields(user2, paramUser2, arrayList);
  }
  
  public CustomFormFrontendService.ValidationResult<UserRegistration> normalizeAndValidateAdd(Tenant paramTenant, UserRegistration paramUserRegistration1, UserRegistration paramUserRegistration2, User paramUser, UUID paramUUID) {
    return normalizeAndValidate(paramTenant, paramUserRegistration1, paramUserRegistration2, paramUser, paramUUID, true);
  }
  
  public CustomFormFrontendService.ValidationResult<UserRegistration> normalizeAndValidateEdit(Tenant paramTenant, UserRegistration paramUserRegistration1, UserRegistration paramUserRegistration2, User paramUser, UUID paramUUID) {
    return normalizeAndValidate(paramTenant, paramUserRegistration1, paramUserRegistration2, paramUser, paramUUID, false);
  }
  
  public CustomFormFrontendService.UserValidationResult normalizeAndValidateSelfService(Tenant paramTenant, Application paramApplication, User paramUser1, User paramUser2, UserRegistration paramUserRegistration1, UserRegistration paramUserRegistration2, Map<UUID, List<String>> paramMap, User paramUser3, CustomFormFrontendService.EditPasswordOption paramEditPasswordOption, String paramString) {
    List<FormField> list1 = this.formService.retrieveFieldsByFormId(paramApplication.formConfiguration.selfServiceFormId);
    List<FormField> list2 = (List)list1.stream().filter(paramFormField -> paramFormField.key.startsWith("user.")).collect(Collectors.toList());
    List<FormField> list3 = (List)list1.stream().filter(paramFormField -> paramFormField.key.startsWith("registration.")).collect(Collectors.toList());
    List<FormField> list4 = list1.stream().filter(paramFormField -> (paramFormField.type == FormDataType.consent)).toList();
    User user1 = retrieveUser(paramTenant, paramUser3.id);
    User user2 = merge(this.expressionEvaluator, paramUser1, user1, list2);
    UserRegistration userRegistration1 = retrieveRegistration(paramTenant, paramApplication.id, paramUser3.id);
    UserRegistration userRegistration2 = merge(this.expressionEvaluator, paramUserRegistration1, userRegistration1, list3);
    handleEditPasswordOptions(paramEditPasswordOption, true, list2, user2);
    CustomFormFrontendService.UserValidationResult userValidationResult = new CustomFormFrontendService.UserValidationResult();
    CustomFormFrontendService.ValidationResult<User> validationResult = normalizeAndValidateFormFields(user2, paramUser2, list2);
    CustomFormFrontendService.ValidationResult<UserRegistration> validationResult1 = normalizeAndValidateFormFields(userRegistration2, paramUserRegistration2, list3);
    CustomFormFrontendService.ValidationResult<Map<UUID, List<String>>> validationResult2 = validateConsents(list4, paramMap);
    userValidationResult.user = (User)validationResult.result;
    userValidationResult.registration = (UserRegistration)validationResult1.result;
    userValidationResult.userConsents = (Map<UUID, List<String>>)validationResult2.result;
    userValidationResult.errors
      .add(validationResult.errors)
      .add(validationResult1.errors)
      .add(validationResult2.errors);
    if (paramEditPasswordOption.equals(CustomFormFrontendService.EditPasswordOption.update) && user1.encryptionScheme != null && paramApplication.formConfiguration.selfServiceFormConfiguration.requireCurrentPasswordOnPasswordChange && paramString == null)
      userValidationResult.errors.addFieldError("currentPassword", "[blank]currentPassword", null, new Object[0]); 
    return userValidationResult;
  }
  
  public Map<Integer, List<FormField>> retrieveFieldsByFormId(UUID paramUUID) {
    List<FormStep> list = (this.formService.retrieveById(paramUUID)).steps;
    return IntStream.range(0, list.size())
      .boxed()
      .collect(Collectors.toMap(paramInteger -> paramInteger, paramInteger -> ((FormStep)paramList.get(paramInteger.intValue())).fieldObjects));
  }
  
  public Map<Integer, List<FormField>> retrieveFieldsByFormIdFilterConsents(UUID paramUUID, User paramUser, Tenant paramTenant) {
    Map map = (Map)this.consentService.retrieveAllConsents().stream().collect(Collectors.toMap(paramConsent -> paramConsent.id, paramConsent -> paramConsent));
    return getFilteredFormFields(paramUUID, paramFormField -> 
        
        (paramFormField.type != FormDataType.consent || ((Consent)paramMap.get(paramFormField.consentId)).canSelfConsent(paramUser)));
  }
  
  public Map<UUID, List<String>> retrieveUserConsents(User paramUser) {
    return (Map<UUID, List<String>>)this.consentService.retrieveUserConsentByUserId(paramUser.tenantId, paramUser.id)
      .stream()
      
      .filter(paramUserConsent -> (paramUserConsent.consent.canSelfConsent(paramUser) && paramUserConsent.status == ConsentStatus.Active))
      .collect(Collectors.toMap(paramUserConsent -> paramUserConsent.consentId, paramUserConsent -> paramUserConsent.values));
  }
  
  public CustomFormFrontendService.UpdateResult updateSelfServiceUser(LambdaDelegate paramLambdaDelegate, Tenant paramTenant, Map<Integer, List<FormField>> paramMap, User paramUser, UserRegistration paramUserRegistration, Map<UUID, List<String>> paramMap1, String paramString, EventInfo paramEventInfo) {
    CustomFormFrontendService.UpdateResult updateResult = new CustomFormFrontendService.UpdateResult();
    List list = paramMap.values().stream().flatMap(Collection::stream).toList();
    List<FormField> list1 = (List)list.stream().filter(paramFormField -> paramFormField.key.startsWith("user.")).collect(Collectors.toList());
    List<FormField> list2 = (List)list.stream().filter(paramFormField -> paramFormField.key.startsWith("registration.")).collect(Collectors.toList());
    List<FormField> list3 = list.stream().filter(paramFormField -> (paramFormField.type == FormDataType.consent)).toList();
    User user1 = ((UserResponse)paramLambdaDelegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUser(paramUser.id))).user;
    User user2 = merge(this.expressionEvaluator, paramUser, new User(user1), list1);
    UserRegistration userRegistration1 = ((RegistrationResponse)paramLambdaDelegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveRegistration(paramUser.id, paramUserRegistration.applicationId))).registration;
    UserRegistration userRegistration2 = merge(this.expressionEvaluator, paramUserRegistration, new UserRegistration(userRegistration1), list2);
    if (!list1.isEmpty()) {
      updateResult.oldUser = user1;
      user2.encryptionScheme = null;
      user2.factor = null;
      UserResponse userResponse = paramLambdaDelegate.execute(paramFusionAuthClient -> paramFusionAuthClient.updateUser(paramUser.id, (new UserRequest()).with(()).with(()).with(()).with(()).with(())));
      updateResult.user = userResponse.user;
      updateResult.emailVerificationId = userResponse.emailVerificationId;
      if (userResponse.verificationIds != null)
        updateResult


          
          .phoneVerificationId = userResponse.verificationIds.stream().filter(paramVerificationId -> paramVerificationId.type.is(IdentityType.phoneNumber)).findFirst().map(paramVerificationId -> paramVerificationId.id).orElse(null); 
    } 
    if (!list2.isEmpty()) {
      updateResult.oldRegistration = userRegistration1;
      updateResult.registration = ((RegistrationResponse)paramLambdaDelegate.execute(paramFusionAuthClient -> paramFusionAuthClient.updateRegistration(paramUser.id, new RegistrationRequest(paramEventInfo, paramUser, paramUserRegistration)))).registration;
    } 
    if (!list3.isEmpty())
      handleConsents(paramLambdaDelegate, user1.id, list3, paramMap1); 
    return updateResult;
  }
  
  void handleEditPasswordOptions(CustomFormFrontendService.EditPasswordOption paramEditPasswordOption, boolean paramBoolean, List<FormField> paramList, User paramUser) {
    boolean bool = paramList.stream().anyMatch(paramFormField -> paramFormField.key.equals("user.password"));
    if (bool) {
      if (paramEditPasswordOption == CustomFormFrontendService.EditPasswordOption.useExisting) {
        paramUser.password = null;
        paramList.removeIf(paramFormField -> paramFormField.key.equals("user.password"));
      } else if (paramEditPasswordOption == CustomFormFrontendService.EditPasswordOption.requireChange) {
        paramUser.password = null;
        paramUser.passwordChangeRequired = true;
        paramUser.passwordChangeReason = ChangePasswordReason.Administrative;
        paramList.removeIf(paramFormField -> paramFormField.key.equals("user.password"));
      } else if (!paramBoolean) {
        paramUser.password = null;
        paramList.removeIf(paramFormField -> paramFormField.key.equals("user.password"));
      } 
    } else {
      paramUser.password = null;
    } 
  }
  
  <T> CustomFormFrontendService.ValidationResult<T> normalizeAndValidateFormFields(T paramT1, T paramT2, List<FormField> paramList) {
    String str = (paramT1 instanceof User) ? "user." : "registration.";
    Validator validator = new Validator();
    for (FormField formField : paramList) {
      Object object1 = getValue(paramT1, formField, str);
      Object object2 = object1;
      Object object3 = formField.confirm ? getValue(paramT2, formField, str) : null;
      if (object1 != null && object1.toString().equals(""))
        object1 = null; 
      Errors errors = validateField(formField, object1, object3);
      if (errors.empty()) {
        validator.ensure(normalizeField(paramT1, formField, object1, object2), formField.key, "[invalid]", new Object[] { "The value could not be converted to [" + String.valueOf(formField.type) + "]." });
        continue;
      } 
      validator.withErrors(errors);
    } 
    CustomFormFrontendService.ValidationResult<T> validationResult = new CustomFormFrontendService.ValidationResult();
    validationResult.errors = validator.done();
    validationResult.result = paramT1;
    return validationResult;
  }
  
  User retrieveUser(Tenant paramTenant, UUID paramUUID) {
    if (paramUUID == null)
      return new User(); 
    ClientResponse<UserResponse, Errors> clientResponse = this.client.setTenantId(paramTenant.id).retrieveUser(paramUUID);
    return clientResponse.wasSuccessful() ? ((UserResponse)clientResponse.successResponse).user : new User();
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
  
  private Map<Integer, List<FormField>> getFilteredFormFields(UUID paramUUID, Predicate<FormField> paramPredicate) {
    return (Map<Integer, List<FormField>>)retrieveFieldsByFormId(paramUUID)
      .entrySet()
      .stream()
      .map(paramEntry -> new Pair(paramEntry.getKey(), ((List)paramEntry.getValue()).stream().filter(paramPredicate).collect(Collectors.toList())))





      
      .filter(paramPair -> !((List)paramPair.second).isEmpty())
      .collect(Collectors.toMap(paramPair -> (Integer)paramPair.first, paramPair -> (List)paramPair.second));
  }
  
  private Object getValue(Object paramObject, FormField paramFormField, String paramString) {
    String str = paramFormField.key.substring(paramString.length());
    return (paramObject == null) ? null : this.expressionEvaluator.getValue(str, paramObject);
  }
  
  private void handleConsents(LambdaDelegate paramLambdaDelegate, UUID paramUUID, List<FormField> paramList, Map<UUID, List<String>> paramMap) {
    Map map = (Map)((UserConsentResponse)paramLambdaDelegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserConsents(paramUUID))).userConsents.stream().collect(Collectors.toMap(paramUserConsent -> paramUserConsent.consentId, paramUserConsent -> paramUserConsent));
    for (FormField formField : paramList) {
      if (map.containsKey(formField.consentId) && 
        !paramMap.containsKey(formField.consentId) && (((UserConsent)map
        
        .get(formField.consentId)).status != ConsentStatus.Revoked || 
        !((UserConsent)map.get(formField.consentId)).values.isEmpty())) {
        UserConsent userConsent = (UserConsent)map.get(formField.consentId);
        paramLambdaDelegate.execute(paramFusionAuthClient -> paramFusionAuthClient.updateUserConsent(paramUserConsent.id, new UserConsentRequest((new UserConsent()).with(()).with(()).with(()).with(()).with(()))));
        continue;
      } 
      if (!map.containsKey(formField.consentId) && paramMap.containsKey(formField.consentId)) {
        paramLambdaDelegate.execute(paramFusionAuthClient -> paramFusionAuthClient.createUserConsent(null, new UserConsentRequest((new UserConsent()).with(()).with(()).with(()).with(()).with(()))));
        continue;
      } 
      if (map.containsKey(formField.consentId) && paramMap.containsKey(formField.consentId)) {
        UserConsent userConsent = (UserConsent)map.get(formField.consentId);
        paramLambdaDelegate.execute(paramFusionAuthClient -> paramFusionAuthClient.updateUserConsent(paramUserConsent.id, new UserConsentRequest((new UserConsent()).with(()).with(()).with(()).with(()).with(()).with(()))));
        continue;
      } 
      if (!map.containsKey(formField.consentId) && !paramMap.containsKey(formField.consentId))
        paramLambdaDelegate.execute(paramFusionAuthClient -> paramFusionAuthClient.createUserConsent(null, new UserConsentRequest((new UserConsent()).with(()).with(()).with(()).with(())))); 
    } 
  }
  
  private CustomFormFrontendService.ValidationResult<UserRegistration> normalizeAndValidate(Tenant paramTenant, UserRegistration paramUserRegistration1, UserRegistration paramUserRegistration2, User paramUser, UUID paramUUID, boolean paramBoolean) {
    CustomFormFrontendService.ValidationResult<UserRegistration> validationResult = new CustomFormFrontendService.ValidationResult();
    validationResult.result = (T)paramUserRegistration1;
    boolean bool = checkIfUserCanEditRoles(paramUserRegistration1.applicationId, paramUser, paramUUID);
    validationResult








      
      .errors = (new Validator()).notMissing(paramUserRegistration1.applicationId, "registration.applicationId", new Object[] { paramUserRegistration1.applicationId }).ifLastCheckHadNoError(paramValidator -> paramValidator.ifTrue(paramBoolean1, ()).ifFalse(paramBoolean1, ())).done();
    if (!validationResult.errors.empty())
      return validationResult; 
    Application application = this.applicationReader.retrieveById(null, paramUserRegistration1.applicationId);
    validationResult
      
      .errors = (new Validator()).validObject(application, "registration.applicationId", new Object[0]).done();
    if (!validationResult.errors.empty())
      return validationResult; 
    ArrayList<FormField> arrayList = new ArrayList<>(this.formService.retrieveFieldsByFormId(application.formConfiguration.adminRegistrationFormId));
    if (!bool)
      arrayList.removeIf(paramFormField -> paramFormField.key.equals("registration.roles")); 
    UserRegistration userRegistration1 = retrieveRegistration(paramTenant, paramUserRegistration1.applicationId, paramUUID);
    UserRegistration userRegistration2 = merge(this.expressionEvaluator, paramUserRegistration1, userRegistration1, arrayList);
    return normalizeAndValidateFormFields(userRegistration2, paramUserRegistration2, arrayList);
  }
  
  private <T> boolean normalizeField(T paramT, FormField paramFormField, Object paramObject1, Object paramObject2) {
    if (paramObject2 == null)
      return true; 
    String str = paramFormField.key.startsWith("user.") ? paramFormField.key.substring("user.".length()) : paramFormField.key.substring("registration.".length());
    if (FormTools.isNullOrEmptyString(paramObject1)) {
      this.expressionEvaluator.setValue(str, paramT, null);
      return true;
    } 
    return handleConversion(this.expressionEvaluator, str, paramFormField, paramT, paramObject1);
  }
  
  private UserRegistration retrieveRegistration(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2) {
    ClientResponse<RegistrationResponse, Errors> clientResponse = this.client.setTenantId(paramTenant.id).retrieveRegistration(paramUUID2, paramUUID1);
    return clientResponse.wasSuccessful() ? ((RegistrationResponse)clientResponse.successResponse).registration : (new UserRegistration()).with(paramUserRegistration -> paramUserRegistration.applicationId = paramUUID);
  }
  
  private CustomFormFrontendService.ValidationResult<Map<UUID, List<String>>> validateConsents(List<FormField> paramList, Map<UUID, List<String>> paramMap) {
    Validator validator = new Validator();
    for (FormField formField : paramList) {
      List list = paramMap.get(formField.consentId);
      validator.withErrors(validateField(formField, list, (Object)null));
    } 
    CustomFormFrontendService.ValidationResult<Map<UUID, List<String>>> validationResult = new CustomFormFrontendService.ValidationResult();
    validationResult.result = (T)paramMap;
    validationResult.errors = validator.done();
    return validationResult;
  }
  
  private Errors validateField(FormField paramFormField, Object paramObject1, Object paramObject2) {
    boolean bool = (paramFormField.type == FormDataType.email || paramFormField.type == FormDataType.string) ? true : false;
    return (new Validator())


      
      .ifTrue(paramFormField.required, paramValidator -> paramValidator.ifTrue(paramBoolean, ()).ifFalse(paramBoolean, ()))


      
      .ifLastCheckHadNoError(paramValidator -> paramValidator.ifTrue((paramFormField.options != null && !paramFormField.options.isEmpty() && paramObject != null), ()))



      
      .ifLastCheckHadNoError(paramValidator -> paramValidator.ifTrue(paramFormField.confirm, ()))

      
      .ifNoErrors(paramValidator -> paramValidator.ifTrue((paramFormField.validator.enabled && paramObject != null), ()))
      
      .done();
  }
}
