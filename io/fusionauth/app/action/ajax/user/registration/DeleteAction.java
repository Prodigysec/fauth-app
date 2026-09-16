package io.fusionauth.app.action.ajax.user.registration;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.api.user.RegistrationDeleteRequest;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, value = "{userId}/{applicationId}", constraints = {"admin", "user_manager", "user_support_manager"})
public class DeleteAction extends BaseAJAXAction {
  public UUID applicationId;
  
  public UUID userId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.deleteRegistrationWithRequest(this.userId, this.applicationId, new RegistrationDeleteRequest(this.frontEndSupport.buildEventInfo(null))));
    writeAuditLog("Deleted user registration for user with Id [" + String.valueOf(this.userId) + "] and the application with Id [" + String.valueOf(this.applicationId) + "]");
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    if (Application.FUSIONAUTH_APP_ID.equals(this.applicationId) && 
      doesNotHaveRole(new String[] { "admin", "user_manager" }))
      this.frontEndSupport.addGeneralError("[unauthorized]applicationId", new Object[0]); 
  }
}
