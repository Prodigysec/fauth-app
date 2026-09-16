package io.fusionauth.app.action.admin.apiKey;

import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.system.APIKeyFrontEndMessageKeys;
import io.fusionauth.api.service.system.APIKeyService;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.APIKey;
import io.fusionauth.domain.IPAccessControlList;
import io.fusionauth.domain.api.IPAccessControlListSearchRequest;
import io.fusionauth.domain.api.IPAccessControlListSearchResponse;
import io.fusionauth.domain.search.IPAccessControlListSearchCriteria;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;

@List({@Redirect(code = "api-error", uri = "/admin/api-key/", cacheControl = "no-store"), @Redirect(code = "missing", uri = "/admin/api-key/", cacheControl = "no-store"), @Redirect(code = "success", uri = "/admin/api-key/", cacheControl = "no-store")})
public abstract class BaseFormAction extends BaseAction {
  @FTLVariable
  public final List<IPAccessControlList> ipAccessControlLists = new ArrayList<>();
  
  public APIKey apiKey = new APIKey();
  
  public UUID apiKeyId;
  
  @FTLVariable
  public List<APIKeyEndpoint> endpoints;
  
  @FTLVariable
  public APIKeyFrontEndMessageKeys[] retrievableMessageKeys = APIKeyFrontEndMessageKeys.values();
  
  protected BaseFormAction(FrontEndSupport paramFrontEndSupport, Map<String, APIKeyService.APIEndpointScope> paramMap) {
    super(paramFrontEndSupport);
    paramFrontEndSupport.fieldMapperFunction = (paramString -> paramString.replace("authenticationKey", "apiKey"));
    this

      
      .endpoints = paramMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(paramEntry -> new APIKeyEndpoint((String)paramEntry.getKey(), (APIKeyService.APIEndpointScope)paramEntry.getValue())).toList();
  }
  
  @FormPrepareMethod
  public void formPrepare() {
    this.ipAccessControlLists.addAll((Collection<? extends IPAccessControlList>)Objects.requireNonNullElseGet(((IPAccessControlListSearchResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.searchIPAccessControlLists(new IPAccessControlListSearchRequest((new IPAccessControlListSearchCriteria()).with(()))))).ipAccessControlLists, List::of));
  }
  
  public static class APIKeyEndpoint {
    public final boolean global;
    
    public final APIKeyService.APIEndpointScope scope;
    
    public final String uri;
    
    public APIKeyEndpoint(String param1String, APIKeyService.APIEndpointScope param1APIEndpointScope) {
      this.uri = param1String;
      this.scope = param1APIEndpointScope;
      this.global = (param1APIEndpointScope == APIKeyService.APIEndpointScope.Global);
    }
  }
}
