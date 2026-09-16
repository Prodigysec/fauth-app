package io.fusionauth.app.action.admin.entity.type;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.EntityType;
import io.fusionauth.domain.api.EntityTypeResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, value = "{entityTypeId}", constraints = {"admin", "entity_manager"})
@List({@Redirect(code = "missing", uri = "/admin/entity/type/"), @Redirect(code = "not-licensed", uri = "/admin/entity/type/"), @Redirect(code = "success", uri = "/admin/entity/type/")})
public class DeleteAction extends BaseAction {
  public String confirm;
  
  public EntityType entityType;
  
  public UUID entityTypeId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.deleteEntityType(this.entityTypeId));
    writeAuditLog("Deleted Entity Type with Id [" + String.valueOf(this.entityTypeId) + "]");
    return "success";
  }
  
  @PostParameterMethod
  public void retrieveEntityType() {
    this.entityType = ((EntityTypeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveEntityType(this.entityTypeId))).entityType;
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
