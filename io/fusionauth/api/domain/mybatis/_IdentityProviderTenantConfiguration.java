package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.provider.IdentityProviderTenantConfiguration;
import java.util.UUID;

public class _IdentityProviderTenantConfiguration extends IdentityProviderTenantConfiguration {
  @JsonIgnore
  public UUID tenantId;
}
