package io.fusionauth.app.action.ajax.entity.type.permission;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.app.action.ajax.entity.type.BaseEntityTypeAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.EntityTypePermission;
import io.fusionauth.domain.api.EntityTypeRequest;
import io.fusionauth.domain.api.EntityTypeResponse;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "entity_manager"})
public class EditAction extends BaseEntityTypeAJAXAction {
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    if (this.permission.id == null)
      throw new NotFoundException(); 
    return "render";
  }
  
  public String post() {
    EntityTypePermission entityTypePermission1 = this.entityType.getPermission(this.permission.name);
    EntityTypePermission entityTypePermission2 = ((EntityTypeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateEntityTypePermission(this.entityTypeId, this.permissionId, new EntityTypeRequest(this.permission)))).permission;
    writeAuditLogForUpdate("Updated permission with Id [" + String.valueOf(this.permissionId) + "] and name [" + entityTypePermission2.name + "] in entity type with Id [" + String.valueOf(this.entityTypeId) + "] and name [" + this.entityType.name + "]", entityTypePermission1, entityTypePermission2);
    return "success";
  }
}
