package io.fusionauth.app.action.ajax.key;

import com.google.inject.Inject;
import io.fusionauth.api.service.system.KeyReaderService;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Key;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Status;

@Action(requiresAuthentication = true, constraints = {"admin", "key_manager"})
@Status(code = "error", status = 400)
public class RevealAction extends BaseAJAXAction {
  private final KeyReaderService keyReader;
  
  public UUID keyId;
  
  public String secret;
  
  @Inject
  public RevealAction(FrontEndSupport paramFrontEndSupport, KeyReaderService paramKeyReaderService) {
    super(paramFrontEndSupport);
    this.keyReader = paramKeyReaderService;
  }
  
  public String post() {
    Key key = this.keyReader.retrieveById(this.keyId);
    if (key.type == Key.KeyType.Secret)
      return "error"; 
    this.secret = key.secret;
    return "render";
  }
}
