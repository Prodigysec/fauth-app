package io.fusionauth.app.action.api.entity.grant;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.entity.EntityService;
import io.fusionauth.app.action.api.BaseSearchAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.EntityGrantSearchRequest;
import io.fusionauth.domain.api.EntityGrantSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.EntityGrantSearchCriteria;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api"})
public class SearchAction extends BaseSearchAPIAction<EntityGrantSearchCriteria> {
  @JSONRequest
  public final EntityGrantSearchRequest request = new EntityGrantSearchRequest();
  
  private final EntityService entityService;
  
  @JSONResponse
  public EntityGrantSearchResponse response;
  
  @Inject
  public SearchAction(AuthenticationKeyCache paramAuthenticationKeyCache, FrontEndSupport paramFrontEndSupport, EntityService paramEntityService) {
    super(paramAuthenticationKeyCache, paramFrontEndSupport);
    this.entityService = paramEntityService;
  }
  
  public UUID getEntityId() {
    return (criteria()).entityId;
  }
  
  public void setEntityId(UUID paramUUID) {
    (criteria()).entityId = paramUUID;
  }
  
  public String getName() {
    return (criteria()).name;
  }
  
  public void setName(String paramString) {
    (criteria()).name = paramString;
  }
  
  public UUID getUserId() {
    return (criteria()).userId;
  }
  
  public void setUserId(UUID paramUUID) {
    (criteria()).userId = paramUUID;
  }
  
  protected EntityGrantSearchCriteria criteria() {
    return this.request.search;
  }
  
  protected String search() {
    this.response = new EntityGrantSearchResponse(this.entityService.searchGrants(this.request.search));
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    EntityService.ValidationResult validationResult = this.entityService.validateSearchRequest(this.request, "search.");
    this.frontEndSupport.transfer(validationResult.errors);
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    EntityService.ValidationResult validationResult = this.entityService.validateSearchRequest(this.request, "");
    this.frontEndSupport.transfer(validationResult.errors);
  }
}
