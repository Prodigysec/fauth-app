package io.fusionauth.api.service.search.guice;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.SearchEngineType;
import io.fusionauth.api.service.search.DatabaseEntitySearchEngine;
import io.fusionauth.api.service.search.ElasticsearchEntitySearchEngine;
import io.fusionauth.api.service.search.EntitySearchEngine;

public class EntitySearchEngineProvider implements Provider<EntitySearchEngine> {
  private final FusionAuthConfiguration configuration;
  
  private final Injector injector;
  
  @Inject
  public EntitySearchEngineProvider(Injector paramInjector, FusionAuthConfiguration paramFusionAuthConfiguration) {
    this.injector = paramInjector;
    this.configuration = paramFusionAuthConfiguration;
  }
  
  public EntitySearchEngine get() {
    if (this.configuration.searchEngineType() == SearchEngineType.elasticsearch)
      return (EntitySearchEngine)this.injector.getInstance(ElasticsearchEntitySearchEngine.class); 
    return (EntitySearchEngine)this.injector.getInstance(DatabaseEntitySearchEngine.class);
  }
}
