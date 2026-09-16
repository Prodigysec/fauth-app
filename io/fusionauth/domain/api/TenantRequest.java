package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Tenant;
import java.util.List;
import java.util.UUID;

public class TenantRequest extends BaseEventRequest {
  public UUID sourceTenantId;
  
  public Tenant tenant;
  
  public List<UUID> webhookIds;
  
  @JacksonConstructor
  public TenantRequest() {}
  
  public TenantRequest(Tenant paramTenant) {
    this.tenant = paramTenant;
  }
  
  public TenantRequest(EventInfo paramEventInfo, Tenant paramTenant, List<UUID> paramList) {
    super(paramEventInfo);
    this.tenant = paramTenant;
    this.webhookIds = paramList;
  }
}
