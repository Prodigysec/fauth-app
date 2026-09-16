package io.fusionauth.app.action.ajax.user;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.validator.Validator;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.UserComment;
import io.fusionauth.domain.api.UserCommentRequest;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{userId}", requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager"})
public class CommentAction extends BaseAJAXAction {
  public UserComment userComment = new UserComment();
  
  public UUID userId;
  
  @Inject
  public CommentAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    this.userComment.userId = this.userId;
    this.userComment.commenterId = this.codeCurrentUser.id;
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.commentOnUser(new UserCommentRequest(this.userComment)));
    writeAuditLog("Commented on user with Id [" + String.valueOf(this.userId) + "] with text [" + this.userComment.comment + "]");
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    (new Validator())
      .notBlank(this.userComment.comment, "userComment.comment", new Object[0]);
  }
}
