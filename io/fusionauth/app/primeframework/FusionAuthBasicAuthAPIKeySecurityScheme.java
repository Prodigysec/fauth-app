package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.guice.FusionAuthLocalURLProvider;
import io.fusionauth.api.service.cache.IPAccessControlListCache;
import io.fusionauth.api.service.cache.SystemConfigurationCache;
import io.fusionauth.api.util.EncoderTools;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.action.ActionInvocationStore;

public class FusionAuthBasicAuthAPIKeySecurityScheme extends FusionAPIKeySecurityScheme {
  private static final String BasicScheme = "basic ";
  
  @Inject
  public FusionAuthBasicAuthAPIKeySecurityScheme(ActionInvocationStore paramActionInvocationStore, FusionAuthLocalURLProvider paramFusionAuthLocalURLProvider, HTTPRequest paramHTTPRequest, AuthenticationKeyCache paramAuthenticationKeyCache, FusionAuthConfiguration paramFusionAuthConfiguration, IPAccessControlListCache paramIPAccessControlListCache, SystemConfigurationCache paramSystemConfigurationCache) {
    super(paramActionInvocationStore, paramHTTPRequest, paramAuthenticationKeyCache, paramFusionAuthConfiguration, paramFusionAuthLocalURLProvider, paramIPAccessControlListCache, paramSystemConfigurationCache);
  }
  
  protected String authenticationKey() {
    String str = this.request.getHeader("Authorization");
    if (str != null && 
      str.toLowerCase().startsWith("basic "))
      try {
        str = str.substring("basic ".length());
        String str1 = EncoderTools.Base64.decodeToString(str);
        String[] arrayOfString = str1.split(":");
        if (arrayOfString[0].equals("apikey"))
          return arrayOfString[1]; 
      } catch (Exception exception) {} 
    return null;
  }
}
