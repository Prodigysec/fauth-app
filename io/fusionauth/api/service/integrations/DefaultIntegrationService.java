package io.fusionauth.api.service.integrations;

import com.google.inject.Inject;
import com.inversoft.cache.CacheNotifier;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ApplicationMapper;
import io.fusionauth.api.domain.IntegrationMapper;
import io.fusionauth.api.domain.mybatis.IntegrationsResultHandler;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Integrations;

public class DefaultIntegrationService implements IntegrationService {
  private final ApplicationMapper applicationMapper;
  
  private final CacheNotifier cacheNotifier;
  
  private final IntegrationMapper mapper;
  
  @Inject
  public DefaultIntegrationService(ApplicationMapper paramApplicationMapper, CacheNotifier paramCacheNotifier, IntegrationMapper paramIntegrationMapper) {
    this.applicationMapper = paramApplicationMapper;
    this.cacheNotifier = paramCacheNotifier;
    this.mapper = paramIntegrationMapper;
  }
  
  public Integrations retrieve() {
    IntegrationsResultHandler integrationsResultHandler = new IntegrationsResultHandler();
    this.mapper.retrieve(integrationsResultHandler);
    return integrationsResultHandler.integrations;
  }
  
  public void update(Integrations paramIntegrations) {
    Integrations integrations = retrieve();
    if (paramIntegrations.cleanspeak != null)
      paramIntegrations.cleanspeak.applicationIds = null; 
    if (integrations.cleanspeak.enabled != ((paramIntegrations.cleanspeak != null && paramIntegrations.cleanspeak.enabled)))
      this.applicationMapper.retrieveAllIgnoreActive(null)
        .stream()
        .filter(paramApplication -> (paramApplication.cleanSpeakConfiguration != null))
        .forEach(paramApplication -> this.applicationMapper.update(paramApplication.with(()))); 
    this.mapper.update(paramIntegrations);
    this.cacheNotifier.reload("Integrations");
  }
  
  public Errors validate(Integrations paramIntegrations) {
    paramIntegrations.cleanspeak.normalize();
    paramIntegrations.kafka.normalize();
    return (new Validator())
      
      .validAbsoluteHttpURL(paramIntegrations.cleanspeak.url, "integrations.cleanspeak.url", new Object[] { paramIntegrations.cleanspeak.url }).ifTrue(paramIntegrations.cleanspeak.enabled, paramValidator -> paramValidator.notMissing(paramIntegrations.cleanspeak.url, "integrations.cleanspeak.url", new Object[0]).ifTrue(paramIntegrations.cleanspeak.usernameModeration.enabled, ()))




      
      .ifTrue(paramIntegrations.kafka.enabled, paramValidator -> paramValidator.notMissing(paramIntegrations.kafka.defaultTopic, "integrations.kafka.defaultTopic", new Object[0]))

      
      .done();
  }
}
