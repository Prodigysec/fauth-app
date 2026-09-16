package io.fusionauth.api.service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.inversoft.cache.CacheNotifier;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.WebhookAttemptLogMapper;
import io.fusionauth.api.domain.WebhookMapper;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.api.util.KeyValidator;
import io.fusionauth.api.util.MapperTools;
import io.fusionauth.domain.Webhook;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.WebhookSearchCriteria;
import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.mybatis.guice.transactional.Transactional;

public class DefaultWebhookService implements WebhookService {
  private final CacheNotifier cacheNotifier;
  
  private final KeyValidator keyValidator;
  
  private final ObjectMapper objectMapper;
  
  private final TenantReaderService tenantReader;
  
  private final WebhookAttemptLogMapper webhookAttemptLogMapper;
  
  private final WebhookMapper webhookMapper;
  
  @Inject
  public DefaultWebhookService(CacheNotifier paramCacheNotifier, KeyValidator paramKeyValidator, ObjectMapper paramObjectMapper, TenantReaderService paramTenantReaderService, WebhookAttemptLogMapper paramWebhookAttemptLogMapper, WebhookMapper paramWebhookMapper) {
    this.cacheNotifier = paramCacheNotifier;
    this.keyValidator = paramKeyValidator;
    this.objectMapper = paramObjectMapper;
    this.tenantReader = paramTenantReaderService;
    this.webhookAttemptLogMapper = paramWebhookAttemptLogMapper;
    this.webhookMapper = paramWebhookMapper;
  }
  
  @Transactional
  public void _create(Webhook paramWebhook) {
    if (paramWebhook.id == null)
      paramWebhook.id = UUID.randomUUID(); 
    paramWebhook.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramWebhook.lastUpdateInstant = paramWebhook.insertInstant;
    this.webhookMapper.create(paramWebhook);
    MapperTools.safeCreateUpdate(5000, paramWebhook.tenantIds, paramList -> this.webhookMapper.createAssociationsToTenants(paramWebhook.id, paramList));
  }
  
  @Transactional
  public void _update(Webhook paramWebhook1, Webhook paramWebhook2) {
    this.webhookMapper.deleteAssociationsByWebhookId(paramWebhook2.id);
    MapperTools.safeCreateUpdate(5000, paramWebhook2.tenantIds, paramList -> this.webhookMapper.createAssociationsToTenants(paramWebhook.id, paramList));
    paramWebhook2.insertInstant = paramWebhook1.insertInstant;
    paramWebhook2.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    this.webhookMapper.update(paramWebhook2);
  }
  
  public void create(Webhook paramWebhook) {
    _create(paramWebhook);
    this.cacheNotifier.reload("Webhooks");
  }
  
  public int delete(UUID paramUUID) {
    this.webhookMapper.deleteAssociationsByWebhookId(paramUUID);
    this.webhookAttemptLogMapper.deleteAttemptsByWebhookId(paramUUID);
    int i = this.webhookMapper.delete(paramUUID);
    if (i == 1)
      this.cacheNotifier.reload("Webhooks"); 
    return i;
  }
  
  public List<Webhook> retrieveAll() {
    return this.webhookMapper.retrieveAll();
  }
  
  public Webhook retrieveById(UUID paramUUID) {
    return this.webhookMapper.retrieveById(paramUUID);
  }
  
  public Webhook retrieveByURL(URI paramURI) {
    return this.webhookMapper.retrieveByURL(paramURI);
  }
  
  public SearchResults<Webhook> search(WebhookSearchCriteria paramWebhookSearchCriteria) {
    int i = this.webhookMapper.retrieveCountByCriteria(paramWebhookSearchCriteria);
    if (i == 0)
      return new SearchResults<>(Collections.emptyList(), i); 
    List<Webhook> list = this.webhookMapper.retrieveByCriteria(paramWebhookSearchCriteria);
    return new SearchResults<>(list, i);
  }
  
  public void update(Webhook paramWebhook1, Webhook paramWebhook2) {
    _update(paramWebhook1, paramWebhook2);
    this.cacheNotifier.reload("Webhooks");
  }
  
  public WebhookService.ValidationResult validate(Webhook paramWebhook, boolean paramBoolean) {
    WebhookService.ValidationResult validationResult = new WebhookService.ValidationResult();
    validationResult.existing = !paramBoolean ? retrieveById(paramWebhook.id) : null;
    Webhook webhook = (paramBoolean && paramWebhook.id != null) ? retrieveById(paramWebhook.id) : null;
    validationResult
































      
      .errors = (new Validator(this.objectMapper)).notMissing(paramWebhook.url, "webhook.url", new Object[0]).ifTrue(!paramBoolean, paramValidator -> paramValidator.notMissing(paramWebhook.id, "webhookId", new Object[] { paramWebhook.id })).ifTrue(paramWebhook.global, paramValidator -> paramValidator.empty(paramWebhook.tenantIds, "webhook.tenantIds", new Object[0])).notDuplicate(webhook, "webhookId", new Object[] { paramWebhook.id }).ifNoFieldErrors("webhook.tenantIds", paramValidator -> paramValidator.forEach(paramWebhook.tenantIds, ())).ifTrue((paramWebhook.url != null), paramValidator -> paramValidator.validAbsoluteHttpURL(paramWebhook.url, "webhook.url", new Object[] { paramWebhook.url })).maxLength(paramWebhook.description, 255, "webhook.description", new Object[] { Integer.valueOf(255) }).maxLength(paramWebhook.httpAuthenticationPassword, 255, "webhook.httpAuthenticationPassword", new Object[] { Integer.valueOf(255) }).maxLength(paramWebhook.httpAuthenticationUsername, 255, "webhook.httpAuthenticationUsername", new Object[] { Integer.valueOf(255) }).notMissing(paramWebhook.readTimeout, "webhook.readTimeout", new Object[0]).notMissing(paramWebhook.connectTimeout, "webhook.connectTimeout", new Object[0]).ifTrue((paramWebhook.readTimeout != null), paramValidator -> paramValidator.valid((paramWebhook.readTimeout.intValue() > 0), "webhook.readTimeout", new Object[0])).ifTrue((paramWebhook.connectTimeout != null), paramValidator -> paramValidator.valid((paramWebhook.connectTimeout.intValue() > 0), "webhook.connectTimeout", new Object[0])).maxLengthJSON(paramWebhook.headers, 32768, "webhook.headers", new Object[] { Integer.valueOf(32768) }).ensure((paramWebhook.sslCertificate == null || paramWebhook.sslCertificateKeyId == null), "webhook.sslCertificate", "[notMissing]", new Object[0]).maxLength(paramWebhook.sslCertificate, 32768, "webhook.sslCertificate", new Object[] { Integer.valueOf(32768) }).ifTrue((paramWebhook.sslCertificateKeyId != null), paramValidator -> this.keyValidator.validateCertificate(paramValidator, paramWebhook.sslCertificateKeyId, "webhook.sslCertificateKeyId")).ifTrue(paramWebhook.signatureConfiguration.enabled, paramValidator -> paramValidator.notMissing(paramWebhook.signatureConfiguration.signingKeyId, "webhook.signatureConfiguration.signingKeyId", new Object[0])).ifTrue((paramWebhook.signatureConfiguration.signingKeyId != null), paramValidator -> this.keyValidator.validateSigningKeyForWebhook(paramValidator, paramWebhook.signatureConfiguration.signingKeyId, "webhook.signatureConfiguration.signingKeyId")).done();
    return validationResult;
  }
}
