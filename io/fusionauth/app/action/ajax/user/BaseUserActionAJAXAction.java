package io.fusionauth.app.action.ajax.user;

import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.util.ActionTools;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.ExpiryUnit;
import io.fusionauth.domain.UserActionLog;
import io.fusionauth.domain.api.user.ActionRequest;
import io.fusionauth.domain.api.user.ActionResponse;
import io.fusionauth.domain.event.UserActionEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public abstract class BaseUserActionAJAXAction extends BaseAJAXAction {
  @FTLVariable
  public static final ExpiryUnit[] expiryUnits = ExpiryUnit.values();
  
  public ActionRequest.ActionData action = new ActionRequest.ActionData();
  
  public UUID actionId;
  
  public Boolean expires;
  
  public ExpiryUnit expiryUnit;
  
  public Integer expiryValue;
  
  public UUID userId;
  
  protected BaseUserActionAJAXAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  protected ZonedDateTime calculateExpiry() {
    if (this.expires == null)
      return null; 
    if (!this.expires.booleanValue())
      return UserActionEvent.Infinite; 
    return ActionTools.getExpirationFrom(this.expiryValue.intValue(), this.expiryUnit);
  }
  
  protected UserActionLog findAction() {
    return ((ActionResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveAction(this.actionId))).action;
  }
}
