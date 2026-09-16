package io.fusionauth.app.action.api.tenant;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.api.PasswordValidationRulesResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;

@Action("{tenantId}")
public class PasswordValidationRulesAction extends BaseTenantAPIAction {
  @JSONResponse
  public PasswordValidationRulesResponse response;
  
  @PreParameter
  public UUID tenantId;
  
  @Inject
  public PasswordValidationRulesAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
  }
  
  public String get() {
    Tenant tenant = getTenant();
    this.response = new PasswordValidationRulesResponse(tenant.passwordValidationRules);
    this.response.passwordValidationRules.breachDetection = null;
    return "render";
  }
  
  protected UUID askActionForTenantId() {
    return this.tenantId;
  }
}
