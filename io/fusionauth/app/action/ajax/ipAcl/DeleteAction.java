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

@Action(value = "{ipAccessControlListId}", requiresAuthentication = true, constraints = {"admin", "acl_deleter"})
public class DeleteAction extends BaseAJAXAction {
  public UUID ipAccessControlListId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    IPAccessControlList iPAccessControlList = ((IPAccessControlListResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveIPAccessControlList(this.ipAccessControlListId))).ipAccessControlList;
    superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.deleteIPAccessControlList(this.ipAccessControlListId));
    writeAuditLog("Deleted the ACL with Id [" + String.valueOf(iPAccessControlList.id) + "] and name [" + iPAccessControlList.name + "]");
    return "success";
  }
}
