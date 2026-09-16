package io.fusionauth.domain.api.identity.verify;

import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.api.BaseEventRequest;

public class VerifyCompleteRequest extends BaseEventRequest implements Buildable<VerifyCompleteRequest> {
  public String oneTimeCode;
  
  public String verificationId;
}
