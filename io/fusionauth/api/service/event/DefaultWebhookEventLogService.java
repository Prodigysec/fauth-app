package io.fusionauth.api.service.event;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.WebhookAttemptLogMapper;
import io.fusionauth.api.domain.WebhookEventLogMapper;
import io.fusionauth.api.service.cache.SystemConfigurationCache;
import io.fusionauth.domain.WebhookAttemptLog;
import io.fusionauth.domain.WebhookEventLog;
import io.fusionauth.domain.WebhookEventResult;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.WebhookEventLogSearchCriteria;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultWebhookEventLogService implements WebhookEventLogService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultWebhookEventLogService.class);
  
  private final WebhookAttemptLogMapper secondaryWebhookAttemptLogMapper;
  
  private final WebhookEventLogMapper secondaryWebhookEventLogMapper;
  
  private final SystemConfigurationCache systemConfigurationCache;
  
  private final WebhookAttemptLogMapper webhookAttemptLogMapper;
  
  private final WebhookEventLogMapper webhookEventLogMapper;
  
  @Inject
  public DefaultWebhookEventLogService(WebhookAttemptLogMapper paramWebhookAttemptLogMapper1, WebhookEventLogMapper paramWebhookEventLogMapper1, SystemConfigurationCache paramSystemConfigurationCache, @Named("secondary") WebhookAttemptLogMapper paramWebhookAttemptLogMapper2, @Named("secondary") WebhookEventLogMapper paramWebhookEventLogMapper2) {
    this.webhookAttemptLogMapper = paramWebhookAttemptLogMapper1;
    this.secondaryWebhookAttemptLogMapper = paramWebhookAttemptLogMapper2;
    this.webhookEventLogMapper = paramWebhookEventLogMapper1;
    this.secondaryWebhookEventLogMapper = paramWebhookEventLogMapper2;
    this.systemConfigurationCache = paramSystemConfigurationCache;
  }
  
  public void createWebhookAttemptLog(Supplier<WebhookAttemptLog> paramSupplier) {
    if ((this.systemConfigurationCache.get()).webhookEventLogConfiguration.enabled) {
      WebhookAttemptLog webhookAttemptLog = paramSupplier.get();
      if (webhookAttemptLog.id == null)
        webhookAttemptLog.id = UUID.randomUUID(); 
      this.secondaryWebhookAttemptLogMapper.create(webhookAttemptLog);
    } 
  }
  
  public void createWebhookEventLog(Supplier<WebhookEventLog> paramSupplier) {
    if ((this.systemConfigurationCache.get()).webhookEventLogConfiguration.enabled) {
      WebhookEventLog webhookEventLog = paramSupplier.get();
      webhookEventLog.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
      webhookEventLog.lastUpdateInstant = webhookEventLog.insertInstant;
      this.secondaryWebhookEventLogMapper.create(webhookEventLog);
    } else {
      logger.debug("Skipping webhook event log creation as the webhook event log is disabled.");
    } 
  }
  
  public SearchResults<WebhookEventLog> searchWebhookEventLog(WebhookEventLogSearchCriteria paramWebhookEventLogSearchCriteria) {
    int i = this.webhookEventLogMapper.retrieveCountByCriteria(paramWebhookEventLogSearchCriteria);
    List<WebhookEventLog> list = (i > 0) ? this.webhookEventLogMapper.retrieveByCriteria(paramWebhookEventLogSearchCriteria) : List.of();
    return new SearchResults<>(list, i);
  }
  
  public void updateWebhookEventLogResult(UUID paramUUID, WebhookEventResult paramWebhookEventResult) {
    if ((this.systemConfigurationCache.get()).webhookEventLogConfiguration.enabled) {
      ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
      WebhookEventLog webhookEventLog = (new WebhookEventLog()).with(paramWebhookEventLog -> paramWebhookEventLog.id = paramUUID).with(paramWebhookEventLog -> paramWebhookEventLog.eventResult = paramWebhookEventResult).with(paramWebhookEventLog -> paramWebhookEventLog.lastAttemptInstant = paramZonedDateTime).with(paramWebhookEventLog -> paramWebhookEventLog.lastUpdateInstant = paramZonedDateTime);
      this.secondaryWebhookEventLogMapper.updateStatus(webhookEventLog);
    } else {
      logger.debug("Webhook event with id [{}] not updated to [{}] as the webhook event log is disabled.", paramUUID, paramWebhookEventResult);
    } 
  }
  
  public WebhookEventLogService.ValidationResult validateRetrieveWebhookAttemptLogById(UUID paramUUID) {
    WebhookEventLogService.ValidationResult validationResult = new WebhookEventLogService.ValidationResult();
    validationResult.webhookAttemptLog = this.webhookAttemptLogMapper.retrieveById(paramUUID);
    validationResult
      .errors = (new Validator()).notMissing(paramUUID, "id", new Object[0]).done();
    return validationResult;
  }
  
  public WebhookEventLogService.ValidationResult validateRetrieveWebhookEventLogById(UUID paramUUID) {
    WebhookEventLogService.ValidationResult validationResult = new WebhookEventLogService.ValidationResult();
    validationResult.webhookEventLog = this.webhookEventLogMapper.retrieveById(paramUUID);
    validationResult
      .errors = (new Validator()).notMissing(paramUUID, "id", new Object[0]).done();
    return validationResult;
  }
}
