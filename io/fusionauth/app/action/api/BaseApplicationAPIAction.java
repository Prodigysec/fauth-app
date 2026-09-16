package io.fusionauth.app.action.api;

import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Application;
import org.primeframework.mvc.ErrorException;

public abstract class BaseApplicationAPIAction extends BaseTenantAPIAction {
  protected BaseApplicationAPIAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
  }
  
  public void validateTenantScopedWritesForUniversalApplications(Application paramApplication) {
    if (paramApplication != null && paramApplication.universalConfiguration.universal && 
      
      tenantIdWasSpecified())
      throw new ErrorException("forbidden"); 
  }
}
