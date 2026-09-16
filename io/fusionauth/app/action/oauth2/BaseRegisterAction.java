package io.fusionauth.app.action.oauth2;

import com.inversoft.error.Errors;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.app.service.user.FormStepViewModel;
import io.fusionauth.app.service.user.RegistrationFrontendService;
import io.fusionauth.app.service.user.RegistrationState;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.user.RegistrationRequest;
import io.fusionauth.domain.form.Form;
import io.fusionauth.domain.form.FormField;
import io.fusionauth.domain.form.FormStep;
import io.fusionauth.http.server.HTTPRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.parameter.annotation.PreParameter;

@ThemedForward(code = "webhook-transaction-failed", cacheControl = "no-store", status = 200)
public abstract class BaseRegisterAction extends BaseOAuthAction {
  protected final RegistrationFrontendService registrationFrontendService;
  
  protected final UserReaderService userReader;
  
  protected final UserService userService;
  
  @FTLVariable
  public RegistrationRequest confirm = new RegistrationRequest();
  
  protected RegistrationState registrationState;
  
  protected BaseRegisterAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, OAuthService paramOAuthService, RegistrationFrontendService paramRegistrationFrontendService, LoginIntentService paramLoginIntentService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    this.registrationFrontendService = paramRegistrationFrontendService;
    this.userReader = paramUserReaderService;
    this.userService = paramUserService;
  }
  
  public Map<UUID, List<String>> getConsents() {
    return this.registrationState.consents;
  }
  
  @FTLVariable
  public List<FormField> getFields() {
    FormStepViewModel formStepViewModel = getCurrentStep();
    return Optional.<FormStepViewModel>ofNullable(formStepViewModel)
      .map(paramFormStepViewModel2 -> paramFormStepViewModel2.fieldObjects.stream().filter(()).toList())


      
      .orElse(List.of());
  }
  
  public UserRegistration getRegistration() {
    return this.registrationState.registration;
  }
  
  public void setRegistration(UserRegistration paramUserRegistration) {
    this.registrationState.registration = paramUserRegistration;
  }
  
  @FTLVariable
  public String getRegistrationState() {
    return this.registrationFrontendService.encryptState(this.registrationState);
  }
  
  @PreParameter
  public void setRegistrationState(String paramString) {
    try {
      this.registrationState = this.registrationFrontendService.decryptState(paramString);
    } catch (ErrorException errorException) {
      if (errorException.getCause() instanceof com.fasterxml.jackson.core.JacksonException) {
        HTTPRequest hTTPRequest = this.frontEndSupport.request;
        String str = hTTPRequest.getParameter("client_id");
        UUID uUID = UUID.fromString(hTTPRequest.getParameter("tenantId"));
        FrontEndThemeResolver.Result result = this.frontEndThemeResolver.resolve(hTTPRequest, str, uUID, this.bypassTheme);
        Integer integer = Integer.valueOf(hTTPRequest.getParameter("step"));
        Boolean bool = Boolean.valueOf(hTTPRequest.getParameter("parentEmailRequired"));
        this.registrationState = this.registrationFrontendService.decryptLegacyRegistrationState(result.tenant, result.application, paramString, integer

            
            .intValue(), bool
            .booleanValue());
      } else {
        throw errorException;
      } 
    } 
  }
  
  @FTLVariable
  public int getStep() {
    return this.registrationState.getStepIndex() + 1;
  }
  
  @FTLVariable
  public int getTotalSteps() {
    return this.registrationState.getTotalSteps();
  }
  
  @FTLVariable
  public User getUser() {
    return this.registrationState.user;
  }
  
  @FTLVariable
  public void setUser(User paramUser) {
    this.registrationState.user = paramUser;
  }
  
  protected void clearUser() {
    User user = getUser();
    user.encryptionScheme = null;
    user.factor = null;
    user.salt = null;
    user.id = null;
  }
  
  protected FormStepViewModel getCurrentStep() {
    return this.registrationState.getCurrentStep();
  }
  
  protected Form getDomainForm() {
    if (this.registrationState.isBasicRegistration())
      return null; 
    List list = (this.registrationState.getForm()).steps.stream().map(paramFormStepViewModel -> (new FormStep()).with(())).toList();
    return (new Form()).with(paramForm -> paramForm.steps = paramList);
  }
  
  protected void normalizeAndValidateWithRegistrationConfiguration(User paramUser, boolean paramBoolean1, String paramString, boolean paramBoolean2) {
    UserService.RegistrationValidationResult registrationValidationResult = this.userService.validateWithRegistrationConfiguration(this.codeTenant, this.codeApplication, 
        getDomainForm(), paramUser, paramString, paramBoolean1, paramBoolean2, this.frontEndSupport


        
        .buildEventInfo(this.metaData));
    this.errorMapping.putAll(registrationValidationResult.fieldMapping);
    IntStream.range(0, registrationValidationResult.errors.size())
      
      .filter(paramInt -> !((Errors)paramRegistrationValidationResult.errors.get(paramInt)).empty())
      .findAny()
      .ifPresent(paramInt -> {
          this.registrationState.setStepIndex(paramInt);
          transferErrors(paramRegistrationValidationResult.errors.get(paramInt));
        });
  }
}
