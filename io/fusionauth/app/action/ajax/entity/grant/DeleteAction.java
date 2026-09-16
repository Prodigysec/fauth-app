package io.fusionauth.app.action.ajax.entity.grant;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "entity_manager", "user_manager", "user_support_manager"})
public class DeleteAction extends BaseAJAXAction {
  public UUID entityId;
  
  public UUID recipientEntityId;
  
  public UUID userId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.deleteEntityGrant(this.entityId, this.recipientEntityId, this.userId));
    audit();
    return "success";
  }
  
  private void audit() {
    StringBuilder stringBuilder = (new StringBuilder()).append("Deleted an Entity grant for entity with Id [").append(this.entityId).append("] and to ");
    if (this.userId != null) {
      stringBuilder.append(" user with Id [")
        .append(this.userId)
        .append("]");
    } else {
      stringBuilder.append(" entity with Id [")
        .append(this.recipientEntityId)
        .append("]");
    } 
    writeAuditLog(stringBuilder.toString());
  }
}
