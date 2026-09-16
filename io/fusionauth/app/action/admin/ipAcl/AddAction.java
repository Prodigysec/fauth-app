package io.fusionauth.app.action.admin.ipAcl;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.IPAccessControlEntry;
import io.fusionauth.domain.IPAccessControlEntryAction;
import io.fusionauth.domain.IPAccessControlList;
import io.fusionauth.domain.api.IPAccessControlListRequest;
import io.fusionauth.domain.api.IPAccessControlListResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;

@Action(value = "{ipAccessControlListId}", requiresAuthentication = true, constraints = {"admin", "acl_manager"})
@Redirect(code = "success", uri = "/admin/ip-acl/")
public class AddAction extends BaseFormAction {
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    if (this.ipAccessControlListId != null) {
      this.ipAccessControlList = ((IPAccessControlListResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveIPAccessControlList(this.ipAccessControlListId))).ipAccessControlList;
      this.ipAccessControlList.id = null;
      this.ipAccessControlListId = null;
      this.ipAccessControlList.name += " - copy";
    } 
    if (this.ipAccessControlList.entries.isEmpty()) {
      this.ipAccessControlList.entries.add((new IPAccessControlEntry()).with(paramIPAccessControlEntry -> paramIPAccessControlEntry.action = IPAccessControlEntryAction.Block)
          .with(paramIPAccessControlEntry -> paramIPAccessControlEntry.startIPAddress = "*"));
      this.ipAccessControlList.entries.add((new IPAccessControlEntry()).with(paramIPAccessControlEntry -> paramIPAccessControlEntry.action = IPAccessControlEntryAction.Allow));
    } 
    return "input";
  }
  
  public String post() {
    IPAccessControlList iPAccessControlList = ((IPAccessControlListResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.createIPAccessControlList(this.ipAccessControlListId, new IPAccessControlListRequest(this.ipAccessControlList)))).ipAccessControlList;
    writeAuditLog("Created ACL with Id [" + String.valueOf(iPAccessControlList.id) + "] and name [" + iPAccessControlList.name + "]");
    return "success";
  }
}
