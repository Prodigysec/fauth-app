package io.fusionauth.api.service.integrations;

import com.inversoft.error.Errors;
import io.fusionauth.domain.Integrations;

public interface IntegrationService {
  Integrations retrieve();
  
  void update(Integrations paramIntegrations);
  
  Errors validate(Integrations paramIntegrations);
}
