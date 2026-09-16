package io.fusionauth.app.action.ajax.theme;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{themeId}", requiresAuthentication = true, constraints = {"admin", "system_manager"})
public class DeleteAction extends BaseAJAXAction {
  public UUID themeId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.deleteTheme(this.themeId));
    writeAuditLog("Deleted the theme with Id [" + String.valueOf(this.themeId) + "]");
    return "success";
  }
}
