package io.fusionauth.api.service.connector;

import io.fusionauth.api.service.authentication.AuthenticationService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.connector.ConnectorPolicy;
import java.util.List;

public interface Connector {
  AuthenticationService.AuthenticationResult authenticate(BaseConnectorConfiguration paramBaseConnectorConfiguration, ConnectorPolicy paramConnectorPolicy, Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, List<IdentityType> paramList, String paramString2, EventInfo paramEventInfo);
}
