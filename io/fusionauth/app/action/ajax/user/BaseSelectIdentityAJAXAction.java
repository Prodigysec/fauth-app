package io.fusionauth.app.action.ajax.user;

import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.client.LambdaDelegate;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.api.UserResponse;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class BaseSelectIdentityAJAXAction extends BaseAJAXAction implements IdentitySelector {
  @FTLVariable
  public List<UserIdentity> identities;
  
  public String loginId;
  
  public UUID userId;
  
  protected UserIdentity identity;
  
  protected User user;
  
  protected BaseSelectIdentityAJAXAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  protected void loadUserPrepareDelegate() {
    Objects.requireNonNull(this.frontEndSupport);
    this.delegate = new LambdaDelegate(this.client, paramClientResponse -> paramClientResponse.successResponse, this.frontEndSupport::ajaxJSONErrorHandling);
    this.user = ((UserResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUser(this.userId))).user;
    Objects.requireNonNull(this.frontEndSupport);
    this.delegate = new LambdaDelegate(this.client.setTenantId(this.user.tenantId), paramClientResponse -> paramClientResponse.successResponse, this.frontEndSupport::ajaxJSONErrorHandling);
  }
}
