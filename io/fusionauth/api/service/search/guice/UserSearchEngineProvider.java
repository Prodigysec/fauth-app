package io.fusionauth.api.service.search.guice;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.SearchEngineType;
import io.fusionauth.api.service.search.DatabaseUserSearchEngine;
import io.fusionauth.api.service.search.ElasticsearchUserSearchEngine;
import io.fusionauth.api.service.search.UserSearchEngine;

public class UserSearchEngineProvider implements Provider<UserSearchEngine> {
  private final FusionAuthConfiguration configuration;
  
  private final Injector injector;
  
  @Inject
  public UserSearchEngineProvider(Injector paramInjector, FusionAuthConfiguration paramFusionAuthConfiguration) {
    this.injector = paramInjector;
    this.configuration = paramFusionAuthConfiguration;
  }
  
  public UserSearchEngine get() {
    if (this.configuration.searchEngineType() == SearchEngineType.elasticsearch)
      return (UserSearchEngine)this.injector.getInstance(ElasticsearchUserSearchEngine.class); 
    return (UserSearchEngine)this.injector.getInstance(DatabaseUserSearchEngine.class);
  }
}
