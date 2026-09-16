package io.fusionauth.app.action.api.webhook;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.event.WebhookService;
import io.fusionauth.app.action.api.BaseSearchAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.WebhookSearchRequest;
import io.fusionauth.domain.api.WebhookSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.WebhookSearchCriteria;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class SearchAction extends BaseSearchAPIAction<WebhookSearchCriteria> {
  private final WebhookService webhookService;
  
  @JSONRequest
  public WebhookSearchRequest request = new WebhookSearchRequest();
  
  @JSONResponse
  public WebhookSearchResponse response;
  
  @Inject
  public SearchAction(AuthenticationKeyCache paramAuthenticationKeyCache, FrontEndSupport paramFrontEndSupport, WebhookService paramWebhookService) {
    super(paramAuthenticationKeyCache, paramFrontEndSupport);
    this.webhookService = paramWebhookService;
  }
  
  public String description() {
    return (criteria()).description;
  }
  
  public void setDescription(String paramString) {
    (criteria()).description = paramString;
  }
  
  public void setTenantId(UUID paramUUID) {
    (criteria()).tenantId = paramUUID;
  }
  
  public void setUrl(String paramString) {
    (criteria()).url = paramString;
  }
  
  public UUID tenantId() {
    return (criteria()).tenantId;
  }
  
  public String url() {
    return (criteria()).url;
  }
  
  protected WebhookSearchCriteria criteria() {
    return this.request.search;
  }
  
  protected String search() {
    this.response = new WebhookSearchResponse(this.webhookService.search(this.request.search));
    return "render";
  }
}
