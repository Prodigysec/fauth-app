package io.fusionauth.app.action.admin.tenantManager;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.tenantManager.TenantManagerService;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.api.ApplicationSearchRequest;
import io.fusionauth.domain.api.ApplicationSearchResponse;
import io.fusionauth.domain.api.FormResponse;
import io.fusionauth.domain.api.tenantManager.TenantManagerConfigurationRequest;
import io.fusionauth.domain.api.tenantManager.TenantManagerConfigurationResponse;
import io.fusionauth.domain.form.Form;
import io.fusionauth.domain.form.FormType;
import io.fusionauth.domain.tenantManager.TenantManagerApplicationConfiguration;
import io.fusionauth.domain.tenantManager.TenantManagerConfiguration;
import io.fusionauth.domain.tenantManager.TenantManagerIdentityProviderTypeConfiguration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "system_manager"})
@Redirect(uri = "/admin/tenant-manager/edit")
public class EditAction extends BaseAction {
  @FTLVariable
  public final List<Form> registrationForms = new ArrayList<>();
  
  @FTLVariable
  public final List<TenantManagerIdentityProviderTypeConfiguration> typeConfigurations = new ArrayList<>();
  
  @FTLVariable
  public final List<Application> universalApplications = new ArrayList<>();
  
  public List<UUID> applicationIds = new ArrayList<>();
  
  @FTLVariable
  public Boolean disableAddAllowedIdentityProviderType = Boolean.valueOf(false);
  
  public TenantManagerConfiguration tenantManagerConfiguration = new TenantManagerConfiguration();
  
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    TenantManagerConfiguration tenantManagerConfiguration1 = ((TenantManagerConfigurationResponse)superDelegate().execute(FusionAuthClient::retrieveTenantManagerConfiguration)).tenantManagerConfiguration;
    this.tenantManagerConfiguration.applicationConfigurations.clear();
    Objects.requireNonNull(this.tenantManagerConfiguration.applicationConfigurations);
    this.applicationIds.stream().filter(Objects::nonNull).distinct().map(TenantManagerApplicationConfiguration::new).forEach(this.tenantManagerConfiguration.applicationConfigurations::add);
    TenantManagerConfiguration tenantManagerConfiguration2 = ((TenantManagerConfigurationResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateTenantManagerConfiguration(new TenantManagerConfigurationRequest(this.tenantManagerConfiguration)))).tenantManagerConfiguration;
    this.frontEndSupport.addGeneralInfo("success", new Object[0]);
    writeAuditLogForUpdate("Updated the tenant manager configuration", tenantManagerConfiguration1, tenantManagerConfiguration2);
    return "success";
  }
  
  @FormPrepareMethod
  public void prepare() {
    this.tenantManagerConfiguration = ((TenantManagerConfigurationResponse)superDelegate().execute(FusionAuthClient::retrieveTenantManagerConfiguration)).tenantManagerConfiguration;
    this



      
      .applicationIds = (this.tenantManagerConfiguration.applicationConfigurations != null) ? this.tenantManagerConfiguration.applicationConfigurations.stream().map(paramTenantManagerApplicationConfiguration -> paramTenantManagerApplicationConfiguration.applicationId).filter(Objects::nonNull).toList() : List.of();
    this.typeConfigurations.addAll(this.tenantManagerConfiguration.identityProviderTypeConfigurations
        .values().stream().sorted(Comparator.comparing(paramTenantManagerIdentityProviderTypeConfiguration -> paramTenantManagerIdentityProviderTypeConfiguration.type.name())).toList());
    if (this.tenantManagerConfiguration.identityProviderTypeConfigurations.size() == TenantManagerService.SupportedTenantManagerIdentityProviderTypes.size())
      this.disableAddAllowedIdentityProviderType = Boolean.valueOf(true); 
    List list = (List)Objects.requireNonNullElseGet(((FormResponse)this.delegate.execute(FusionAuthClient::retrieveForms)).forms, Collections::emptyList);
    this.registrationForms.addAll(list.stream().filter(paramForm -> (paramForm.type == FormType.registration)).toList());
    ApplicationSearchRequest applicationSearchRequest = (new ApplicationSearchRequest()).with(paramApplicationSearchRequest -> {
          paramApplicationSearchRequest.expand = List.of();
          paramApplicationSearchRequest.search.numberOfResults = 1000;
          paramApplicationSearchRequest.search.universal = Boolean.valueOf(true);
        });
    this.universalApplications.addAll(((ApplicationSearchResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.searchApplications(paramApplicationSearchRequest))).applications);
  }
}
