package io.fusionauth.app.action.admin.entity;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.api.EntityResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;

@Action(value = "{entityId}", requiresAuthentication = true, constraints = {"admin", "entity_manager"})
@Redirect(code = "missing", uri = "/admin/entity/")
public class ManageAction extends BaseAction {
  public Entity entity;
  
  public UUID entityId;
  
  @Inject
  public ManageAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.entity = ((EntityResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveEntity(this.entityId))).entity;
    return "input";
  }
}
