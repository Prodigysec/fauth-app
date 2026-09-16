package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.reactor.BreachedPasswordTenantMetric;
import java.util.UUID;

public class _BreachedPasswordTenantMetric extends BreachedPasswordTenantMetric {
  @JsonIgnore
  public UUID tenantId;
}
