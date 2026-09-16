package io.fusionauth.app.action.admin;

import com.google.inject.Inject;
import com.inversoft.error.Error;
import com.inversoft.util.SecurityTools;
import io.fusionauth.api.service.system.InstanceService;
import io.fusionauth.api.service.system.SetupService;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.APIKey;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EmailConfiguration;
import io.fusionauth.domain.Tenant;
import java.util.HashSet;
import java.util.Set;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin"})
@List({@Redirect(code = "complete", uri = "/admin/"), @Redirect(code = "next", uri = "/admin/first-time-setup?step=${step}")})
public class FirstTimeSetupAction extends BaseAction {
  @FTLVariable
  public static final EmailConfiguration.EmailSecurityType[] emailSecurityTypes = EmailConfiguration.EmailSecurityType.values();
  
  private final Set<String> availableSteps = new HashSet<>(Set.of("license", "application", "apiKey", "email", "summary"));
  
  private final InstanceService instanceService;
  
  private final SetupService setupService;
  
  public APIKey apiKey = new APIKey();
  
  public Application application = new Application();
  
  @FTLVariable
  public SetupService.FirstTimeSetup firstTimeSetup;
  
  public String licenseId;
  
  public String licenseKey;
  
  public String skip;
  
  @FTLVariable
  public String step;
  
  public Tenant tenant = new Tenant();
  
  @Inject
  public FirstTimeSetupAction(FrontEndSupport paramFrontEndSupport, InstanceService paramInstanceService, SetupService paramSetupService) {
    super(paramFrontEndSupport);
    this.instanceService = paramInstanceService;
    this.setupService = paramSetupService;
    paramFrontEndSupport.errorMapperFunction = (paramError -> paramError);
    paramFrontEndSupport.fieldMapperFunction = (paramString -> paramString.replaceAll("application\\.oauthConfiguration\\.authorizedRedirectURLs\\[\\d+]", "application.oauthConfiguration.authorizedRedirectURLs"));
  }
  
  public String get() {
    if (this.firstTimeSetup.complete)
      return "complete"; 
    if (this.skip != null) {
      this.step = stepAfter(this.skip);
      return "next";
    } 
    return "input";
  }
  
  public String post() {
    processStep();
    return "next";
  }
  
  @PostParameterMethod
  public void postParameter() {
    this.firstTimeSetup = this.setupService.retrieveFirstTimeSetupState(this.codeCurrentTenant);
    if (this.frontEndSupport.isGET()) {
      if (!this.availableSteps.contains(this.step))
        this.step = stepAfter(""); 
      if (this.firstTimeSetup.state.getState(this.step) != SetupService.FirstTimeSetupState.State.INCOMPLETE)
        this.step = stepAfter(this.step); 
      if ("apiKey".equals(this.step)) {
        if (this.apiKey.key == null)
          this.apiKey.key = SecurityTools.secureRandom(42); 
      } else if ("email".equals(this.step)) {
        this.tenant = (new Tenant(this.codeCurrentTenant)).secure();
        if (!this.firstTimeSetup.emailConfigured && 
          this.tenant.emailConfiguration.security == null)
          this.tenant.emailConfiguration.security = EmailConfiguration.EmailSecurityType.TLS; 
      } 
      if (this.firstTimeSetup.application != null)
        this.application = this.firstTimeSetup.application; 
      if (this.firstTimeSetup.apiKey != null)
        this.apiKey = this.firstTimeSetup.apiKey; 
    } else if (!this.availableSteps.contains(this.step)) {
      this.step = stepAfter("");
      throw new ErrorException("input");
    } 
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validate() {
    if ("application".equals(this.step)) {
      this.application.normalize();
      this.frontEndSupport.transfer(this.setupService.validateFirstTimeSetupApplication(this.application, this.codeCurrentTenant));
    } else if ("email".equals(this.step)) {
      this.frontEndSupport.transfer(this.setupService.validateFirstTimeSetupEmailConfiguration(this.tenant.emailConfiguration));
    } else if ("license".equals(this.step)) {
      this.frontEndSupport.transfer(this.setupService.validateFirstTimeSetupLicense(this.licenseKey));
    } 
  }
  
  private void processStep() {
    boolean bool = true;
    if ("license".equals(this.step)) {
      if (!this.setupService.firstTimeSetupLicense(this.licenseKey)) {
        this.frontEndSupport.addFieldError("licenseKey", "[invalid]licenseKey", new Object[0]);
        bool = false;
      } 
    } else if ("application".equals(this.step)) {
      this.setupService.firstTimeSetupApplication(this.codeCurrentTenant.id, this.application);
    } else if ("apiKey".equals(this.step)) {
      this.setupService.firstTimeSetupAPIKey(this.apiKey);
    } else if ("email".equals(this.step)) {
      this.setupService.firstTimeSetupEmail(this.tenant.emailConfiguration);
    } else if ("summary".equals(this.step)) {
      this.instanceService.firstTimeSetupComplete();
    } 
    if (bool)
      this.step = stepAfter(this.step); 
  }
  
  private String stepAfter(String paramString) {
    switch (paramString) {
      case "":
        return 
          (this.firstTimeSetup.state.application == SetupService.FirstTimeSetupState.State.INCOMPLETE) ? "application" : stepAfter("application");
      case "application":
        return (this.firstTimeSetup.state.apiKey == SetupService.FirstTimeSetupState.State.INCOMPLETE) ? "apiKey" : stepAfter("apiKey");
      case "apiKey":
        return (this.firstTimeSetup.state.email == SetupService.FirstTimeSetupState.State.INCOMPLETE) ? "email" : stepAfter("email");
      case "email":
        return (this.firstTimeSetup.state.license == SetupService.FirstTimeSetupState.State.INCOMPLETE) ? "license" : stepAfter("license");
    } 
    return "summary";
  }
}
