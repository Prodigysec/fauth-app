package io.fusionauth.app.action.ajax.user;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.DisplayableRawLogin;
import io.fusionauth.domain.api.user.RecentLoginResponse;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager", "user_support_viewer"})
public class RecentLoginsAction extends BaseUserActionAJAXAction {
  public Integer limit = Integer.valueOf(10);
  
  public List<DisplayableRawLogin> logins;
  
  public int offset;
  
  @Inject
  public RecentLoginsAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.logins = ((RecentLoginResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserRecentLogins(this.userId, this.offset, this.limit))).logins;
    return "render";
  }
}
