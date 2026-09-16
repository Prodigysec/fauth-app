package io.fusionauth.app.action.ajax.reactor;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.ReactorResponse;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "reactor_manager"})
public class RegenerateKeyAction extends BaseAJAXAction {
  @FTLVariable
  public ReactorStatus status;
  
  @Inject
  public RegenerateKeyAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  @PostParameterMethod
  public void fetchStatus() {
    ClientResponse<ReactorResponse, Void> clientResponse = this.superClient.retrieveReactorStatus();
    this.status = clientResponse.wasSuccessful() ? ((ReactorResponse)clientResponse.successResponse).status : new ReactorStatus();
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    if (this.status.breachedPasswordDetection == ReactorFeatureStatus.ACTIVE || this.status.breachedPasswordDetection == ReactorFeatureStatus.DISCONNECTED) {
      ClientResponse<Void, Void> clientResponse = this.superClient.regenerateReactorKeys();
      if (clientResponse.wasSuccessful()) {
        this.frontEndSupport.addGeneralInfo(MessageScope.FLASH, "[RegenerateSuccess]", new Object[0]);
      } else {
        this.frontEndSupport.addGeneralError(MessageScope.FLASH, "[RegenerateError]", new Object[0]);
      } 
    } 
    return "success";
  }
}
