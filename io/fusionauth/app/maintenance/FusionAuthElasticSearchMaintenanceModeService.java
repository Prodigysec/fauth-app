package io.fusionauth.app.maintenance;

import com.google.inject.Inject;
import com.inversoft.configuration.InversoftConfiguration;
import com.inversoft.maintenance.search.ElasticsearchMaintenanceModeSearchService;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.SearchEngineType;

public class FusionAuthElasticSearchMaintenanceModeService extends ElasticsearchMaintenanceModeSearchService {
  private final FusionAuthConfiguration configuration;
  
  @Inject
  public FusionAuthElasticSearchMaintenanceModeService(InversoftConfiguration paramInversoftConfiguration) {
    super(paramInversoftConfiguration);
    this.configuration = (FusionAuthConfiguration)paramInversoftConfiguration;
  }
  
  public boolean isEnabled() {
    return (this.configuration.searchEngineType() == SearchEngineType.elasticsearch);
  }
  
  public String resolveIndexName(String paramString) {
    return paramString + "_a";
  }
}
