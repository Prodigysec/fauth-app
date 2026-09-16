package io.fusionauth.app.action.admin.theme;

import com.google.inject.Inject;
import com.inversoft.cache.Cache;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.CachedTheme;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Theme;
import io.fusionauth.domain.api.ThemeRequest;
import io.fusionauth.domain.api.ThemeResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;

@Action(requiresAuthentication = true, constraints = {"admin", "theme_manager"})
@List({@Redirect(code = "success", uri = "/admin/theme/"), @Redirect(code = "api-error", uri = "/admin/theme/")})
public class AddAction extends BaseFormAction {
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport, Cache<UUID, CachedTheme> paramCache) {
    super(paramFrontEndSupport, paramCache);
  }
  
  public String get() {
    this.theme = ((ThemeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTheme((this.themeId == null) ? Theme.FUSIONAUTH_THEME_ID : this.themeId))).theme;
    if (this.theme == null) {
      this.frontEndSupport.addGeneralError("[NotFoundException]", new Object[0]);
      return "success";
    } 
    if (this.themeId == null) {
      this.theme.name = null;
    } else {
      this.theme.name += " - copy";
    } 
    this.themeId = null;
    this.theme.id = null;
    return "input";
  }
  
  public String post() {
    Theme theme = ((ThemeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.createTheme(this.themeId, new ThemeRequest(this.theme)))).theme;
    writeAuditLog("Created the theme with Id [" + String.valueOf(theme.id) + "] and name [" + theme.name + "]");
    return "success";
  }
}
