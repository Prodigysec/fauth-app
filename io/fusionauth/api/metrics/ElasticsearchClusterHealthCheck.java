package io.fusionauth.api.metrics;

import com.codahale.metrics.health.HealthCheck;
import io.fusionauth.api.service.search.SearchEngine;

public class ElasticsearchClusterHealthCheck extends HealthCheck {
  private final SearchEngine<?> searchEngine;
  
  public ElasticsearchClusterHealthCheck(SearchEngine<?> paramSearchEngine) {
    this.searchEngine = paramSearchEngine;
  }
  
  protected HealthCheck.Result check() {
    try {
      SearchEngine.SearchEngineStatus searchEngineStatus = this.searchEngine.status();
      String str = searchEngineStatus.cluster.get("status").asText();
      int i = searchEngineStatus.cluster.get("number_of_nodes").asInt();
      if (str.equals("red") || (str.equals("yellow") && i > 1))
        return HealthCheck.Result.unhealthy("Cluster status : %s", new Object[] { str }); 
      return HealthCheck.Result.healthy("Cluster status : %s", new Object[] { str });
    } catch (Exception exception) {
      return HealthCheck.Result.unhealthy(exception.getMessage());
    } 
  }
}
