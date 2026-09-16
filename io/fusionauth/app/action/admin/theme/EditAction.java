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

@Action(value = "{themeId}", requiresAuthentication = true, constraints = {"admin", "theme_manager"})
@List({@Redirect(code = "success", uri = "/admin/theme/"), @Redirect(code = "missing", uri = "/admin/theme/")})
public class EditAction extends BaseFormAction {
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport, Cache<UUID, CachedTheme> paramCache) {
    super(paramFrontEndSupport, paramCache);
  }
  
  public String get() {
    this.theme = ((ThemeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTheme(this.themeId))).theme;
    return "input";
  }
  
  public String post() {
    Theme theme1 = ((ThemeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTheme(this.themeId))).theme;
    this.theme.data.clear();
    this.theme.data.putAll(theme1.data);
    Theme theme2 = ((ThemeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateTheme(this.themeId, new ThemeRequest(this.theme)))).theme;
    writeAuditLogForUpdate("Updated theme with Id [" + String.valueOf(this.themeId) + "] and name [" + theme2.name + "]", theme1, theme2);
    return "success";
  }
}
