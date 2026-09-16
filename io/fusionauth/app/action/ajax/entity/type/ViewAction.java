package io.fusionauth.app.action.ajax.entity.type;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.EntityType;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.api.EntityTypeResponse;
import io.fusionauth.domain.api.KeyResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{entityTypeId}", requiresAuthentication = true, constraints = {"admin", "entity_manager"})
public class ViewAction extends BaseAJAXAction {
  public Key accessTokenKey;
  
  public EntityType entityType;
  
  public UUID entityTypeId;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.entityType = ((EntityTypeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveEntityType(this.entityTypeId))).entityType;
    if (this.entityType.jwtConfiguration != null && this.entityType.jwtConfiguration.accessTokenKeyId != null)
      this.accessTokenKey = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveKey(this.entityType.jwtConfiguration.accessTokenKeyId))).key; 
    return "render";
  }
}
