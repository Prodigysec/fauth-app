package io.fusionauth.app.action.api.identityProvider;

import com.google.inject.Inject;
import io.fusionauth.api.service.cache.IdentityProviderCache;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.identityProvider.LookupResponse;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class LookupAction extends BaseAPIAction {
  private final IdentityProviderCache identityProviderCache;
  
  public String domain;
  
  @JSONResponse
  public LookupResponse response;
  
  public UUID tenantId;
  
  @Inject
  public LookupAction(FrontEndSupport paramFrontEndSupport, IdentityProviderCache paramIdentityProviderCache) {
    super(paramFrontEndSupport);
    this.identityProviderCache = paramIdentityProviderCache;
  }
  
  public String get() {
    BaseIdentityProvider<?> baseIdentityProvider = this.identityProviderCache.lookup(this.tenantId, this.domain);
    if (baseIdentityProvider == null)
      return "missing"; 
    this.response = new LookupResponse(baseIdentityProvider);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validate() {
    if (this.domain == null)
      this.frontEndSupport.addFieldError("domain", "[missing]domain", new Object[0]); 
  }
}
