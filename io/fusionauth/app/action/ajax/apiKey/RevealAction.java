package io.fusionauth.app.action.ajax.apiKey;

import com.google.inject.Inject;
import io.fusionauth.api.service.system.APIKeyReaderService;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.APIKey;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "api_key_manager"})
public class RevealAction extends BaseAJAXAction {
  private final APIKeyReaderService apiKeyReader;
  
  public UUID apiKeyId;
  
  public String key;
  
  @Inject
  public RevealAction(FrontEndSupport paramFrontEndSupport, APIKeyReaderService paramAPIKeyReaderService) {
    super(paramFrontEndSupport);
    this.apiKeyReader = paramAPIKeyReaderService;
  }
  
  public String post() {
    APIKey aPIKey = this.apiKeyReader.retrieveById(null, this.apiKeyId);
    this.key = aPIKey.key;
    return "render";
  }
}
