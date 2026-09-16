package io.fusionauth.app.action.internal;

import com.google.inject.Inject;
import io.fusionauth.api.admin.JWTManager;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.primeframework.UndocumentedAPI;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.event.EventRequest;
import io.fusionauth.domain.event.EventType;
import io.fusionauth.domain.event.JWTRefreshTokenRevokeEvent;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;

@Action(requiresAuthentication = true, scheme = {"api-internal"})
@UndocumentedAPI
public class WebhookAction extends BaseAPIAction {
  @JSONRequest
  public final EventRequest request = new EventRequest();
  
  @Inject
  public WebhookAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String post() {
    if (this.request.event.getType() != EventType.JWTRefreshTokenRevoke)
      return "success"; 
    JWTRefreshTokenRevokeEvent jWTRefreshTokenRevokeEvent = (JWTRefreshTokenRevokeEvent)this.request.event;
    if (jWTRefreshTokenRevokeEvent.refreshToken != null) {
      JWTManager.revokeByRefreshToken(jWTRefreshTokenRevokeEvent.refreshToken.id, ((Integer)jWTRefreshTokenRevokeEvent.applicationTimeToLiveInSeconds.get(jWTRefreshTokenRevokeEvent.refreshToken.applicationId)).intValue());
    } else if (jWTRefreshTokenRevokeEvent.userId != null) {
      JWTManager.revokedByUser(jWTRefreshTokenRevokeEvent.userId, jWTRefreshTokenRevokeEvent.applicationTimeToLiveInSeconds);
    } else if (jWTRefreshTokenRevokeEvent.applicationId != null) {
      for (UUID uUID : jWTRefreshTokenRevokeEvent.applicationIds())
        JWTManager.revokeByApplication(uUID, ((Integer)jWTRefreshTokenRevokeEvent.applicationTimeToLiveInSeconds.get(uUID)).intValue()); 
    } 
    return "success";
  }
}
