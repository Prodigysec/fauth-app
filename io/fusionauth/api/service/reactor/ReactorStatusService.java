package io.fusionauth.api.service.reactor;

import io.fusionauth.domain.reactor.ReactorStatus;

public interface ReactorStatusService {
  ReactorStatus retrieveStatus();
}
