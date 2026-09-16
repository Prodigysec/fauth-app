package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.reactor.MFATenantMetric;
import java.util.UUID;

public class _MFATenantMetric extends MFATenantMetric {
  @JsonIgnore
  public UUID tenantId;
}
