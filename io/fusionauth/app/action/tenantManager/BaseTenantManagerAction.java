package io.fusionauth.app.action.tenantManager;

import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.CachedTheme;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.util.ActionTools;
import io.fusionauth.api.util.PropertiesTools;
import io.fusionauth.app.action.BaseThemedAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.client.LambdaDelegate;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.AuditLog;
import io.fusionauth.domain.SystemConfiguration;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Theme;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.SystemConfigurationResponse;
import io.fusionauth.domain.api.TenantResponse;
import io.fusionauth.domain.api.ThemeResponse;
import io.fusionauth.domain.api.tenantManager.TenantManagerConfigurationResponse;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.tenantManager.TenantManagerConfiguration;
import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.Forward.List;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.action.result.annotation.JSON.List;
import org.primeframework.mvc.action.result.annotation.SaveRequest;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.action.result.annotation.Status.List;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.csrf.CSRFProvider;
import org.primeframework.mvc.util.QueryStringBuilder;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.annotation.PreValidationMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@List({@Status(code = "unauthorized", status = 403), @Status(code = "error", status = 400), @Status})
@List({@JSON(code = "render", status = 200, cacheControl = "no-store")})
@List({@Forward(code = "missing-tenant", page = "/tenant-manager/error.ftl"), @Forward(code = "invalid-license", page = "/tenant-manager/invalid-license.ftl"), @Forward(code = "invalid-referer-header", page = "/tenant-manager/upgrade-request.ftl", status = 202, cacheControl = "no-store")})
@SaveRequest(uri = "/tenant-manager/login?tenantId=${tenantId}")
public abstract class BaseTenantManagerAction {
  public static final String TMAuditLogReason = "Tenant Manager User Interface";
  
  private static final Logger logger = LoggerFactory.getLogger(BaseTenantManagerAction.class);
  
  public final UUID clientId;
  
  public final CSRFProvider csrfProvider;
  
  public final ReactorStatusService reactorStatusService;
  
  public final UserLoginSecurityContext securityContext;
  
  protected final FusionAuthClientProvider clientProvider;
  
  public FusionAuthClient client = null;
  
  @FTLVariable
  public String csrfToken;
  
  public User currentUser;
  
  public LambdaDelegate delegate;
  
  public FrontEndSupport frontEndSupport;
  
  @FTLVariable
  public boolean isAdmin;
  
  public Locale locale;
  
  public String logoURL;
  
  public String redirectToLogin;
  
  public FusionAuthClient superClient = null;
  
  public SystemConfiguration systemConfiguration;
  
  @FTLVariable
  public Tenant tenant;
  
  public UUID tenantId;
  
  public TenantManagerConfiguration tenantManagerConfiguration;
  
  public Theme theme;
  
  @FTLVariable
  public List<String> userRoles = new ArrayList<>();
  
  public ZoneId zoneId;
  
  protected User codeUser;
  
  public BaseTenantManagerAction(UserLoginSecurityContext paramUserLoginSecurityContext, FrontEndSupport paramFrontEndSupport, FusionAuthClientProvider paramFusionAuthClientProvider, ReactorStatusService paramReactorStatusService, CSRFProvider paramCSRFProvider, UUID paramUUID) {
    this.clientId = paramUUID;
    this.securityContext = paramUserLoginSecurityContext;
    this.clientProvider = paramFusionAuthClientProvider;
    this.csrfProvider = paramCSRFProvider;
    this.frontEndSupport = paramFrontEndSupport;
    this.reactorStatusService = paramReactorStatusService;
  }
  
  @PreValidationMethod
  public void collectUserInfo() {
    this.codeUser = (User)this.securityContext.getCurrentUser();
    if (this.codeUser != null)
      this.currentUser = (new User(this.codeUser)).secure(); 
    this.zoneId = this.frontEndSupport.resolveZoneId(this.codeUser, Application.FUSIONAUTH_APP_ID);
    this.userRoles.addAll(this.securityContext.getCurrentUsersRoles());
    this.isAdmin = this.userRoles.contains("admin");
    this.csrfToken = this.csrfProvider.getToken(this.frontEndSupport.request);
  }
  
  @FTLVariable
  public String getFusionAuthHost() {
    return this.frontEndSupport.getFusionAuthBaseURL();
  }
  
  @PostParameterMethod
  public void setup() throws IOException {
    if (this.tenantId == null) {
      this.tenantId = ActionTools.resolveTenantIdFromHeader(this.frontEndSupport.request).orElse(null);
      if (this.tenantId == null)
        return; 
    } 
    this

      
      .redirectToLogin = QueryStringBuilder.builder("/tenant-manager/login").with("tenantId", this.tenantId).build();
    this.client = this.clientProvider.get(this.tenantId);
    this.superClient = this.clientProvider.get();
    Objects.requireNonNull(this.frontEndSupport);
    this.delegate = new LambdaDelegate(this.client, paramClientResponse -> paramClientResponse.successResponse, this.frontEndSupport::frontEndErrorHandling);
    fetchTenants();
    fetchSystemConfiguration();
    fetchTenantManagerConfiguration();
    setLogoURL();
    this.frontEndSupport.response.setHeader("Content-Security-Policy", "default-src 'none'; script-src 'self'; connect-src 'self'; img-src 'self' https://www.gravatar.com " + this.logoURL + "; style-src 'self'; base-uri 'self'; form-action 'self'; manifest-src 'self';");
    setupTheme();
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST", "PUT", "DELETE"})
  public void validation() {
    if (this.tenantId == null)
      throw new ErrorException("missing-tenant"); 
    if (this.tenant == null)
      throw new ErrorException("missing-tenant"); 
    if (!(this.reactorStatusService.retrieveStatus()).tenantManagerApplication.equals(ReactorFeatureStatus.ACTIVE))
      throw new ErrorException("invalid-license"); 
  }
  
  protected LambdaDelegate superDelegate() {
    Objects.requireNonNull(this.frontEndSupport);
    return new LambdaDelegate(this.superClient, paramClientResponse -> paramClientResponse.successResponse, this.frontEndSupport::frontEndErrorHandling);
  }
  
  protected void writeAuditLog(String paramString) {
    writeAuditLogForUpdate(paramString, null, null);
  }
  
  protected void writeAuditLogForUpdate(String paramString, Object paramObject1, Object paramObject2) {
    this.frontEndSupport.writeAuditLog(this.client, (new AuditLog(this.codeUser.getLogin(), paramString)).with(paramAuditLog -> paramAuditLog.oldValue = paramObject)
        .with(paramAuditLog -> paramAuditLog.newValue = paramObject)
        .with(paramAuditLog -> paramAuditLog.tenantId = this.tenantId)
        .with(paramAuditLog -> paramAuditLog.reason = "Tenant Manager User Interface"));
  }
  
  private void fetchSystemConfiguration() {
    if (this.superClient != null) {
      ClientResponse<SystemConfigurationResponse, Void> clientResponse = this.superClient.retrieveSystemConfiguration();
      if (clientResponse.wasSuccessful())
        this.systemConfiguration = ((SystemConfigurationResponse)clientResponse.successResponse).systemConfiguration; 
    } 
  }
  
  private void fetchTenantManagerConfiguration() {
    this.tenantManagerConfiguration = ((TenantManagerConfigurationResponse)superDelegate().execute(FusionAuthClient::retrieveTenantManagerConfiguration)).tenantManagerConfiguration;
  }
  
  private void fetchTenants() {
    ClientResponse<TenantResponse, Errors> clientResponse = this.client.retrieveTenant(this.tenantId);
    if (clientResponse.wasSuccessful())
      this.tenant = ((TenantResponse)clientResponse.successResponse).tenant.secure(); 
  }
  
  private void setLogoURL() {
    if (this.systemConfiguration != null && this.systemConfiguration.uiConfiguration != null && this.systemConfiguration.uiConfiguration.logoURL != null) {
      this.logoURL = this.systemConfiguration.uiConfiguration.logoURL;
    } else {
      this.logoURL = "";
    } 
  }
  
  private void setupTheme() throws IOException {
    ClientResponse<ApplicationResponse, Void> clientResponse = this.client.retrieveApplication(this.clientId);
    if (clientResponse.wasSuccessful()) {
      Application application = ((ApplicationResponse)(this.client.retrieveApplication(this.clientId)).successResponse).application;
      UUID uUID = application.themeId;
      if (uUID == null && this.tenant != null)
        uUID = this.tenant.themeId; 
      if (uUID == null)
        uUID = Theme.FUSIONAUTH_THEME_ID; 
      Theme theme = ((ThemeResponse)this.superClient.retrieveTheme(uUID).getSuccessResponse()).theme;
      if (this.locale == null)
        this.locale = (Locale)this.frontEndSupport.localeProvider.get(); 
      String str = ((ThemeResponse)this.superClient.retrieveTheme(Theme.FUSIONAUTH_THEME_ID).getSuccessResponse()).theme.defaultMessages;
      this.theme = new BaseThemedAction.LocaleResolvedCachedTheme(this.locale, new CachedTheme(theme), PropertiesTools.loadProperties(str), this.frontEndSupport.messageProvider);
    } else {
      logger.error("Unable to retrieve the Tenant manager application with id [{}] to determine the theme. Response was: [{}].", this.clientId, this.frontEndSupport.objectMapper.writeValueAsString(clientResponse.errorResponse));
      throw new ErrorException("error");
    } 
  }
}
