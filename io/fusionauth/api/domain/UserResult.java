package io.fusionauth.api.domain;

import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.User;

public class UserResult implements Buildable<UserResult> {
  public ExternalIdentifier externalIdentifier;
  
  public User user;
  
  public UserResult() {}
  
  public UserResult(User paramUser) {
    this.user = paramUser;
  }
  
  public UserResult(ExternalIdentifier paramExternalIdentifier, User paramUser) {
    this.externalIdentifier = paramExternalIdentifier;
    this.user = paramUser;
  }
}
