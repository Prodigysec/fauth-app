package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.UserComment;
import java.util.List;

public class UserCommentResponse {
  public UserComment userComment;
  
  public List<UserComment> userComments;
  
  @JacksonConstructor
  public UserCommentResponse() {}
  
  public UserCommentResponse(List<UserComment> paramList) {
    this.userComments = paramList;
  }
  
  public UserCommentResponse(UserComment paramUserComment) {
    this.userComment = paramUserComment;
  }
}
