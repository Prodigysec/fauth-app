package io.fusionauth.api.service.cache;

import com.google.inject.Inject;
import com.inversoft.cache.BaseCacheLoader;
import com.inversoft.cache.Cache;
import io.fusionauth.api.domain.WebhookMapper;
import io.fusionauth.domain.Webhook;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class WebhookCacheLoader extends BaseCacheLoader<UUID, Webhook> implements Runnable {
  private final WebhookCache cache;
  
  private final WebhookMapper webhookMapper;
  
  @Inject
  public WebhookCacheLoader(WebhookCache paramWebhookCache, WebhookMapper paramWebhookMapper) {
    super(Collections.emptyList());
    this.cache = paramWebhookCache;
    this.webhookMapper = paramWebhookMapper;
  }
  
  public void run() {
    load();
  }
  
  protected void internalLoad(Cache<UUID, Webhook> paramCache) {
    paramCache.replace((Map)this.webhookMapper.retrieveAll().stream().collect(Collectors.toMap(paramWebhook -> paramWebhook.id, paramWebhook -> paramWebhook)));
  }
  
  protected Cache<UUID, Webhook> resolveCache() {
    return (Cache<UUID, Webhook>)this.cache;
  }
}
