package io.fusionauth.api.domain.guice;

import io.fusionauth.api.service.system.APIKeyService;
import javax.inject.Inject;

public class TenantManagerFusionAuthClientProvider extends FusionAuthClientProvider {
  @Inject
  public TenantManagerFusionAuthClientProvider(@FusionAuthLocalClientURL String paramString) {
    super(paramString);
  }
  
  public String getKey() {
    return APIKeyService.TENANT_MANAGER_LOCAL_KEY;
  }
}
