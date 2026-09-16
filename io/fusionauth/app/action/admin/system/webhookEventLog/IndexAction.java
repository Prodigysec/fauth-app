package io.fusionauth.app.action.admin.system.webhookEventLog;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.admin.BaseSearchAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.WebhookEventLog;
import io.fusionauth.domain.WebhookEventResult;
import io.fusionauth.domain.api.WebhookEventLogSearchRequest;
import io.fusionauth.domain.api.WebhookEventLogSearchResponse;
import io.fusionauth.domain.event.EventType;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.WebhookEventLogSearchCriteria;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "webhook_event_log_viewer"})
public class IndexAction extends BaseSearchAction<WebhookEventLog, WebhookEventLogSearchCriteria> {
  @FTLVariable
  public List<EventType> eventTypes = EventType.allTypes();
  
  @FTLVariable
  public List<WebhookEventResult> webhookResults = WebhookEventResult.allResults();
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  protected WebhookEventLogSearchCriteria defaultSearchCriteria() {
    return new WebhookEventLogSearchCriteria();
  }
  
  protected SearchResults<WebhookEventLog> search() {
    WebhookEventLogSearchResponse webhookEventLogSearchResponse = superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.searchWebhookEventLogs(new WebhookEventLogSearchRequest(this.s)));
    return new SearchResults<>(webhookEventLogSearchResponse.webhookEventLogs, webhookEventLogSearchResponse.total);
  }
}
