package io.fusionauth.app.primeframework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.csrf.DefaultEncryptionBasedTokenCSRFProvider;

public class FusionAuthCSRFProvider extends DefaultEncryptionBasedTokenCSRFProvider {
  @Inject
  public FusionAuthCSRFProvider(Encryptor paramEncryptor, ObjectMapper paramObjectMapper, UserLoginSecurityContext paramUserLoginSecurityContext) {
    super(paramEncryptor, paramObjectMapper, paramUserLoginSecurityContext);
    setNonceTimeout(43200000L);
  }
  
  public String getParameterName() {
    return "csrfToken";
  }
  
  public boolean validateRequest(HTTPRequest paramHTTPRequest) {
    return (paramHTTPRequest.getPath().startsWith("/account") || super.validateRequest(paramHTTPRequest));
  }
}
