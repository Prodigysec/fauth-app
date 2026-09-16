package io.fusionauth.api.migration.guice;

import com.google.inject.Inject;
import com.inversoft.migration.Migration;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.RuntimeMode;
import io.fusionauth.api.service.reindex.ReindexService;

public class Migration_1_13_0 implements Migration {
  private final FusionAuthConfiguration configuration;
  
  private final ReindexService reindexService;
  
  @Inject
  public Migration_1_13_0(FusionAuthConfiguration paramFusionAuthConfiguration, ReindexService paramReindexService) {
    this.configuration = paramFusionAuthConfiguration;
    this.reindexService = paramReindexService;
  }
  
  public void cleanup() {}
  
  public void runOnce() throws InterruptedException {
    this.reindexService.reindexUsers();
    if (this.configuration.runtimeMode() != RuntimeMode.Production)
      Thread.sleep(5000L); 
  }
}
