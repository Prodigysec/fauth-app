package io.fusionauth.app.action.ajax.user.twoFactor;

import com.google.inject.Inject;
import com.inversoft.error.Error;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.twoFactor.TwoFactorSendRequest;
import io.fusionauth.domain.message.MessageType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.parameter.annotation.UnknownParameters;

@Action(requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager", "user_support_viewer"})
public class SendAction extends BaseAJAXAction {
  public String email;
  
  public MessageType messageType;
  
  public String method;
  
  public String methodId;
  
  public String mobilePhone;
  
  @UnknownParameters
  public Map<String, Object> unknown = new HashMap<>();
  
  @Inject
  public SendAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String post() {
    ClientResponse<Void, Errors> clientResponse = this.client.sendTwoFactorCodeForEnableDisable((new TwoFactorSendRequest())
        .with(paramTwoFactorSendRequest -> paramTwoFactorSendRequest.email = this.email)
        .with(paramTwoFactorSendRequest -> paramTwoFactorSendRequest.messageType = this.messageType)
        .with(paramTwoFactorSendRequest -> paramTwoFactorSendRequest.method = this.method)
        .with(paramTwoFactorSendRequest -> paramTwoFactorSendRequest.methodId = this.methodId)
        .with(paramTwoFactorSendRequest -> paramTwoFactorSendRequest.mobilePhone = this.mobilePhone)
        .with(paramTwoFactorSendRequest -> paramTwoFactorSendRequest.userId = this.codeCurrentUser.id));
    if (clientResponse.wasSuccessful() || clientResponse.status == 429)
      return "success"; 
    for (Error error : ((Errors)clientResponse.errorResponse).generalErrors) {
      this.frontEndSupport.addGeneralError(error.code, new Object[] { error.message });
    } 
    ((Errors)clientResponse.errorResponse).fieldErrors.forEach((paramString, paramList) -> paramList.forEach(()));
    return "render-input-json";
  }
}
