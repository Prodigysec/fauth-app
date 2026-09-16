package io.fusionauth.app.action.admin;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.cache.InstanceCacheLoader;
import io.fusionauth.api.service.system.SetupService;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.domain.Acquisition;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.User;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.security.oauth.Tokens;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
@List({@Redirect(code = "next", uri = "/admin/setup-wizard?setupToken=${setupToken}"), @Redirect(code = "login", uri = "/admin/login"), @Redirect(code = "success", uri = "/admin/")})
public class SetupWizardAction extends BaseAction {
  public final Acquisition acquisition = new Acquisition();
  
  public final User user = new User();
  
  private final InstanceCacheLoader instanceCacheLoader;
  
  private final SetupService setupService;
  
  public boolean acceptLicense;
  
  @FTLVariable
  public List<String> acquisitionChannels = SetupService.AcquisitionChannels;
  
  public boolean addToNewsletter = true;
  
  public boolean adminUserInputEntered;
  
  public String passwordConfirm;
  
  public String setupToken;
  
  public boolean setupTokenInputRequired;
  
  public String timezone;
  
  @FTLVariable
  public boolean usageDataEnabled;
  
  @FTLVariable
  public boolean usageDataManaged;
  
  private boolean adminUserInputRequired;
  
  private SetupService.SetupValidationResult result;
  
  @Inject
  public SetupWizardAction(FrontEndSupport paramFrontEndSupport, SetupService paramSetupService, InstanceCacheLoader paramInstanceCacheLoader) {
    super(paramFrontEndSupport);
    this.setupService = paramSetupService;
    this.instanceCacheLoader = paramInstanceCacheLoader;
  }
  
  public String get() {
    if ((this.frontEndSupport.getInstance()).setupComplete)
      return "login"; 
    this.usageDataEnabled = true;
    return "input";
  }
  
  public String post() {
    if ((this.frontEndSupport.getInstance()).setupComplete)
      return "login"; 
    if (this.setupTokenInputRequired)
      return "input"; 
    if (this.adminUserInputRequired)
      return "next"; 
    if (this.timezone != null)
      try {
        this.zoneId = ZoneId.of(this.timezone);
      } catch (DateTimeException dateTimeException) {
        this.zoneId = ZoneId.systemDefault();
      }  
    SetupService.SetupResult setupResult = this.setupService.setup(this.result.user(), this.frontEndSupport.getTrustedClientIPAddress(), this.addToNewsletter, this.acquisition, true);
    if (this.addToNewsletter && !setupResult.addedToNewsletter)
      this.frontEndSupport.addGeneralError("[failure]addToNewsletter", new Object[0]); 
    if (!this.usageDataManaged)
      superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.patchSystemConfiguration(Map.of("systemConfiguration", Map.of("usageDataConfiguration", Map.of("enabled", Boolean.valueOf(this.usageDataEnabled)))))); 
    this.frontEndSupport.userLoginSecurityContext.login(new Tokens(null, setupResult.refreshToken));
    return "success";
  }
  
  @PostParameterMethod
  public void postParameter() {
    this.instanceCacheLoader.run();
    this.usageDataManaged = this.frontEndSupport.configuration.usageDataManaged();
    if (this.usageDataManaged)
      this.usageDataEnabled = true; 
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validate() {
    if ((this.frontEndSupport.getInstance()).setupComplete)
      return; 
    Errors errors = this.setupService.validateSetupToken(this.setupToken);
    if (!errors.empty()) {
      this.setupTokenInputRequired = true;
      this.frontEndSupport.transfer(errors);
      return;
    } 
    if (!this.adminUserInputEntered) {
      this.adminUserInputRequired = true;
    } else {
      this.user.normalize();
      this.result = this.setupService.validate(this.user, this.passwordConfirm, this.acquisition);
      this.frontEndSupport.transfer(this.result.errors());
      if (!this.acceptLicense)
        this.frontEndSupport.addFieldError("acceptLicense", "[missing]acceptLicense", new Object[0]); 
    } 
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    if (!(this.frontEndSupport.getInstance()).setupComplete) {
      Errors errors = this.setupService.validateSetupToken(this.setupToken);
      if (!errors.empty())
        this.setupTokenInputRequired = true; 
    } 
  }
}
