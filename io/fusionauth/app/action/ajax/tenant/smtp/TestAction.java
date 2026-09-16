package io.fusionauth.app.action.ajax.tenant.smtp;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.CollectionTools;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.email.SMTPTestService;
import io.fusionauth.app.action.admin.tenant.BaseFormAction;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.EmailHeader;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.api.TenantResponse;
import java.util.HashMap;
import java.util.Map;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.UnknownParameters;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "tenant_manager"})
public class TestAction extends BaseAJAXAction {
  private final SMTPTestService smtpTestService;
  
  public String additionalEmailHeaders;
  
  public BaseFormAction.EditPasswordOption editPasswordOption;
  
  public String email;
  
  @JSONResponse
  public SMTPTestService.SMTPTestResult result;
  
  @FTLVariable
  public Tenant tenant = new Tenant();
  
  @UnknownParameters
  public Map<String, Object> unknown = new HashMap<>();
  
  @Inject
  public TestAction(FrontEndSupport paramFrontEndSupport, SMTPTestService paramSMTPTestService) {
    super(paramFrontEndSupport);
    this.smtpTestService = paramSMTPTestService;
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    this.tenant.id = this.tenantId;
    if (this.editPasswordOption == BaseFormAction.EditPasswordOption.useExisting && this.tenantId != null) {
      Tenant tenant = ((TenantResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTenant(this.tenantId))).tenant;
      this.tenant.emailConfiguration.password = tenant.emailConfiguration.password;
    } 
    this.result = this.smtpTestService.test(this.tenant, this.email);
    this.result.exception = null;
    return "render-json";
  }
  
  @ValidationMethod
  public void validate() {
    Map map = CollectionTools.stringToMap(this.additionalEmailHeaders);
    if (map == null) {
      this.frontEndSupport.addFieldError("additionalEmailHeaders", "[invalid]additionalEmailHeaders", new Object[0]);
    } else {
      map.forEach((paramString1, paramString2) -> this.tenant.emailConfiguration.additionalHeaders.add(new EmailHeader(paramString1, paramString2)));
    } 
    this.frontEndSupport.transfer((new Validator()).notBlank(this.email, "email", new Object[0])
        .done());
  }
}
