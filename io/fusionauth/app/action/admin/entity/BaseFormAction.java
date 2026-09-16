package io.fusionauth.app.action.admin.entity;

import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.EntityType;
import io.fusionauth.domain.api.EntityTypeResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;

public abstract class BaseFormAction extends BaseAction {
  public Entity entity = new Entity();
  
  public UUID entityId;
  
  public List<EntityType> entityTypes = new ArrayList<>();
  
  protected BaseFormAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  @FormPrepareMethod
  public void prepare() {
    List<EntityType> list = ((EntityTypeResponse)superDelegate().execute(FusionAuthClient::retrieveEntityTypes)).entityTypes;
    if (list != null)
      this.entityTypes.addAll(list); 
  }
}
