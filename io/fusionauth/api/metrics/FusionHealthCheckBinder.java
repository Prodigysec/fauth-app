package io.fusionauth.api.metrics;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.inject.Inject;
import com.inversoft.configuration.InversoftConfiguration;
import io.fusionauth.api.domain.VersionMapper;
import io.fusionauth.api.service.search.ElasticsearchUserSearchEngine;
import io.fusionauth.api.service.search.UserSearchEngine;

public class FusionHealthCheckBinder {
  @Inject
  public FusionHealthCheckBinder(InversoftConfiguration paramInversoftConfiguration, HealthCheckRegistry paramHealthCheckRegistry, UserSearchEngine paramUserSearchEngine, VersionMapper paramVersionMapper) {
    paramHealthCheckRegistry.register("Database-primary", new DatabaseHealthCheck(paramInversoftConfiguration, paramVersionMapper));
    if (paramUserSearchEngine instanceof ElasticsearchUserSearchEngine) {
      ElasticsearchUserSearchEngine elasticsearchUserSearchEngine = (ElasticsearchUserSearchEngine)paramUserSearchEngine;
      paramHealthCheckRegistry.register("ElasticsearchCluster", new ElasticsearchClusterHealthCheck(elasticsearchUserSearchEngine));
    } 
  }
}
