package io.fusionauth.api.service.event;

import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.domain.WebhookAttemptLog;
import io.fusionauth.domain.WebhookEventLog;
import io.fusionauth.domain.WebhookEventResult;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.WebhookEventLogSearchCriteria;
import java.util.UUID;
import java.util.function.Supplier;

public interface WebhookEventLogService {
  void createWebhookAttemptLog(Supplier<WebhookAttemptLog> paramSupplier);
  
  void createWebhookEventLog(Supplier<WebhookEventLog> paramSupplier);
  
  SearchResults<WebhookEventLog> searchWebhookEventLog(WebhookEventLogSearchCriteria paramWebhookEventLogSearchCriteria);
  
  void updateWebhookEventLogResult(UUID paramUUID, WebhookEventResult paramWebhookEventResult);
  
  ValidationResult validateRetrieveWebhookAttemptLogById(UUID paramUUID);
  
  ValidationResult validateRetrieveWebhookEventLogById(UUID paramUUID);
  
  public static class ValidationResult extends BaseValidationResult {
    public WebhookAttemptLog webhookAttemptLog;
    
    public WebhookEventLog webhookEventLog;
  }
}
