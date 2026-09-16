package io.fusionauth.app.action.admin.integration;

import com.google.inject.Inject;
import io.fusionauth.api.service.integrations.IntegrationService;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Enableable;
import io.fusionauth.domain.Integrations;
import java.util.Map;
import java.util.TreeMap;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "system_manager"})
public class IndexAction extends BaseAction {
  private final IntegrationService integrationService;
  
  public boolean anyAvailable;
  
  public boolean anyConfigured;
  
  public Map<String, Enableable> integrations = new TreeMap<>();
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport, IntegrationService paramIntegrationService) {
    super(paramFrontEndSupport);
    this.integrationService = paramIntegrationService;
  }
  
  public String get() {
    this.integrationService.retrieve()
      .with(paramIntegrations -> this.integrations.put("CleanSpeak", paramIntegrations.cleanspeak))
      .with(paramIntegrations -> this.integrations.put("Kafka", paramIntegrations.kafka));
    this.anyAvailable = this.integrations.values().stream().anyMatch(paramEnableable -> !paramEnableable.enabled);
    this.anyConfigured = this.integrations.values().stream().anyMatch(paramEnableable -> paramEnableable.enabled);
    return "input";
  }
}
