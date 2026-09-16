package io.fusionauth.app.action.admin.application;

import com.google.inject.Inject;
import com.inversoft.cache.Cache;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.ProxyInfoSupplier;
import io.fusionauth.api.domain.CachedTheme;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.ObjectState;
import io.fusionauth.domain.api.ApplicationResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;

@Action(requiresAuthentication = true, constraints = {"admin", "application_manager"})
@List({@Redirect(code = "api-error", uri = "/admin/application/"), @Redirect(code = "missing", uri = "/admin/application/"), @Redirect(code = "success", uri = "/admin/application/")})
public class ManageRolesAction extends BaseFormAction {
  @Inject
  public ManageRolesAction(FrontEndSupport paramFrontEndSupport, ProxyInfoSupplier paramProxyInfoSupplier, Cache<UUID, CachedTheme> paramCache) {
    super(paramFrontEndSupport, paramProxyInfoSupplier, paramCache);
  }
  
  public String get() {
    this.application = ((ApplicationResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveApplication(this.applicationId))).application;
    if (this.application.state != ObjectState.Active) {
      this.frontEndSupport.addGeneralError("[inactive]", new Object[0]);
      return "success";
    } 
    return "input";
  }
}
