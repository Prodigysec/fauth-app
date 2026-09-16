package io.fusionauth.app.action.admin.systemConfiguration;

import com.google.inject.Inject;
import com.inversoft.error.Error;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.CollectionTools;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.CORSConfiguration;
import io.fusionauth.domain.SystemConfiguration;
import io.fusionauth.domain.SystemTrustedProxyConfigurationPolicy;
import io.fusionauth.domain.api.SystemConfigurationRequest;
import io.fusionauth.domain.api.SystemConfigurationResponse;
import io.fusionauth.domain.util.HTTPMethod;
import java.util.Collection;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "system_manager"})
@Redirect(uri = "/admin/system-configuration/edit")
public class EditAction extends BaseAction {
  @FTLVariable
  public static final HTTPMethod[] allowedMethods = HTTPMethod.values();
  
  @FTLVariable
  public static final SystemTrustedProxyConfigurationPolicy[] trustPolicies = SystemTrustedProxyConfigurationPolicy.values();
  
  public CORSConfiguration corsConfiguration = new CORSConfiguration();
  
  public String trustedProxies;
  
  public boolean usageDataManaged;
  
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
    paramFrontEndSupport.fieldMapperFunction = (paramString -> paramString.replace("systemConfiguration.corsConfiguration.allowedHeaders", "corsConfiguration.allowedHeaders").replace("systemConfiguration.corsConfiguration.allowedOrigins", "corsConfiguration.allowedOrigins").replace("systemConfiguration.corsConfiguration.exposedHeaders", "corsConfiguration.exposedHeaders").replaceAll("systemConfiguration\\.trustedProxyConfiguration\\.trusted\\[\\d+]", "trustedProxies"));
    paramFrontEndSupport.errorMapperFunction = EditAction::mapError;
  }
  
  public static Error mapError(Error paramError) {
    paramError
      
      .code = paramError.code.replace("systemConfiguration.corsConfiguration.allowedHeaders", "corsConfiguration.allowedHeaders").replace("systemConfiguration.corsConfiguration.allowedOrigins", "corsConfiguration.allowedOrigins").replace("systemConfiguration.corsConfiguration.exposedHeaders", "corsConfiguration.exposedHeaders");
    return paramError;
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    this.systemConfiguration.corsConfiguration.allowedHeaders = this.corsConfiguration.allowedHeaders;
    this.systemConfiguration.corsConfiguration.allowedMethods = this.corsConfiguration.allowedMethods;
    this.systemConfiguration.corsConfiguration.allowedOrigins = this.corsConfiguration.allowedOrigins;
    this.systemConfiguration.corsConfiguration.exposedHeaders = this.corsConfiguration.exposedHeaders;
    SystemConfiguration systemConfiguration1 = ((SystemConfigurationResponse)superDelegate().execute(FusionAuthClient::retrieveSystemConfiguration)).systemConfiguration;
    SystemConfiguration systemConfiguration2 = ((SystemConfigurationResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateSystemConfiguration(new SystemConfigurationRequest(this.systemConfiguration)))).systemConfiguration;
    this.frontEndSupport.addGeneralInfo("success", new Object[0]);
    writeAuditLogForUpdate("Updated the system configuration", systemConfiguration1, systemConfiguration2);
    return "success";
  }
  
  @FormPrepareMethod
  public void prepareForm() {
    this.corsConfiguration = this.systemConfiguration.corsConfiguration;
    if (this.systemConfiguration.uiConfiguration == null)
      this.systemConfiguration.uiConfiguration = new SystemConfiguration.UIConfiguration(); 
    this.trustedProxies = CollectionTools.collectionToString(this.systemConfiguration.trustedProxyConfiguration.trusted);
    this.usageDataManaged = this.frontEndSupport.configuration.usageDataManaged();
  }
  
  @ValidationMethod
  public void validate() {
    Collection<? extends String> collection = CollectionTools.stringToCollection(this.trustedProxies);
    if (collection == null) {
      this.frontEndSupport.addFieldError("trustedProxies", "[invalid]trustedProxies", new Object[0]);
    } else {
      this.systemConfiguration.trustedProxyConfiguration.trusted.clear();
      this.systemConfiguration.trustedProxyConfiguration.trusted.addAll(collection);
    } 
  }
}
