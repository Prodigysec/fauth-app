package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.ApplicationOAuthScope;

public class ApplicationOAuthScopeRequest {
  public ApplicationOAuthScope scope;
  
  @JacksonConstructor
  public ApplicationOAuthScopeRequest() {}
  
  public ApplicationOAuthScopeRequest(ApplicationOAuthScope paramApplicationOAuthScope) {
    this.scope = paramApplicationOAuthScope;
  }
}
