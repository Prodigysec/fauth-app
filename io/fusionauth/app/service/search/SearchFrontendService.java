package io.fusionauth.app.service.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.StringTools;
import io.fusionauth.api.util.UUIDTools;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.api.KeyResponse;
import io.fusionauth.domain.api.KeySearchRequest;
import io.fusionauth.domain.api.KeySearchResponse;
import io.fusionauth.domain.search.EntitySearchCriteria;
import io.fusionauth.domain.search.KeySearchCriteria;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public interface SearchFrontendService {
  static KeySearchResponse searchForSigningKeys(String paramString, FusionAuthClient paramFusionAuthClient, Function<Key, Boolean> paramFunction) {
    KeySearchCriteria keySearchCriteria = new KeySearchCriteria();
    keySearchCriteria.numberOfResults = 25;
    if (StringTools.isNotBlank(paramString)) {
      UUID uUID = UUIDTools.fromString(paramString);
      if (uUID != null) {
        KeySearchResponse keySearchResponse = new KeySearchResponse();
        ClientResponse<KeyResponse, Errors> clientResponse1 = paramFusionAuthClient.retrieveKey(uUID);
        if (clientResponse1.wasSuccessful()) {
          Key key = ((KeyResponse)clientResponse1.successResponse).key;
          if (key != null && !((Boolean)paramFunction.apply(key)).booleanValue()) {
            keySearchResponse.keys.add(key);
            keySearchResponse.total = 1L;
          } 
        } 
        return keySearchResponse;
      } 
      keySearchCriteria.name = paramString;
    } 
    KeySearchRequest keySearchRequest = new KeySearchRequest(keySearchCriteria);
    ClientResponse<KeySearchResponse, Errors> clientResponse = paramFusionAuthClient.searchKeys(keySearchRequest);
    if (clientResponse.wasSuccessful()) {
      KeySearchResponse keySearchResponse = (KeySearchResponse)clientResponse.successResponse;
      Objects.requireNonNull(paramFunction);
      keySearchResponse.keys.removeIf(paramFunction::apply);
      return keySearchResponse;
    } 
    return new KeySearchResponse();
  }
  
  String buildEntitySearchQuery(FrontendEntitySearchCriteria paramFrontendEntitySearchCriteria);
  
  public static class FrontendEntitySearchCriteria extends EntitySearchCriteria {
    @JsonIgnore
    public UUID id;
    
    @JsonIgnore
    public UUID tenantId;
    
    @JsonIgnore
    public UUID typeId;
  }
}
