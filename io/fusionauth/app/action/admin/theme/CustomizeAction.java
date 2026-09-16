package io.fusionauth.app.action.admin.theme;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.inversoft.error.Error;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.system.ThemeService;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.theme.variables.ColorVar;
import io.fusionauth.app.service.theme.variables.FontVar;
import io.fusionauth.app.service.theme.variables.NumericVar;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.SimpleThemeVariables;
import io.fusionauth.domain.Theme;
import io.fusionauth.domain.ThemeType;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.ApplicationSearchRequest;
import io.fusionauth.domain.api.ApplicationSearchResponse;
import io.fusionauth.domain.api.ThemeRequest;
import io.fusionauth.domain.api.ThemeResponse;
import io.fusionauth.http.server.HTTPContext;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.parameter.annotation.PreParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{themeId}", requiresAuthentication = true, constraints = {"admin", "theme_manager"})
@List({@Redirect(code = "success", uri = "/admin/theme/"), @Redirect(code = "missing", uri = "/admin/theme/")})
public class CustomizeAction extends BaseAction {
  @FTLVariable
  public static final UUID baseThemeId = Theme.FUSIONAUTH_SIMPLE_THEME_ID;
  
  @FTLVariable
  public static final ColorVar[] colorVars = ColorVar.values();
  
  @FTLVariable
  public static final FontVar[] fontVars = FontVar.values();
  
  @FTLVariable
  public static final NumericVar[] numericVars = NumericVar.values();
  
  @FTLVariable
  public final Map<String, Map<String, String>> templateCategoryMap = ThemeService.TemplateCategoryMapping;
  
  public Application application;
  
  public UUID applicationId;
  
  public HTTPContext context;
  
  public boolean existingTheme;
  
  public boolean isBaseTheme;
  
  public boolean isLicensed;
  
  public String method = "authenticator";
  
  public UUID sourceThemeId;
  
  public Theme theme = new Theme();
  
  public UUID themeId;
  
  public String variables;
  
  @Inject
  public CustomizeAction(FrontEndSupport paramFrontEndSupport, HTTPContext paramHTTPContext) {
    super(paramFrontEndSupport);
    this.context = paramHTTPContext;
    paramFrontEndSupport.errorMapperFunction = CustomizeAction::mapError;
    paramFrontEndSupport.fieldMapperFunction = CustomizeAction::mapField;
    this.isLicensed = (paramFrontEndSupport.reactorStatusService.retrieveStatus()).licensed;
  }
  
  private static Error mapError(Error paramError) {
    paramError.code = paramError.code.replace("theme.variables.", "");
    paramError.code = paramError.code.replace("favicons.href", "faviconImageURL");
    return paramError;
  }
  
  private static String mapField(String paramString) {
    if (paramString.startsWith("theme.variables.favicons"))
      return "theme.variables.faviconImageURL"; 
    return paramString;
  }
  
  @PreParameterMethod
  public void addXSSProtectionHeader() {
    this.frontEndSupport.response.setHeader("X-XSS-Protection", "0");
  }
  
  public String get() throws JsonProcessingException {
    this.existingTheme = (this.themeId != null);
    this.theme = ((ThemeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTheme(themeIdToFetch()))).theme;
    if (!this.existingTheme)
      this.theme.name += " - Copy"; 
    this.isBaseTheme = Theme.FUSIONAUTH_SIMPLE_THEME_ID.equals(this.themeId);
    this.variables = this.frontEndSupport.objectMapper.writeValueAsString(this.theme.variables);
    if (this.isBaseTheme)
      this.theme.defaultMessages = ((ThemeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTheme(Theme.FUSIONAUTH_THEME_ID))).theme.defaultMessages; 
    return "input";
  }
  
  public String post() {
    if (this.existingTheme) {
      Theme theme = ((ThemeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTheme(this.themeId))).theme;
      updateTheme();
      writeAuditLogForUpdate("Updated theme with Id [" + String.valueOf(this.themeId) + "] and name [" + this.theme.name + "]", theme, this.theme);
    } else {
      updateTheme();
      writeAuditLog("Created the theme with Id [" + String.valueOf(this.theme.id) + "] and name [" + this.theme.name + "]");
    } 
    return "success";
  }
  
  @PostParameterMethod
  public void setup() {
    if (this.applicationId == null) {
      ApplicationSearchRequest applicationSearchRequest = (new ApplicationSearchRequest()).with(paramApplicationSearchRequest -> paramApplicationSearchRequest.search.numberOfResults = 1);
      this.application = (Application)((ApplicationSearchResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.searchApplications(paramApplicationSearchRequest))).applications.getFirst();
      this.applicationId = this.application.id;
    } else {
      this.application = ((ApplicationResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveApplication(this.applicationId))).application;
    } 
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePOST() {
    try {
      this.theme.variables = (SimpleThemeVariables)this.frontEndSupport.objectMapper.readValue(this.variables, SimpleThemeVariables.class);
    } catch (IOException iOException) {
      this.frontEndSupport.addGeneralError("[InvalidVariables]", new Object[0]);
    } 
  }
  
  private UUID themeIdToFetch() {
    if (this.themeId != null)
      return this.themeId; 
    return (UUID)Objects.requireNonNullElse(this.sourceThemeId, Theme.FUSIONAUTH_SIMPLE_THEME_ID);
  }
  
  private void updateTheme() {
    this.theme.type = ThemeType.simple;
    this
      
      .theme = this.existingTheme ? ((ThemeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateTheme(this.themeId, new ThemeRequest(this.theme)))).theme : ((ThemeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.createTheme(this.themeId, new ThemeRequest(this.theme)))).theme;
  }
}
