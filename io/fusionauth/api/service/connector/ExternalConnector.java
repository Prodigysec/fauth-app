package io.fusionauth.api.service.connector;

import io.fusionauth.api.service.authentication.AuthenticationService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.Tenant;
import java.util.List;

public interface ExternalConnector {
  AuthenticationService.AuthenticationResult synchronizeExternalUser(Tenant paramTenant, Application paramApplication, AuthenticationService.AuthenticationResult paramAuthenticationResult, String paramString1, List<IdentityType> paramList, String paramString2, EventInfo paramEventInfo);
}
