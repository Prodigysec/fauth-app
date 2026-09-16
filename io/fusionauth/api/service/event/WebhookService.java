package io.fusionauth.api.service.event;

import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.domain.Webhook;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.WebhookSearchCriteria;
import java.net.URI;
import java.util.List;
import java.util.UUID;

public interface WebhookService {
  void create(Webhook paramWebhook);
  
  int delete(UUID paramUUID);
  
  List<Webhook> retrieveAll();
  
  Webhook retrieveById(UUID paramUUID);
  
  Webhook retrieveByURL(URI paramURI);
  
  SearchResults<Webhook> search(WebhookSearchCriteria paramWebhookSearchCriteria);
  
  void update(Webhook paramWebhook1, Webhook paramWebhook2);
  
  ValidationResult validate(Webhook paramWebhook, boolean paramBoolean);
  
  public static class ValidationResult extends BaseValidationResult {
    public Webhook existing;
    
    public Webhook webhook;
  }
}
