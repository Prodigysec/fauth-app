package io.fusionauth.app.action.admin.entity;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.EntityResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{entityId}", requiresAuthentication = true, constraints = {"admin", "entity_manager"})
@List({@Redirect(code = "missing", uri = "/admin/entity/"), @Redirect(code = "not-licensed", uri = "/admin/entity/"), @Redirect(code = "success", uri = "/admin/entity/")})
public class DeleteAction extends BaseFormAction {
  @FTLVariable
  public String confirm;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.deleteEntity(this.entityId));
    writeAuditLog("Deleted Entity with Id [" + String.valueOf(this.entityId) + "]");
    return "success";
  }
  
  @PostParameterMethod
  public void retrieveEntity() {
    this.entity = ((EntityResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveEntity(this.entityId))).entity;
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
