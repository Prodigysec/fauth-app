package io.fusionauth.app.action.ajax.entity.type.permission;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.app.action.ajax.entity.type.BaseEntityTypeAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "entity_manager"})
public class DeleteAction extends BaseEntityTypeAJAXAction {
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    if (this.permission.id == null)
      throw new NotFoundException(); 
    return "render";
  }
  
  public String post() {
    superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.deleteEntityTypePermission(this.entityTypeId, this.permissionId));
    writeAuditLog("Deleted permission with Id [" + String.valueOf(this.permissionId) + "] and name [" + this.permission.name + "] from entityType with Id [" + String.valueOf(this.entityTypeId) + "] and name [" + this.entityType.name + "]");
    return "success";
  }
}
