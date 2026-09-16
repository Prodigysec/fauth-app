package io.fusionauth.app.action.admin.integration;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.CollectionTools;
import io.fusionauth.api.service.integrations.IntegrationService;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Integrations;
import io.fusionauth.domain.KafkaConfiguration;
import io.fusionauth.domain.api.IntegrationRequest;
import io.fusionauth.domain.api.IntegrationResponse;
import java.util.Map;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "system_manager"})
@Redirect(uri = "/admin/integration/")
public class KafkaAction extends BaseAction {
  private final IntegrationService integrationService;
  
  public Integrations integrations;
  
  public String producerConfiguration;
  
  @Inject
  public KafkaAction(FrontEndSupport paramFrontEndSupport, IntegrationService paramIntegrationService) {
    super(paramFrontEndSupport);
    this.integrationService = paramIntegrationService;
  }
  
  public String get() {
    this.integrations = this.integrationService.retrieve();
    this.integrations.kafka.normalize();
    this.producerConfiguration = CollectionTools.mapToString(this.integrations.kafka.producer);
    return "input";
  }
  
  public String post() {
    Integrations integrations = ((IntegrationResponse)superDelegate().execute(FusionAuthClient::retrieveIntegration)).integrations;
    KafkaConfiguration kafkaConfiguration1 = integrations.kafka;
    integrations.kafka = this.integrations.kafka;
    KafkaConfiguration kafkaConfiguration2 = ((IntegrationResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateIntegrations(new IntegrationRequest(paramIntegrations)))).integrations.kafka;
    this.frontEndSupport.addGeneralInfo("success", new Object[0]);
    writeAuditLogForUpdate("Updated the Kafka integration", kafkaConfiguration1, kafkaConfiguration2);
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    Map<String, String> map = CollectionTools.stringToMap(this.producerConfiguration);
    if (map == null) {
      this.frontEndSupport.addGeneralError("[invalid]producerConfiguration", new Object[0]);
    } else {
      this.integrations.kafka.producer = map;
    } 
  }
}
