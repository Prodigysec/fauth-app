package io.fusionauth.api.service.cache;

import com.google.inject.Inject;
import com.inversoft.cache.BaseCacheLoader;
import com.inversoft.cache.Cache;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.SearchEngineType;
import io.fusionauth.api.service.search.BaseElasticsearchSearchEngine;
import io.fusionauth.api.service.search.ElasticsearchEntitySearchEngine;
import io.fusionauth.api.service.search.ElasticsearchUserSearchEngine;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SearchIndexNamesCacheLoader extends BaseCacheLoader<String, List<String>> implements Runnable {
  private final SearchIndexNameCache cache;
  
  private final FusionAuthConfiguration configuration;
  
  private final ElasticsearchEntitySearchEngine entitySearchEngine;
  
  private final ElasticsearchUserSearchEngine userSearchEngine;
  
  @Inject
  public SearchIndexNamesCacheLoader(SearchIndexNameCache paramSearchIndexNameCache, FusionAuthConfiguration paramFusionAuthConfiguration, ElasticsearchEntitySearchEngine paramElasticsearchEntitySearchEngine, ElasticsearchUserSearchEngine paramElasticsearchUserSearchEngine) {
    super(Collections.emptyList());
    this.cache = paramSearchIndexNameCache;
    this.configuration = paramFusionAuthConfiguration;
    this.entitySearchEngine = paramElasticsearchEntitySearchEngine;
    this.userSearchEngine = paramElasticsearchUserSearchEngine;
  }
  
  public void run() {
    load();
  }
  
  protected void internalLoad(Cache<String, List<String>> paramCache) {
    if (this.configuration.searchEngineType() == SearchEngineType.database)
      return; 
    HashMap<Object, Object> hashMap = new HashMap<>();
    BaseElasticsearchSearchEngine.WriteIndicies writeIndicies1 = this.entitySearchEngine.retrieveWriteIndicies();
    hashMap.put(writeIndicies1.alias, writeIndicies1.indices);
    BaseElasticsearchSearchEngine.WriteIndicies writeIndicies2 = this.userSearchEngine.retrieveWriteIndicies();
    hashMap.put(writeIndicies2.alias, writeIndicies2.indices);
    paramCache.replace(hashMap);
  }
  
  protected Cache<String, List<String>> resolveCache() {
    return (Cache<String, List<String>>)this.cache;
  }
}
