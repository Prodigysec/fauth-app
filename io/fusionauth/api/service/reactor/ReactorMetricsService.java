package io.fusionauth.api.service.reactor;

import io.fusionauth.domain.reactor.ReactorMetrics;

public interface ReactorMetricsService {
  ReactorMetrics retrieveMetrics();
}
