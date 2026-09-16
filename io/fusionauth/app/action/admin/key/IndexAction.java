package io.fusionauth.app.action.admin.key;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.admin.BaseSearchAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.api.KeyResponse;
import io.fusionauth.domain.api.KeySearchRequest;
import io.fusionauth.domain.api.KeySearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.KeySearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "key_manager"})
public class IndexAction extends BaseSearchAction<Key, KeySearchCriteria> {
  @FTLVariable
  public List<Key.KeyAlgorithm> keyAlgorithms = Arrays.asList(Key.KeyAlgorithm.values());
  
  @FTLVariable
  public List<Key.KeyType> keyTypes = Arrays.asList(Key.KeyType.values());
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  protected KeySearchCriteria defaultSearchCriteria() {
    return new KeySearchCriteria();
  }
  
  protected SearchResults<Key> search() {
    UUID uUID = parseUUID(this.s.name);
    if (uUID != null) {
      ClientResponse<KeyResponse, Errors> clientResponse = this.superClient.retrieveKey(uUID);
      if (clientResponse.wasSuccessful())
        return new SearchResults<>(List.of(((KeyResponse)clientResponse.getSuccessResponse()).key), 1L); 
      return null;
    } 
    KeySearchResponse keySearchResponse = superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.searchKeys(new KeySearchRequest(this.s)));
    return new SearchResults<>(keySearchResponse.keys, keySearchResponse.total);
  }
}
