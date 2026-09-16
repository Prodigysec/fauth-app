package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.fusionauth.app.guice.FusionAuthMVCSecurityModule;
import io.fusionauth.app.primeframework.UndocumentedAPI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.action.config.ActionConfigurationProvider;

public class APIEndpointScopesProvider implements Provider<Map<String, APIKeyService.APIEndpointScope>> {
  private final ActionConfigurationProvider actionConfigurationProvider;
  
  @Inject
  public APIEndpointScopesProvider(ActionConfigurationProvider paramActionConfigurationProvider) {
    this.actionConfigurationProvider = paramActionConfigurationProvider;
  }
  
  public Map<String, APIKeyService.APIEndpointScope> get() {
    HashMap<Object, Object> hashMap = new HashMap<>();
    this.actionConfigurationProvider.getActionConfigurations()
      .stream()
      .filter(paramActionConfiguration -> paramActionConfiguration.uri.startsWith("/api/"))
      .filter(paramActionConfiguration -> !paramActionConfiguration.uri.startsWith("/api/api-key"))
      .filter(paramActionConfiguration -> paramActionConfiguration.annotation.requiresAuthentication())
      .filter(paramActionConfiguration -> {
          Objects.requireNonNull(FusionAuthMVCSecurityModule.apiSecuritySchemes);
          return Arrays.<String>stream(paramActionConfiguration.annotation.scheme()).anyMatch(FusionAuthMVCSecurityModule.apiSecuritySchemes::contains);
        }).filter(paramActionConfiguration -> !paramActionConfiguration.annotations.containsKey(UndocumentedAPI.class))
      .forEach(paramActionConfiguration -> paramMap.put(paramActionConfiguration.uri, Arrays.<String>asList(paramActionConfiguration.annotation.scheme()).contains("api-no-tenant") ? APIKeyService.APIEndpointScope.Global : APIKeyService.APIEndpointScope.TenantScoped));
    Map<?, ?> map = Map.of("/oauth2/device/user-code", APIKeyService.APIEndpointScope.TenantScoped);
    hashMap.putAll(map);
    return Map.copyOf(hashMap);
  }
}
