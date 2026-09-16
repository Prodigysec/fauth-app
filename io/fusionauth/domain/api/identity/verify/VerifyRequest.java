package io.fusionauth.domain.api.identity.verify;

import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.api.BaseEventRequest;

public class VerifyRequest extends BaseEventRequest implements Buildable<VerifyRequest> {
  public String loginId;
  
  public String loginIdType;
}
