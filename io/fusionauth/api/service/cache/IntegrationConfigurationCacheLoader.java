package io.fusionauth.api.service.cache;

import com.google.inject.Inject;
import com.inversoft.cache.BaseCacheLoader;
import com.inversoft.cache.Cache;
import io.fusionauth.api.domain.IntegrationMapper;
import io.fusionauth.api.domain.mybatis.IntegrationsResultHandler;
import io.fusionauth.api.service.messaging.KafkaProducerProvider;
import io.fusionauth.domain.Integration;
import io.fusionauth.domain.Integrations;
import java.util.Collections;
import java.util.HashMap;

public class IntegrationConfigurationCacheLoader extends BaseCacheLoader<String, Integration> implements Runnable {
  private final Cache<String, Integration> cache;
  
  private final IntegrationMapper integrationMapper;
  
  @Inject
  public IntegrationConfigurationCacheLoader(Cache<String, Integration> paramCache, IntegrationMapper paramIntegrationMapper, KafkaProducerProvider paramKafkaProducerProvider) {
    super(Collections.singletonList(paramKafkaProducerProvider));
    this.cache = paramCache;
    this.integrationMapper = paramIntegrationMapper;
  }
  
  public void run() {
    load();
  }
  
  protected void internalLoad(Cache<String, Integration> paramCache) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    IntegrationsResultHandler integrationsResultHandler = new IntegrationsResultHandler();
    this.integrationMapper.retrieve(integrationsResultHandler);
    Integrations integrations = integrationsResultHandler.integrations;
    hashMap.put("cleanspeak", integrations.cleanspeak);
    hashMap.put("kafka", integrations.kafka);
    paramCache.replace(hashMap);
  }
  
  protected Cache<String, Integration> resolveCache() {
    return this.cache;
  }
}
