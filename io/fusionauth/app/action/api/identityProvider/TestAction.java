package io.fusionauth.app.action.api.identityProvider;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.identity.IdentityProviderService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.identityProvider.IdentityProviderConnectionTestRequest;
import io.fusionauth.domain.api.identityProvider.IdentityProviderConnectionTestResponse;
import io.fusionauth.domain.provider.IdentityProviderConnectionTestResult;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api"})
public class TestAction extends BaseTenantAPIAction {
  @JSONRequest
  public final IdentityProviderConnectionTestRequest request = new IdentityProviderConnectionTestRequest();
  
  private final IdentityProviderService identityProviderService;
  
  public String connectionTestId;
  
  @JSONResponse
  public IdentityProviderConnectionTestResponse response;
  
  private IdentityProviderService.ValidationResult result;
  
  @Inject
  public TestAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, IdentityProviderService paramIdentityProviderService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.identityProviderService = paramIdentityProviderService;
  }
  
  public String get() {
    if (this.result.connectionTestId == null)
      return "missing"; 
    IdentityProviderConnectionTestResult identityProviderConnectionTestResult = this.identityProviderService.retrieveConnectionTestResult(this.result.connectionTestId);
    this.response = new IdentityProviderConnectionTestResponse();
    this.response.result = identityProviderConnectionTestResult;
    return "render";
  }
  
  public String post() {
    String str = this.identityProviderService.startConnectionTest(this.result.tenant, this.request);
    this.response = new IdentityProviderConnectionTestResponse();
    this.response.connectionTestId = str;
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    if (this.connectionTestId == null) {
      this.frontEndSupport.addFieldError("connectionTestId", "[missing]connectionTestId", new Object[0]);
      return;
    } 
    this.result = this.identityProviderService.validateRetrieveConnectionTestResult(getOptionalTenant(), this.connectionTestId);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod
  public void validatePost() {
    if (tenantIdWasSpecified())
      this.request.tenantId = getOptionalTenantId(); 
    this.result = this.identityProviderService.validateStartConnectionTest(getOptionalTenant(), this.request);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
