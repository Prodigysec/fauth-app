package io.fusionauth.app.action.admin.entity.type.permission;

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

@Action(requiresAuthentication = true, constraints = {"admin", "entity_manager"})
@List({@Redirect(code = "api-error", uri = "/admin/entity/type"), @Redirect(code = "success", uri = "/admin/entity/type")})
public class IndexAction extends BaseAction {
  public EntityType entityType = new EntityType();
  
  public UUID entityTypeId;
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.entityType = ((EntityTypeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveEntityType(this.entityTypeId))).entityType;
    return "input";
  }
}
