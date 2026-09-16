package io.fusionauth.app.action.ajax.messenger;

import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.messenger.MessengerType;
import java.util.UUID;
import org.primeframework.mvc.action.result.annotation.JSON;

@JSON(code = "input", status = 400)
public abstract class BaseTestAJAXAction extends BaseAJAXAction {
  @FTLVariable
  public UUID messengerId;
  
  @FTLVariable
  public MessengerType type;
  
  protected BaseTestAJAXAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
}
