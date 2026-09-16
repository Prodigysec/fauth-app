package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.primeframework.mvc.security.DefaultJWTSecurityContext;
import org.primeframework.mvc.security.JWTRequestAdapter;
import org.primeframework.mvc.security.VerifierProvider;

public class AccountSideLoadJWTSecurityContext extends DefaultJWTSecurityContext {
  @Inject
  public AccountSideLoadJWTSecurityContext(@Named("AccountSideLoadJWTAdapter") JWTRequestAdapter paramJWTRequestAdapter, VerifierProvider paramVerifierProvider) {
    super(paramJWTRequestAdapter, paramVerifierProvider);
  }
}
