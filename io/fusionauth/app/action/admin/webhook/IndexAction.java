package io.fusionauth.app.action.admin.webhook;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.admin.BaseSearchAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Webhook;
import io.fusionauth.domain.api.WebhookResponse;
import io.fusionauth.domain.api.WebhookSearchRequest;
import io.fusionauth.domain.api.WebhookSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.WebhookSearchCriteria;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "webhook_manager"})
public class IndexAction extends BaseSearchAction<Webhook, WebhookSearchCriteria> {
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  protected WebhookSearchCriteria defaultSearchCriteria() {
    return new WebhookSearchCriteria();
  }
  
  protected SearchResults<Webhook> search() {
    UUID uUID = parseUUID(this.s.url);
    if (uUID != null) {
      ClientResponse<WebhookResponse, Void> clientResponse = this.superClient.retrieveWebhook(uUID);
      if (clientResponse.wasSuccessful())
        return new SearchResults<>(List.of(((WebhookResponse)clientResponse.getSuccessResponse()).webhook), 1L); 
      return null;
    } 
    WebhookSearchResponse webhookSearchResponse = superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.searchWebhooks(new WebhookSearchRequest(this.s)));
    return new SearchResults<>(webhookSearchResponse.webhooks, webhookSearchResponse.total);
  }
}
