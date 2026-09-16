package io.fusionauth.app.action.ajax.entity.type;

import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.EntityType;
import io.fusionauth.domain.EntityTypePermission;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.api.EntityTypeResponse;
import io.fusionauth.domain.api.KeyResponse;
import java.util.UUID;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

public abstract class BaseEntityTypeAJAXAction extends BaseAJAXAction {
  public Key accessTokenKey;
  
  public EntityType entityType;
  
  public UUID entityTypeId;
  
  public EntityTypePermission permission = new EntityTypePermission();
  
  public UUID permissionId;
  
  protected BaseEntityTypeAJAXAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  @PostParameterMethod
  public void retrieveEntityType() {
    if (this.entityTypeId != null) {
      this.entityType = ((EntityTypeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveEntityType(this.entityTypeId))).entityType;
      if (this.entityType.jwtConfiguration != null && this.entityType.jwtConfiguration.accessTokenKeyId != null)
        this.accessTokenKey = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveKey(this.entityType.jwtConfiguration.accessTokenKeyId))).key; 
      if (this.permissionId != null && this.frontEndSupport.isGET())
        this.permission = this.entityType.permissions.stream().filter(paramEntityTypePermission -> paramEntityTypePermission.id.equals(this.permissionId)).findFirst().orElse(new EntityTypePermission()); 
    } 
  }
}
