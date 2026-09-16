package io.fusionauth.app.action.ajax.application;

import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationOAuthScope;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.api.ApplicationOAuthScopeResponse;
import io.fusionauth.domain.api.ApplicationResponse;
import java.util.UUID;

public class BaseApplicationAJAXAction extends BaseAJAXAction {
  public Application application;
  
  public UUID applicationId;
  
  public ApplicationRole role;
  
  public UUID roleId;
  
  public ApplicationOAuthScope scope;
  
  public UUID scopeId;
  
  protected BaseApplicationAJAXAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  protected void retrieveApplication() {
    this.application = ((ApplicationResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveApplication(this.applicationId))).application;
  }
  
  protected void retrieveRole() {
    this.role = this.application.roles.stream().filter(paramApplicationRole -> paramApplicationRole.id.equals(this.roleId)).findFirst().orElse(new ApplicationRole());
  }
  
  protected void retrieveScope() {
    this.scope = ((ApplicationOAuthScopeResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveOAuthScope(this.applicationId, this.scopeId))).scope;
  }
}
