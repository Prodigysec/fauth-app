package io.fusionauth.api.domain.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.fusionauth.api.domain.ApplicationMapper;
import io.fusionauth.domain.Application;
import java.util.UUID;

@Singleton
public class FusionAuthTenantIdProvider implements Provider<UUID> {
  private UUID tenantId;
  
  @Inject
  public FusionAuthTenantIdProvider(ApplicationMapper paramApplicationMapper) {
    this.tenantId = (paramApplicationMapper.retrieveById(null, Application.FUSIONAUTH_APP_ID)).tenantId;
  }
  
  public UUID get() {
    return this.tenantId;
  }
  
  public void update(UUID paramUUID) {
    this.tenantId = paramUUID;
  }
}
