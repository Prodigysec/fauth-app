package io.fusionauth.app.action.admin.entity.type;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.EntityType;
import io.fusionauth.domain.api.EntityTypeRequest;
import io.fusionauth.domain.api.EntityTypeResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;

@Action(requiresAuthentication = true, value = "{entityTypeId}", constraints = {"admin", "entity_manager"})
@List({@Redirect(code = "missing", uri = "/admin/entity/type/"), @Redirect(code = "not-licensed", uri = "/admin/entity/type/"), @Redirect(code = "success", uri = "/admin/entity/type/")})
public class EditAction extends BaseFormAction {
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.entityType = ((EntityTypeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveEntityType(this.entityTypeId))).entityType;
    return "input";
  }
  
  public String post() {
    EntityType entityType1 = ((EntityTypeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveEntityType(this.entityTypeId))).entityType;
    this.entityType.data.clear();
    this.entityType.data.putAll(entityType1.data);
    EntityType entityType2 = ((EntityTypeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateEntityType(this.entityTypeId, new EntityTypeRequest(this.entityType)))).entityType;
    writeAuditLogForUpdate("Updated Entity Type with Id [" + String.valueOf(this.entityTypeId) + "] and name [" + entityType2.name + "]", entityType1, entityType2);
    return "success";
  }
}
