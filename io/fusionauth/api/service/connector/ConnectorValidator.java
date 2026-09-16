package io.fusionauth.api.service.connector;

import com.inversoft.error.Errors;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;

public interface ConnectorValidator {
  Errors validate(BaseConnectorConfiguration paramBaseConnectorConfiguration, boolean paramBoolean);
}
