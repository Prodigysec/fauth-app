package io.fusionauth.app.action.api.application;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.app.action.api.BaseSearchAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ObjectState;
import io.fusionauth.domain.api.ApplicationSearchRequest;
import io.fusionauth.domain.api.ApplicationSearchResponse;
import io.fusionauth.domain.search.ApplicationSearchCriteria;
import io.fusionauth.domain.search.BaseSearchCriteria;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api"})
public class SearchAction extends BaseSearchAPIAction<ApplicationSearchCriteria> {
  @JSONRequest
  public final ApplicationSearchRequest request = new ApplicationSearchRequest();
  
  private final ApplicationReaderService applicationReader;
  
  public List<String> expand;
  
  @JSONResponse
  public ApplicationSearchResponse response;
  
  @Inject
  public SearchAction(AuthenticationKeyCache paramAuthenticationKeyCache, FrontEndSupport paramFrontEndSupport, ApplicationReaderService paramApplicationReaderService) {
    super(paramAuthenticationKeyCache, paramFrontEndSupport);
    this.applicationReader = paramApplicationReaderService;
  }
  
  public String name() {
    return (criteria()).name;
  }
  
  public void setName(String paramString) {
    (criteria()).name = paramString;
  }
  
  public void setState(ObjectState paramObjectState) {
    (criteria()).state = paramObjectState;
  }
  
  public void setTenantId(UUID paramUUID) {
    (criteria()).tenantId = paramUUID;
  }
  
  public void setUniversal(Boolean paramBoolean) {
    (criteria()).universal = paramBoolean;
  }
  
  public ObjectState state() {
    return (criteria()).state;
  }
  
  public UUID tenantId() {
    return (criteria()).tenantId;
  }
  
  public Boolean universal() {
    return (criteria()).universal;
  }
  
  protected ApplicationSearchCriteria criteria() {
    return this.request.search;
  }
  
  protected String search() {
    if (tenantIdWasSpecified())
      (criteria()).tenantId = this.tenant.id; 
    Set<ApplicationReaderService.ApplicationExpansion> set = ApplicationReaderService.ApplicationExpansion.all();
    if (this.frontEndSupport.isPOST())
      this.expand = this.request.expand; 
    if (this.expand != null)
      set = (Set<ApplicationReaderService.ApplicationExpansion>)this.expand.stream().map(ApplicationReaderService.ApplicationExpansion::safeValueOf).filter(Objects::nonNull).collect(Collectors.toSet()); 
    this.response = new ApplicationSearchResponse(this.applicationReader.search(this.request.search, set));
    this.response.applications.forEach(Application::secure);
    if (!set.contains(ApplicationReaderService.ApplicationExpansion.roles))
      this.response.expandable.add(ApplicationReaderService.ApplicationExpansion.roles.name()); 
    if (!set.contains(ApplicationReaderService.ApplicationExpansion.scopes))
      this.response.expandable.add(ApplicationReaderService.ApplicationExpansion.scopes.name()); 
    return "render";
  }
}
