package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import io.fusionauth.api.security.KeyCache;
import io.fusionauth.jwt.Verifier;
import java.util.Map;
import org.primeframework.mvc.security.VerifierProvider;

public class FusionAuthVerifierProvider implements VerifierProvider {
  private final KeyCache keyCache;
  
  @Inject
  public FusionAuthVerifierProvider(KeyCache paramKeyCache) {
    this.keyCache = paramKeyCache;
  }
  
  public Map<String, Verifier> get() {
    return this.keyCache.getVerifiers();
  }
}
