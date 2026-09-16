package io.fusionauth.app.action.ajax.user;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.validator.Validator;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ObjectState;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.UserActionReason;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.UserActionReasonResponse;
import io.fusionauth.domain.api.UserActionResponse;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.api.user.ActionRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, value = "{userId}", constraints = {"admin", "user_manager", "user_support_manager"})
public class ActionAction extends BaseUserActionAJAXAction {
  public List<Application> applications = new ArrayList<>();
  
  public User user;
  
  public List<UserActionReason> userActionReasons;
  
  public List<UserAction> userActions;
  
  @Inject
  public ActionAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  @PostParameterMethod
  public void loadData() {
    this.user = ((UserResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUser(this.userId))).user;
    for (Iterator<UserRegistration> iterator = this.user.getRegistrations().iterator(); iterator.hasNext(); ) {
      UserRegistration userRegistration = iterator.next();
      Application application = ((ApplicationResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveApplication(paramUserRegistration.applicationId))).application;
      if (application.state == ObjectState.Active)
        this.applications.add(application); 
    } 
    this.userActions = ((UserActionResponse)superDelegate().execute(FusionAuthClient::retrieveUserActions)).userActions;
    if (this.userActions != null)
      this.userActions.sort(UserAction::compareTo); 
    this.userActionReasons = ((UserActionReasonResponse)superDelegate().execute(FusionAuthClient::retrieveUserActionReasons)).userActionReasons;
    if (this.userActionReasons != null)
      this.userActionReasons.sort(UserActionReason::compareTo); 
  }
  
  public String post() {
    this.action.actioneeUserId = this.userId;
    this.action.actionerUserId = this.codeCurrentUser.id;
    this.action.expiry = calculateExpiry();
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.actionUser(new ActionRequest(this.frontEndSupport.buildEventInfo(null), this.action, true)));
    writeAuditLog("Actioned user with Id [" + String.valueOf(this.action.actioneeUserId) + "] with the action with Id [" + String.valueOf(this.action.userActionId) + "]");
    return "success";
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validate() {
    Validator validator = (new Validator()).notMissing(this.action.userActionId, "action.userActionId", new Object[0]);
    if (this.expires != null && this.expires.booleanValue())
      validator.notMissing(this.expiryValue, "expiryValue", new Object[0])
        .ifLastCheckHadNoError(paramValidator -> paramValidator.ensure((this.expiryValue.intValue() > 0), "expiryValue", "[negative]", new Object[0])); 
    Objects.requireNonNull(this.frontEndSupport);
    validator.done(this.frontEndSupport::transfer);
  }
}
