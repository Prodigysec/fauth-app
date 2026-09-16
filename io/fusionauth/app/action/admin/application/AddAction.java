package io.fusionauth.app.action.admin.application;

import com.google.inject.Inject;
import com.inversoft.cache.Cache;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.util.SecurityTools;
import io.fusionauth.api.domain.CachedTheme;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.guice.TenantManagerApplicationId;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.license.FrontEndLicensedFeaturesService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.api.ApplicationRequest;
import io.fusionauth.domain.api.ApplicationResponse;
import java.util.Objects;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "application_manager"})
@List({@Redirect(code = "success", uri = "/admin/application/"), @Redirect(code = "api-error", uri = "/admin/application/")})
public class AddAction extends BaseFormAction {
  @FTLVariable
  public final UUID tenantManagerId;
  
  private final FrontEndLicensedFeaturesService licensedFeaturesService;
  
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport, FrontEndLicensedFeaturesService paramFrontEndLicensedFeaturesService, ProxyInfoSupplier paramProxyInfoSupplier, @TenantManagerApplicationId UUID paramUUID, Cache<UUID, CachedTheme> paramCache) {
    super(paramFrontEndSupport, paramProxyInfoSupplier, paramCache);
    this.licensedFeaturesService = paramFrontEndLicensedFeaturesService;
    this.tenantManagerId = paramUUID;
  }
  
  public String get() {
    if (this.applicationId != null) {
      this.application = ((ApplicationResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveApplication(this.applicationId))).application;
      this.application.id = null;
      this.applicationId = null;
      this.application.oauthConfiguration.clientId = null;
      this.application.oauthConfiguration.clientSecret = null;
      this.application.name += " - copy";
    } 
    this.licensedFeaturesService.disableLicensedFeatures(this.application);
    return "input";
  }
  
  public String post() {
    Application application = ((ApplicationResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.createApplication(this.applicationId, new ApplicationRequest(this.frontEndSupport.buildEventInfo(null), this.application)))).application;
    writeAuditLog("Created the application with Id [" + String.valueOf(application.id) + "] and name [" + this.application.name + "]");
    return "success";
  }
  
  @PostParameterMethod
  public void postParameter() {
    this.application.roles.removeIf(paramApplicationRole -> (paramApplicationRole == null || paramApplicationRole.name == null || paramApplicationRole.name.trim().isEmpty()));
    if (this.application.cleanSpeakConfiguration != null)
      this.application.cleanSpeakConfiguration.applicationIds.removeIf(Objects::isNull); 
  }
  
  @FormPrepareMethod
  public void prepareForm() {
    this.application.oauthConfiguration.clientSecret = SecurityTools.secureRandom();
  }
  
  @ValidationMethod
  public void validate() {
    if (this.tenantId == null && !this.application.universalConfiguration.universal)
      this.frontEndSupport.addFieldError("tenantId", "[blank]tenantId", new Object[0]); 
  }
}
