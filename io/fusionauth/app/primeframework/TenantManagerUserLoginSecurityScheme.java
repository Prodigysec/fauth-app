package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.http.HTTPMethod;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.security.UserLoginConstraintsValidator;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.csrf.CSRFProvider;

public class TenantManagerUserLoginSecurityScheme extends FusionAuthUserLoginSecurityScheme {
  @Inject
  public TenantManagerUserLoginSecurityScheme(FusionAuthConfiguration paramFusionAuthConfiguration, MVCConfiguration paramMVCConfiguration, @Named("TenantManagerConstraintValidator") UserLoginConstraintsValidator paramUserLoginConstraintsValidator, @Named("TenantManagerCSRFProvider") CSRFProvider paramCSRFProvider, HTTPRequest paramHTTPRequest, HTTPMethod paramHTTPMethod) {
    super(paramFusionAuthConfiguration, paramMVCConfiguration, paramUserLoginConstraintsValidator, paramCSRFProvider, paramHTTPRequest, paramHTTPMethod);
    this.invalidRefererPath = "/tenant-manager/ajax/";
    this.validRefererPath = "/tenant-manager";
  }
  
  @Inject
  public void setUserLoginSecurityContext(@Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext) {
    super.setUserLoginSecurityContext(paramUserLoginSecurityContext);
  }
}
