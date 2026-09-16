package io.fusionauth.app.action.admin.tenant;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.api.TenantDeleteRequest;
import io.fusionauth.domain.api.TenantResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{tenantId}", requiresAuthentication = true, constraints = {"admin", "tenant_deleter"})
@List({@Redirect(code = "api-error", uri = "/admin/tenant/"), @Redirect(code = "success", uri = "/admin/tenant/"), @Redirect(code = "missing", uri = "/admin/tenant/")})
public class DeleteAction extends BaseAction {
  public String confirm;
  
  public Tenant tenant;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
    paramFrontEndSupport.customPreTransferErrorConsumer = (paramErrors -> paramFrontEndSupport.moveFieldErrorToGeneral(paramErrors, "tenantId", "[pendingDelete]tenantId"));
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.deleteTenantWithRequest(this.tenantId, new TenantDeleteRequest(this.frontEndSupport.buildEventInfo(null), true)));
    writeAuditLog("Deleted the tenant with Id [" + String.valueOf(this.tenantId) + "] and name [" + this.tenant.name + "]");
    return "success";
  }
  
  @PostParameterMethod
  public void retrieveTenant() {
    this.tenant = ((TenantResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTenant(this.tenantId))).tenant;
  }
  
  @ValidationMethod
  public void validate() {
    if (this.confirm == null) {
      this.frontEndSupport.addFieldError("confirm", "[missing]confirm", new Object[0]);
    } else if (!this.confirm.equals("DELETE")) {
      this.frontEndSupport.addFieldError("confirm", "[invalid]confirm", new Object[0]);
    } 
  }
}
