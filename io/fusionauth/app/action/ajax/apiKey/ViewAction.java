package io.fusionauth.app.action.ajax.apiKey;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.APIKey;
import io.fusionauth.domain.api.APIKeyResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{apiKeyId}", requiresAuthentication = true, constraints = {"admin", "api_key_manager"})
public class ViewAction extends BaseAJAXAction {
  public APIKey apiKey;
  
  public UUID apiKeyId;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.apiKey = ((APIKeyResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveAPIKey(this.apiKeyId))).apiKey;
    return "render";
  }
}
