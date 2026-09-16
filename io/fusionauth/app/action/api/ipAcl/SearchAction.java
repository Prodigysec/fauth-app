package io.fusionauth.app.action.api.ipAcl;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.ip.IPAccessControlListReaderService;
import io.fusionauth.app.action.api.BaseSearchAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.IPAccessControlListSearchRequest;
import io.fusionauth.domain.api.IPAccessControlListSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.IPAccessControlListSearchCriteria;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class SearchAction extends BaseSearchAPIAction<IPAccessControlListSearchCriteria> {
  @JSONRequest
  public final IPAccessControlListSearchRequest request = new IPAccessControlListSearchRequest();
  
  private final IPAccessControlListReaderService ipAccessControlListReader;
  
  @JSONResponse
  public IPAccessControlListSearchResponse response;
  
  @Inject
  public SearchAction(AuthenticationKeyCache paramAuthenticationKeyCache, IPAccessControlListReaderService paramIPAccessControlListReaderService, FrontEndSupport paramFrontEndSupport) {
    super(paramAuthenticationKeyCache, paramFrontEndSupport);
    this.ipAccessControlListReader = paramIPAccessControlListReaderService;
  }
  
  public String getName() {
    return (criteria()).name;
  }
  
  public void setName(String paramString) {
    (criteria()).name = paramString;
  }
  
  protected IPAccessControlListSearchCriteria criteria() {
    return this.request.search;
  }
  
  protected String search() {
    this.response = new IPAccessControlListSearchResponse(this.ipAccessControlListReader.search(this.request.search));
    return "render";
  }
}
