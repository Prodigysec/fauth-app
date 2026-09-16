package io.fusionauth.app.primeframework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.csrf.DefaultEncryptionBasedTokenCSRFProvider;

public class TenantManagerCSRFProvider extends DefaultEncryptionBasedTokenCSRFProvider {
  public static final String CSRF_TOKEN_HEADER = "X-FusionAuth-CSRF-Token";
  
  @Inject
  public TenantManagerCSRFProvider(Encryptor paramEncryptor, ObjectMapper paramObjectMapper, @Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext) {
    super(paramEncryptor, paramObjectMapper, paramUserLoginSecurityContext);
    setNonceTimeout(43200000L);
  }
  
  public String getHeaderName() {
    return "X-FusionAuth-CSRF-Token";
  }
  
  public String getParameterName() {
    return "csrfToken";
  }
}
