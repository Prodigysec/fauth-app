package io.fusionauth.api.security;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.primeframework.mvc.security.DefaultUserLoginConstraintValidator;
import org.primeframework.mvc.security.UserLoginSecurityContext;

public class TenantManagerConstraintValidator extends DefaultUserLoginConstraintValidator {
  @Inject(optional = true)
  public void setUserLoginSecurityContext(@Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext) {
    super.setUserLoginSecurityContext(paramUserLoginSecurityContext);
  }
}
