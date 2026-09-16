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

@Action(requiresAuthentication = true, constraints = {"admin", "entity_manager"})
@List({@Redirect(code = "not-licensed", uri = "/admin/entity/"), @Redirect(code = "success", uri = "/admin/entity/")})
public class AddAction extends BaseFormAction {
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    Entity entity = ((EntityResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.createEntity(this.entityId, new EntityRequest(this.entity)))).entity;
    writeAuditLog("Created Entity with Id [" + String.valueOf(entity.id) + "] and name [" + entity.name + "]");
    this.delegate.execute(FusionAuthClient::refreshEntitySearchIndex);
    return "success";
  }
}
