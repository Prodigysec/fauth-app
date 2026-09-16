package io.fusionauth.app.action.ajax.apiKey;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.security.SecurityTools;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.APIKey;
import io.fusionauth.domain.api.APIKeyResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{apiKeyId}", requiresAuthentication = true, constraints = {"admin", "api_key_manager"})
public class DeleteAction extends BaseAJAXAction {
  public UUID apiKeyId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    APIKey aPIKey = ((APIKeyResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveAPIKey(this.apiKeyId))).apiKey;
    if (aPIKey.key != null)
      aPIKey.key = SecurityTools.lastN(aPIKey.key, 7); 
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.deleteAPIKey(this.apiKeyId));
    writeAuditLog("Deleted the API key with Id [" + String.valueOf(this.apiKeyId) + "] " + (aPIKey.retrievable ? ("and key ending in [" + 
        aPIKey.key + "]") : ("and name [" + 
        aPIKey.name + "]")));
    return "success";
  }
}
