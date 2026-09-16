package io.fusionauth.domain.jwks;

import java.net.URI;

public interface JSONWebKeyInfoProvider {
  URI issuer();
  
  URI jwksURI();
}
