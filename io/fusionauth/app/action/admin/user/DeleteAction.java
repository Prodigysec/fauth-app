package io.fusionauth.app.action.admin.user;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.UserDeleteSingleRequest;
import io.fusionauth.domain.api.UserResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{userId}", requiresAuthentication = true, constraints = {"admin", "user_deleter"})
@List({@Redirect(code = "missing", uri = "/admin/user/"), @Redirect(code = "success", uri = "/admin/user/")})
public class DeleteAction extends BaseAction {
  public String confirm;
  
  public User user;
  
  public UUID userId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.deleteUserWithRequest(this.userId, new UserDeleteSingleRequest(this.frontEndSupport.buildEventInfo(null), true)));
    writeAuditLog("Deleted user with Id [" + String.valueOf(this.userId) + "]");
    return "success";
  }
  
  @FormPrepareMethod
  public void prepare() {
    this.user = ((UserResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUser(this.userId))).user;
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
