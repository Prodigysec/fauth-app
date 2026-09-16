package io.fusionauth.app.action.admin.apiKey;

import com.google.inject.Inject;
import io.fusionauth.api.domain.api.APIKeySearchCriteria;
import io.fusionauth.api.service.system.APIKeyReaderService;
import io.fusionauth.app.action.admin.BaseSearchAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.APIKey;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;

@Forward(code = "success", cacheControl = "no-store")
@Action(requiresAuthentication = true, constraints = {"admin", "api_key_manager"})
public class IndexAction extends BaseSearchAction<APIKey, APIKeySearchCriteria> {
  private final APIKeyReaderService apiKeyReader;
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport, APIKeyReaderService paramAPIKeyReaderService) {
    super(paramFrontEndSupport);
    this.apiKeyReader = paramAPIKeyReaderService;
  }
  
  protected APIKeySearchCriteria defaultSearchCriteria() {
    return new APIKeySearchCriteria();
  }
  
  protected SearchResults<APIKey> search() {
    UUID uUID = parseUUID(this.s.nameOrDescription);
    if (uUID != null) {
      APIKey aPIKey = this.apiKeyReader.retrieveById(this.s.tenantId, uUID);
      return (aPIKey != null) ? 
        new SearchResults<>(List.of(aPIKey), 1L) : 
        null;
    } 
    return this.apiKeyReader.search(this.s);
  }
}
