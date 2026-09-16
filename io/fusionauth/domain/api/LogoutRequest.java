package io.fusionauth.domain.api;

import io.fusionauth.domain.Buildable;

public class LogoutRequest extends BaseEventRequest implements Buildable<LogoutRequest> {
  public boolean global;
  
  public String refreshToken;
}
