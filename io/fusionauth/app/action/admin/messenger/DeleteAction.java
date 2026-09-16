package io.fusionauth.app.action.admin.messenger;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.MessengerResponse;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{messengerId}", requiresAuthentication = true, constraints = {"admin", "messenger_deleter"})
@List({@Redirect(code = "success", uri = "/admin/messenger/"), @Redirect(code = "missing", uri = "/admin/messenger/")})
public class DeleteAction extends BaseAction {
  public String confirm;
  
  public BaseMessengerConfiguration messenger;
  
  public UUID messengerId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
    paramFrontEndSupport.customPreTransferErrorConsumer = (paramErrors -> paramFrontEndSupport.moveFieldErrorToGeneral(paramErrors, "messengerId", "[inUseByTenant]messengerId"));
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.deleteMessenger(this.messengerId));
    writeAuditLog("Deleted the messenger with Id [" + String.valueOf(this.messengerId) + "] and name [" + this.messenger.name + "]");
    return "success";
  }
  
  @PostParameterMethod
  public void retrieveMessenger() {
    this.messenger = ((MessengerResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveMessenger(this.messengerId))).messenger;
  }
  
  @ValidationMethod
  public void validate() {
    if (this.confirm == null) {
      this.frontEndSupport.addFieldError("confirm", "[missing]confirm", new Object[0]);
    } else if (!this.confirm.equals("DELETE")) {
      this.frontEndSupport.addFieldError("confirm", "[invalid]confirm", new Object[0]);
    } 
  }
}
