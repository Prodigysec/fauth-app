package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.ApplicationOAuthScope;

public class ApplicationOAuthScopeResponse {
  public ApplicationOAuthScope scope;
  
  @JacksonConstructor
  public ApplicationOAuthScopeResponse() {}
  
  public ApplicationOAuthScopeResponse(ApplicationOAuthScope paramApplicationOAuthScope) {
    this.scope = paramApplicationOAuthScope;
  }
}
