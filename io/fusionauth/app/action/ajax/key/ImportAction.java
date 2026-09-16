package io.fusionauth.app.action.ajax.key;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.api.KeyRequest;
import io.fusionauth.domain.api.KeyResponse;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{type}", requiresAuthentication = true, constraints = {"admin", "key_manager"})
public class ImportAction extends BaseFormAction {
  @Inject
  public ImportAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    if (this.t != null && this.t.equals("public"))
      this.frontEndSupport.errorMapping.put("key.certificate", "key.publicKey"); 
    Key key = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.importKey(this.keyId, new KeyRequest(this.key)))).key;
    writeAuditLog("Imported key with Id [" + String.valueOf(key.id) + "]");
    return "success";
  }
}
