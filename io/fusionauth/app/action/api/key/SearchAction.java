package io.fusionauth.app.action.api.key;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.system.KeyReaderService;
import io.fusionauth.app.action.api.BaseSearchAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.api.KeySearchRequest;
import io.fusionauth.domain.api.KeySearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.KeySearchCriteria;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class SearchAction extends BaseSearchAPIAction<KeySearchCriteria> {
  @JSONRequest
  public final KeySearchRequest request = new KeySearchRequest();
  
  private final KeyReaderService keyReader;
  
  @JSONResponse
  public KeySearchResponse response;
  
  @Inject
  public SearchAction(AuthenticationKeyCache paramAuthenticationKeyCache, FrontEndSupport paramFrontEndSupport, KeyReaderService paramKeyReaderService) {
    super(paramAuthenticationKeyCache, paramFrontEndSupport);
    this.keyReader = paramKeyReaderService;
  }
  
  public Key.KeyAlgorithm algorithm() {
    return (criteria()).algorithm;
  }
  
  public String name() {
    return (criteria()).name;
  }
  
  public void setAlgorithm(Key.KeyAlgorithm paramKeyAlgorithm) {
    (criteria()).algorithm = paramKeyAlgorithm;
  }
  
  public void setName(String paramString) {
    (criteria()).name = paramString;
  }
  
  public void setType(Key.KeyType paramKeyType) {
    (criteria()).type = paramKeyType;
  }
  
  public Key.KeyType type() {
    return (criteria()).type;
  }
  
  protected KeySearchCriteria criteria() {
    return this.request.search;
  }
  
  protected String search() {
    this.response = new KeySearchResponse(this.keyReader.search(this.request.search));
    this.response.keys.forEach(Key::secure);
    return "render";
  }
}
