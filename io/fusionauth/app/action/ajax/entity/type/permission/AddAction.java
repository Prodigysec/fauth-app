package io.fusionauth.app.action.ajax.entity.type.permission;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.entity.type.BaseEntityTypeAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.EntityTypePermission;
import io.fusionauth.domain.api.EntityTypeRequest;
import io.fusionauth.domain.api.EntityTypeResponse;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "entity_manager"})
public class AddAction extends BaseEntityTypeAJAXAction {
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    EntityTypePermission entityTypePermission = ((EntityTypeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.createEntityTypePermission(this.entityTypeId, null, new EntityTypeRequest(this.permission)))).permission;
    writeAuditLog("Added permission with Id [" + String.valueOf(entityTypePermission.id) + "] and name [" + this.permission.name + "] to entity type with Id [" + String.valueOf(this.entityTypeId) + "] and name [" + this.entityType.name + "]");
    return "success";
  }
}
