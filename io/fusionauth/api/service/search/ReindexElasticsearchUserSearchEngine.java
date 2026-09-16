package io.fusionauth.api.service.search;

import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.service.cache.SearchIndexNameCache;
import io.fusionauth.domain.User;

public class ReindexElasticsearchUserSearchEngine extends ElasticsearchUserSearchEngine implements ReindexSearchEngine<User> {
  public ReindexElasticsearchUserSearchEngine(FusionAuthConfiguration paramFusionAuthConfiguration, String paramString) {
    super(paramFusionAuthConfiguration, paramString, (SearchIndexNameCache)null);
  }
  
  public String createIndex() {
    return super.createIndex();
  }
  
  public void deleteIndex(String paramString) {
    super.deleteIndex(paramString);
  }
  
  public void updateAliasDeleteOldIndex() {
    super.updateAliasDeleteOldIndex();
  }
}
