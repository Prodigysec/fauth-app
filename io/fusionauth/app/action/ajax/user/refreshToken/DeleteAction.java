package io.fusionauth.app.action.ajax.user.refreshToken;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.jwt.RefreshTokenResponse;
import io.fusionauth.domain.api.jwt.RefreshTokenRevokeRequest;
import io.fusionauth.domain.jwt.RefreshToken;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{tokenId}", requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager"})
public class DeleteAction extends BaseAJAXAction {
  public UUID tokenId;
  
  public UUID userId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    if (this.tokenId != null) {
      RefreshToken refreshToken = ((RefreshTokenResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveRefreshTokenById(this.tokenId))).refreshToken;
      this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.revokeRefreshTokenById(this.tokenId));
      writeAuditLog("Revoked refresh token [" + refreshToken.token + "] with Id [" + String.valueOf(this.tokenId) + "]");
    } else {
      this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.revokeRefreshTokensWithRequest(new RefreshTokenRevokeRequest(this.frontEndSupport.buildEventInfo(null), null, null, this.userId)));
      writeAuditLog("Revoked all tokens for user with user Id [" + String.valueOf(this.userId) + "]");
    } 
    return "success";
  }
}
