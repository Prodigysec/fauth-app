package io.fusionauth.domain.api;

import io.fusionauth.domain.Buildable;
import java.util.UUID;

public class LoginPingRequest extends BaseLoginRequest implements Buildable<LoginPingRequest> {
  public UUID userId;
}
