package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.UserComment;

public class UserCommentRequest {
  public UserComment userComment;
  
  @JacksonConstructor
  public UserCommentRequest() {}
  
  public UserCommentRequest(UserComment paramUserComment) {
    this.userComment = paramUserComment;
  }
}
