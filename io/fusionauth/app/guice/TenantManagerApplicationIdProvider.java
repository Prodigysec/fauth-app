package io.fusionauth.app.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.fusionauth.api.service.system.ApplicationReaderService;
import java.util.UUID;

@Singleton
public class TenantManagerApplicationIdProvider implements Provider<UUID> {
  private final ApplicationReaderService reader;
  
  private UUID tenantManagerApplicationId;
  
  @Inject
  public TenantManagerApplicationIdProvider(ApplicationReaderService paramApplicationReaderService) {
    this.reader = paramApplicationReaderService;
    this.tenantManagerApplicationId = paramApplicationReaderService.retrieveTenantManagerApplicationId();
  }
  
  public UUID get() {
    return this.tenantManagerApplicationId;
  }
  
  public void update() {
    this.tenantManagerApplicationId = this.reader.retrieveTenantManagerApplicationId();
  }
}
