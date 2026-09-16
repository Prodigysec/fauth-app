package io.fusionauth.app.action.ajax.ipAcl;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.IPAccessControlList;
import io.fusionauth.domain.api.IPAccessControlListResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{ipAccessControlListId}", requiresAuthentication = true, constraints = {"admin", "acl_manager"})
public class ViewAction extends BaseAJAXAction {
  public IPAccessControlList ipAccessControlList;
  
  public UUID ipAccessControlListId;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.ipAccessControlList = ((IPAccessControlListResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveIPAccessControlList(this.ipAccessControlListId))).ipAccessControlList;
    return "render";
  }
}
