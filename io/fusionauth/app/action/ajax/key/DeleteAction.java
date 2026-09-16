package io.fusionauth.app.action.ajax.key;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{keyId}", requiresAuthentication = true, constraints = {"admin", "key_manager"})
public class DeleteAction extends BaseAJAXAction {
  public UUID keyId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.deleteKey(this.keyId));
    writeAuditLog("Deleted the key with Id [" + String.valueOf(this.keyId) + "]");
    return "success";
  }
}
