package io.fusionauth.app.action.api.user;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.error.Errors;
import io.fusionauth.api.service.comment.UserCommentService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.UserCommentRequest;
import io.fusionauth.domain.api.UserCommentResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{userId}", requiresAuthentication = true, scheme = {"api"})
public class CommentAction extends BaseTenantAPIAction {
  private final UserCommentService userCommentService;
  
  private final UserReaderService userReader;
  
  @JSONRequest
  public UserCommentRequest request;
  
  @JSONResponse
  public UserCommentResponse response;
  
  public UUID userId;
  
  @Inject
  public CommentAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, UserCommentService paramUserCommentService, UserReaderService paramUserReaderService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.userReader = paramUserReaderService;
    this.userCommentService = paramUserCommentService;
  }
  
  public String get() {
    if (this.userReader.retrieveById(getOptionalTenantId(), this.userId) == null)
      return "missing"; 
    this.response = new UserCommentResponse(this.userCommentService.retrieveAllForUser(this.userId));
    return "render";
  }
  
  public String post() {
    this.response = new UserCommentResponse(this.userCommentService.createComment(this.request.userComment));
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validate() {
    if (this.request == null || this.request.userComment == null) {
      this.frontEndSupport.addFieldError("userComment", "[missing]userComment", new Object[0]);
      return;
    } 
    this.request.userComment.normalize();
    Errors errors = this.userCommentService.validate(getOptionalTenantId(), this.request.userComment);
    this.frontEndSupport.transfer(errors);
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    if (this.userId == null)
      this.frontEndSupport.addFieldError("userId", "[missing]userId", new Object[0]); 
  }
}
