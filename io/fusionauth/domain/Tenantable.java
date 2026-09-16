package io.fusionauth.domain;

import java.util.UUID;

public interface Tenantable {
  UUID getTenantId();
}
