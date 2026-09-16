package io.fusionauth.app.action.ajax.user;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.validator.Validator;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.UserActionLog;
import io.fusionauth.domain.api.UserActionResponse;
import io.fusionauth.domain.api.user.ActionRequest;
import io.fusionauth.domain.api.user.ActionResponse;
import java.util.Objects;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, value = "{actionId}", constraints = {"admin", "user_manager", "user_support_manager"})
public class ModifyActionAction extends BaseUserActionAJAXAction {
  public UserAction userAction;
  
  public UserActionLog userActionLog;
  
  @Inject
  protected ModifyActionAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.action.notifyUser = this.userActionLog.notifyUserOnEnd;
    this.action.emailUser = this.userActionLog.emailUserOnEnd;
    this.expires = Boolean.valueOf(true);
    return "render";
  }
  
  @PostParameterMethod
  public void loadData() {
    this.userActionLog = findAction();
    this.userAction = ((UserActionResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserAction(this.userActionLog.userActionId))).userAction;
  }
  
  public String post() {
    this.action.actionerUserId = this.codeCurrentUser.id;
    this.action.expiry = calculateExpiry();
    UserActionLog userActionLog1 = ((ActionResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveAction(this.actionId))).action;
    UserActionLog userActionLog2 = ((ActionResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.modifyAction(this.actionId, new ActionRequest(this.frontEndSupport.buildEventInfo(null), this.action, true)))).action;
    writeAuditLogForUpdate("Modified the action with Id [" + String.valueOf(this.actionId) + "] and name [" + userActionLog2.name + "]", userActionLog1, userActionLog2);
    return "success";
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validate() {
    if (this.expires != null && this.expires.booleanValue()) {
      Objects.requireNonNull(this.frontEndSupport);
      (new Validator()).notMissing(this.expiryValue, "expiryValue", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.ensure((this.expiryValue.intValue() > 0), "expiryValue", "[negative]", new Object[0])).done(this.frontEndSupport::transfer);
    } 
  }
}
