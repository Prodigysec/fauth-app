package io.fusionauth.app.action.admin.ipAcl;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.IPAccessControlList;
import io.fusionauth.domain.api.IPAccessControlListRequest;
import io.fusionauth.domain.api.IPAccessControlListResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;

@Action(value = "{ipAccessControlListId}", requiresAuthentication = true, constraints = {"admin", "acl_manager"})
@List({@Redirect(code = "missing", uri = "/admin/ip-acl/"), @Redirect(code = "success", uri = "/admin/ip-acl/")})
public class EditAction extends BaseFormAction {
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.ipAccessControlList = ((IPAccessControlListResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveIPAccessControlList(this.ipAccessControlListId))).ipAccessControlList;
    return "input";
  }
  
  public String post() {
    IPAccessControlList iPAccessControlList1 = ((IPAccessControlListResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveIPAccessControlList(this.ipAccessControlListId))).ipAccessControlList;
    IPAccessControlList iPAccessControlList2 = ((IPAccessControlListResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateIPAccessControlList(this.ipAccessControlListId, new IPAccessControlListRequest(this.ipAccessControlList)))).ipAccessControlList;
    writeAuditLogForUpdate("Updated ACL with Id [" + String.valueOf(this.ipAccessControlListId) + "] and name [" + iPAccessControlList2.name + "]", iPAccessControlList1, iPAccessControlList2);
    return "success";
  }
}
