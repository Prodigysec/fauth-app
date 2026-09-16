package io.fusionauth.app.action.admin.entity;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.api.EntityRequest;
import io.fusionauth.domain.api.EntityResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;

@Action(value = "{entityId}", requiresAuthentication = true, constraints = {"admin", "entity_manager"})
@List({@Redirect(code = "missing", uri = "/admin/entity/"), @Redirect(code = "not-licensed", uri = "/admin/entity/"), @Redirect(code = "success", uri = "/admin/entity/")})
public class EditAction extends BaseFormAction {
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.entity = ((EntityResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveEntity(this.entityId))).entity;
    return "input";
  }
  
  public String post() {
    Entity entity1 = ((EntityResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveEntity(this.entityId))).entity;
    this.entity.data.clear();
    this.entity.data.putAll(entity1.data);
    Entity entity2 = ((EntityResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.updateEntity(this.entityId, new EntityRequest(this.entity)))).entity;
    writeAuditLogForUpdate("Updated Entity with Id [" + String.valueOf(this.entityId) + "] and name [" + entity2.name + "]", entity1, entity2);
    return "success";
  }
}
