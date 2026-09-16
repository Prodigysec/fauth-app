package io.fusionauth.domain.api.identity.verify;

import io.fusionauth.domain.Buildable;
import java.util.Map;

public class VerifyCompleteResponse implements Buildable<VerifyCompleteResponse> {
  public Map<String, Object> state;
}
