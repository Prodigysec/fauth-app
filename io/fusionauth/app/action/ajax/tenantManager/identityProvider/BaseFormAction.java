package io.fusionauth.app.action.ajax.tenantManager.identityProvider;

import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.tenantManager.TenantManagerService;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.FormFieldResponse;
import io.fusionauth.domain.api.FormResponse;
import io.fusionauth.domain.api.tenantManager.TenantManagerConfigurationResponse;
import io.fusionauth.domain.form.Form;
import io.fusionauth.domain.form.FormField;
import io.fusionauth.domain.form.FormStep;
import io.fusionauth.domain.provider.IdentityProviderLinkingStrategy;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.tenantManager.TenantManagerConfiguration;
import io.fusionauth.domain.tenantManager.TenantManagerIdentityProviderTypeConfiguration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;

public abstract class BaseFormAction extends BaseAJAXAction {
  @FTLVariable
  public final List<IdentityProviderType> availableIdentityProviderTypes = new ArrayList<>();
  
  @FTLVariable
  public final Set<IdentityProviderLinkingStrategy> linkingStrategies = Collections.unmodifiableSet(new LinkedHashSet<>(TenantManagerService.ValidTenantManagerIdentityProviderLinkingStrategies));
  
  @FTLVariable
  public List<FormField> registrationFormFields = new ArrayList<>();
  
  @FTLVariable
  public String registrationFormName;
  
  @FTLVariable
  public TenantManagerIdentityProviderTypeConfiguration typeConfiguration = new TenantManagerIdentityProviderTypeConfiguration();
  
  Map<String, TenantManagerIdentityProviderTypeConfiguration> existingConfigurations;
  
  protected BaseFormAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  @FormPrepareMethod
  public void prepare() {
    TenantManagerConfiguration tenantManagerConfiguration = ((TenantManagerConfigurationResponse)superDelegate().execute(FusionAuthClient::retrieveTenantManagerConfiguration)).tenantManagerConfiguration;
    this.existingConfigurations = tenantManagerConfiguration.identityProviderTypeConfigurations;
    for (IdentityProviderType identityProviderType : TenantManagerService.SupportedTenantManagerIdentityProviderTypes) {
      if (!this.existingConfigurations.containsKey(identityProviderType.name()))
        this.availableIdentityProviderTypes.add(identityProviderType); 
    } 
    if (tenantManagerConfiguration.attributeFormId != null) {
      ClientResponse<FormResponse, Void> clientResponse = this.client.retrieveForm(tenantManagerConfiguration.attributeFormId);
      if (clientResponse.wasSuccessful()) {
        this.registrationFormName = ((FormResponse)clientResponse.successResponse).form.name;
        this.registrationFormFields = getFormFields(((FormResponse)clientResponse.successResponse).form);
      } 
    } 
    prepareForm();
  }
  
  protected List<FormField> getFormFields(Form paramForm) {
    return paramForm.steps
      .stream()
      .flatMap(paramFormStep -> paramFormStep.fields.stream().map(()).filter(()).map(()))



      
      .toList();
  }
  
  protected abstract void prepareForm();
}
