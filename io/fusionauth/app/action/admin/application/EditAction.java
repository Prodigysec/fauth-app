package io.fusionauth.app.action.admin.application;

import com.google.inject.Inject;
import com.inversoft.cache.Cache;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.ProxyInfoSupplier;
import io.fusionauth.api.domain.CachedTheme;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.guice.TenantManagerApplicationId;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ObjectState;
import io.fusionauth.domain.api.ApplicationRequest;
import io.fusionauth.domain.api.ApplicationResponse;
import java.util.Objects;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "application_manager"})
@List({@Redirect(code = "success", uri = "/admin/application/"), @Redirect(code = "api-error", uri = "/admin/application/"), @Redirect(code = "missing", uri = "/admin/application/")})
public class EditAction extends BaseFormAction {
  @FTLVariable
  public final UUID tenantManagerId;
  
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport, ProxyInfoSupplier paramProxyInfoSupplier, @TenantManagerApplicationId UUID paramUUID, Cache<UUID, CachedTheme> paramCache) {
    super(paramFrontEndSupport, paramProxyInfoSupplier, paramCache);
    this.tenantManagerId = paramUUID;
  }
  
  public String get() {
    this.application = ((ApplicationResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveApplication(this.applicationId))).application;
    if (this.application.state != ObjectState.Active) {
      this.frontEndSupport.addGeneralError("[inactive]", new Object[0]);
      return "success";
    } 
    return "input";
  }
  
  public String post() {
    Application application1 = ((ApplicationResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveApplication(this.applicationId))).application;
    this.application.data.clear();
    this.application.data.putAll(application1.data);
    Application application2 = ((ApplicationResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.updateApplication(this.applicationId, new ApplicationRequest(this.frontEndSupport.buildEventInfo(null), this.application)))).application;
    writeAuditLogForUpdate("Updated the application with Id [" + String.valueOf(this.applicationId) + "] and name [" + application2.name + "]", application1, application2);
    return "success";
  }
  
  @PostParameterMethod
  public void postParameter() {
    if (this.application.cleanSpeakConfiguration != null)
      this.application.cleanSpeakConfiguration.applicationIds.removeIf(Objects::isNull); 
  }
}
