package io.fusionauth.app.action.ajax.entity;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.api.EntityResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{entityId}", requiresAuthentication = true, constraints = {"admin", "entity_manager"})
public class ViewAction extends BaseAJAXAction {
  public Entity entity;
  
  public UUID entityId;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.entity = ((EntityResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveEntity(this.entityId))).entity;
    return "render";
  }
}
