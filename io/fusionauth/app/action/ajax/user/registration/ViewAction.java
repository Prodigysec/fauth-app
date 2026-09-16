package io.fusionauth.app.action.ajax.user.registration;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.user.RegistrationResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{userId}/{applicationId}", requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager", "user_support_viewer"})
public class ViewAction extends BaseAJAXAction {
  public Application application;
  
  public UUID applicationId;
  
  public UserRegistration registration;
  
  public UUID userId;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.registration = ((RegistrationResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveRegistration(this.userId, this.applicationId))).registration;
    this.application = ((ApplicationResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveApplication(this.applicationId))).application;
    return "render";
  }
}
