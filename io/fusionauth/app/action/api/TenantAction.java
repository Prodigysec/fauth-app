package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.event.WebhookService;
import io.fusionauth.api.service.system.TenantService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Webhook;
import io.fusionauth.domain.api.TenantDeleteRequest;
import io.fusionauth.domain.api.TenantRequest;
import io.fusionauth.domain.api.TenantResponse;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.WebhookSearchCriteria;
import io.fusionauth.domain.util.Normalizer;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{tenantId}", requiresAuthentication = true, scheme = {"api"})
public class TenantAction extends BaseTenantAPIAction implements Patchable {
  @JSONRequest(httpMethods = {"DELETE"})
  public final TenantDeleteRequest deleteRequest = new TenantDeleteRequest();
  
  private final TenantService tenantService;
  
  private final WebhookService webhookService;
  
  @JSONPatch
  @JSONRequest(httpMethods = {"GET", "PATCH", "POST", "PUT"})
  public TenantRequest request = new TenantRequest();
  
  @JSONResponse
  public TenantResponse response;
  
  @PreParameter
  public UUID tenantId;
  
  private TenantService.ValidationResult result;
  
  @Inject
  public TenantAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, TenantService paramTenantService, WebhookService paramWebhookService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.tenantService = paramTenantService;
    this.webhookService = paramWebhookService;
  }
  
  public String delete() {
    if (this.result.existing == null)
      return "missing"; 
    if (this.deleteRequest.async) {
      this.tenantService.deleteAsync(this.result.existing, this.deleteRequest.eventInfo);
      return "accepted-status";
    } 
    this.tenantService.delete(this.result.existing, this.deleteRequest.eventInfo);
    return "success";
  }
  
  public String get() {
    if (this.tenantId == null) {
      this.response = new TenantResponse(this.frontEndSupport.tenantReader.retrieveAll());
    } else {
      if (this.result.existing == null)
        return "missing"; 
      this.response = new TenantResponse(this.result.existing);
    } 
    return "render";
  }
  
  public void loadExisting() {
    if (this.tenantId != null) {
      this.request.tenant = this.frontEndSupport.tenantReader.retrieveById(this.tenantId);
      if (this.request.tenant != null) {
        WebhookSearchCriteria webhookSearchCriteria = new WebhookSearchCriteria();
        webhookSearchCriteria.tenantId = this.tenantId;
        SearchResults<Webhook> searchResults = this.webhookService.search(webhookSearchCriteria);
        if (searchResults.total > 0L)
          this.request.webhookIds = (List<UUID>)searchResults.results.stream().filter(paramWebhook -> !paramWebhook.global).map(paramWebhook -> paramWebhook.id).collect(Collectors.toList()); 
      } 
    } 
  }
  
  public String post() {
    this.tenantService.create(this.result.tenant, this.request.webhookIds);
    this.response = new TenantResponse(this.result.tenant);
    return "render";
  }
  
  public String put() {
    if (this.result.existing == null)
      return "missing"; 
    this.tenantService.update(this.result.existing, this.result.tenant, this.request.eventInfo, this.request.webhookIds);
    if (!this.result.existing.configured)
      this.tenantService.setConfigured(this.result.tenant); 
    this.response = new TenantResponse(this.result.tenant);
    return "render";
  }
  
  public void setAsync(boolean paramBoolean) {
    this.deleteRequest.async = paramBoolean;
  }
  
  @ValidationMethod(httpMethods = {"POST", "PUT", "PATCH"})
  public void validate() {
    if (this.request.tenant == null) {
      this.frontEndSupport.addFieldError("tenant", "[missing]tenant", new Object[0]);
      return;
    } 
    UUID uUID = getOptionalTenantId();
    if (uUID != null) {
      if (this.frontEndSupport.isPOST()) {
        this.frontEndSupport.addGeneralError("[restricted]", new Object[0]);
        return;
      } 
      if (!uUID.equals(this.tenantId)) {
        this.result = new TenantService.ValidationResult();
        return;
      } 
    } 
    this.request.tenant.id = this.tenantId;
    this.request.tenant.normalize();
    Normalizer.deDuplicate(this.request.webhookIds);
    if (this.frontEndSupport.isPOST() && this.request.sourceTenantId != null) {
      this.result = this.tenantService.validateCopy(this.request.tenant, this.request.sourceTenantId);
    } else {
      this.result = this.tenantService.validate(this.request.tenant, this.frontEndSupport.isPOST(), this.request.webhookIds, this.request.sourceTenantId);
    } 
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    this.result = this.tenantService.validateDelete(getOptionalTenant(), this.tenantId);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    this.result = this.tenantService.validateGet(getOptionalTenant(), this.tenantId);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  protected boolean resolveDefaultTenant() {
    return false;
  }
}
