package io.fusionauth.app.service.identityProvider;

import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public interface IdentityProviderFrontendService {
  PostDataToExternalIDP buildPostData(Tenant paramTenant, FrontendRequestContext paramFrontendRequestContext);
  
  String buildRedirectURI(Tenant paramTenant, FrontendRequestContext paramFrontendRequestContext);
  
  public static class FrontendRequestContext implements Buildable<FrontendRequestContext> {
    public String client_id;
    
    public String connectionTestId;
    
    public String fusionAuthURI;
    
    public BaseIdentityProvider<?> identityProvider;
    
    public String loginId;
    
    public String prompt;
    
    public String state;
  }
  
  public static class PostDataToExternalIDP {
    public Map<String, String> formData = new HashMap<>();
    
    public URI uri;
  }
}
