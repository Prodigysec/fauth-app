package io.fusionauth.domain.provider;

import io.fusionauth.domain.util.Normalizer;
import java.util.Set;
import java.util.function.Supplier;

public interface DomainBasedIdentityProvider {
  Set<String> getDomains();
  
  default void normalizeDomains() {
    Normalizer.toLowerCase(getDomains(), java.util.HashSet::new);
  }
}
