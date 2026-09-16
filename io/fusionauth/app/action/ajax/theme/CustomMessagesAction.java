package io.fusionauth.app.action.ajax.theme;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.Pair;
import com.inversoft.util.StringTools;
import com.inversoft.validator.Validator;
import io.fusionauth.api.util.PropertiesTools;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.theme.SimpleThemeFrontendService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Theme;
import io.fusionauth.domain.api.ThemeResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "theme_manager"})
public class CustomMessagesAction extends BaseAJAXAction {
  private final SimpleThemeFrontendService simpleThemeService;
  
  public String customMessages;
  
  public String defaultOverrides;
  
  public boolean isDefault;
  
  public String overrideLines;
  
  public String themeDefaults;
  
  public boolean update;
  
  @Inject
  public CustomMessagesAction(FrontEndSupport paramFrontEndSupport, SimpleThemeFrontendService paramSimpleThemeFrontendService) {
    super(paramFrontEndSupport);
    this.simpleThemeService = paramSimpleThemeFrontendService;
  }
  
  public String get() {
    this.themeDefaults = ((ThemeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTheme(Theme.FUSIONAUTH_THEME_ID))).theme.defaultMessages;
    return "render";
  }
  
  public String post() throws IOException {
    if (this.update)
      return "success"; 
    this.themeDefaults = ((ThemeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTheme(Theme.FUSIONAUTH_THEME_ID))).theme.defaultMessages;
    if (!StringTools.isBlank(this.defaultOverrides)) {
      Pair<String, List<Integer>> pair = this.simpleThemeService.mergeMessages(this.themeDefaults, this.defaultOverrides);
      this.themeDefaults = (String)pair.first;
      this.overrideLines = this.frontEndSupport.objectMapper.writeValueAsString(pair.second);
    } 
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    Objects.requireNonNull(this.frontEndSupport);
    (new Validator()).ensure(PropertiesTools.validate(this.customMessages), "customMessages", "[invalid]", new Object[0]).done(this.frontEndSupport::transfer);
  }
}
