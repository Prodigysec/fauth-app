package io.fusionauth.app.action.admin.key;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.api.KeyRequest;
import io.fusionauth.domain.api.KeyResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;

@Action(requiresAuthentication = true, value = "{keyId}", constraints = {"admin", "key_manager"})
@Redirect(code = "success", uri = "/admin/key/")
public class EditAction extends BaseAction {
  public Key key = new Key();
  
  public UUID keyId;
  
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.key = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveKey(this.keyId))).key;
    return "input";
  }
  
  public String post() {
    Key key1 = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveKey(this.keyId))).key;
    Key key2 = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateKey(this.keyId, new KeyRequest(this.key)))).key;
    writeAuditLogForUpdate("Updated key with Id [" + String.valueOf(this.keyId) + "]", key1, key2);
    return "success";
  }
}
