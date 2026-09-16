package io.fusionauth.app.action.admin.integration;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.ProxyInfoSupplier;
import io.fusionauth.api.service.integrations.IntegrationService;
import io.fusionauth.api.service.moderation.cleanspeak.Application;
import io.fusionauth.api.service.moderation.cleanspeak.ApplicationResponse;
import io.fusionauth.api.service.moderation.cleanspeak.CleanSpeakClient;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.CleanSpeakConfiguration;
import io.fusionauth.domain.Integrations;
import io.fusionauth.domain.api.IntegrationRequest;
import io.fusionauth.domain.api.IntegrationResponse;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "system_manager"})
@Redirect(uri = "/admin/integration/")
public class CleanspeakAction extends BaseAction {
  private final IntegrationService integrationService;
  
  private final ProxyInfoSupplier proxyInfoSupplier;
  
  public List<Application> cleanSpeakApplications;
  
  public Integrations integrations;
  
  @Inject
  public CleanspeakAction(FrontEndSupport paramFrontEndSupport, IntegrationService paramIntegrationService, ProxyInfoSupplier paramProxyInfoSupplier) {
    super(paramFrontEndSupport);
    this.integrationService = paramIntegrationService;
    this.proxyInfoSupplier = paramProxyInfoSupplier;
  }
  
  public String get() {
    this.integrations = this.integrationService.retrieve();
    return "input";
  }
  
  public String post() {
    Integrations integrations = ((IntegrationResponse)superDelegate().execute(FusionAuthClient::retrieveIntegration)).integrations;
    CleanSpeakConfiguration cleanSpeakConfiguration1 = integrations.cleanspeak;
    integrations.cleanspeak = this.integrations.cleanspeak;
    CleanSpeakConfiguration cleanSpeakConfiguration2 = ((IntegrationResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateIntegrations(new IntegrationRequest(paramIntegrations)))).integrations.cleanspeak;
    this.frontEndSupport.addGeneralInfo("success", new Object[0]);
    writeAuditLogForUpdate("Updated the CleanSpeak integration", cleanSpeakConfiguration1, cleanSpeakConfiguration2);
    return "success";
  }
  
  @FormPrepareMethod
  public void prepareForm() {
    if (this.integrations.cleanspeak.apiKey != null && this.integrations.cleanspeak.url != null) {
      CleanSpeakClient cleanSpeakClient = new CleanSpeakClient(this.integrations.cleanspeak.apiKey, this.integrations.cleanspeak.url.toString(), paramClientResponse -> paramClientResponse.successResponse, paramClientResponse -> {
          
          }this.proxyInfoSupplier);
      ClientResponse<ApplicationResponse, Errors> clientResponse = cleanSpeakClient.retrieveApplications();
      if (clientResponse.wasSuccessful())
        this.cleanSpeakApplications = ((ApplicationResponse)clientResponse.successResponse).applications; 
    } 
  }
}
