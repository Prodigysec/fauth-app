package io.fusionauth.app.action;

import com.inversoft.rest.ClientResponse;
import com.inversoft.util.StringTools;
import io.fusionauth.api.domain.InstallationType;
import io.fusionauth.api.domain.Instance;
import io.fusionauth.api.domain.RuntimeMode;
import io.fusionauth.api.domain.SearchEngineType;
import io.fusionauth.api.util.LocaleTools;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.client.LambdaDelegate;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.AuditLog;
import io.fusionauth.domain.Location;
import io.fusionauth.domain.SystemConfiguration;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.ReactorResponse;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.Forward.List;
import org.primeframework.mvc.action.result.annotation.SaveRequest;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.action.result.annotation.Status.List;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.parameter.annotation.PreRenderMethod;
import org.primeframework.mvc.scope.annotation.Context;

@List({@Forward(code = "content-too-large", status = 413, cacheControl = "no-store"), @Forward(code = "error", page = "/errors/500.ftl", status = 500, cacheControl = "no-store"), @Forward(code = "input", cacheControl = "no-store"), @Forward(code = "success", cacheControl = "no-store"), @Forward(code = "unauthorized", page = "/admin/unauthorized.ftl", status = 401, cacheControl = "no-store"), @Forward(code = "invalid-referer-header", page = "/admin/upgrade-request.ftl", status = 202, cacheControl = "no-store")})
@List({@Status(code = "not-allowed", status = 405), @Status(code = "not-implemented", status = 501)})
@SaveRequest(uri = "/admin/login")
public abstract class BaseAction {
  public static final String UIAuditLogReason = "FusionAuth User Interface";
  
  @FTLVariable
  public static final UUID fusionAuthId = Application.FUSIONAUTH_APP_ID;
  
  @FTLVariable
  public static final List<Locale> locales;
  
  @FTLVariable
  public static final SortedSet<String> timezones = new TreeSet<>();
  
  @FTLVariable
  public final User ftlCurrentUser;
  
  public final SystemConfiguration systemConfiguration;
  
  protected final Tenant codeCurrentTenant;
  
  protected final User codeCurrentUser;
  
  protected final FrontEndSupport frontEndSupport;
  
  @FTLVariable
  public String currentBaseURL;
  
  @FTLVariable
  public String currentIPAddress;
  
  @FTLVariable
  public Location currentLocation;
  
  @FTLVariable
  public int defaultMaxHitCount;
  
  @FTLVariable
  public Tenant ftlCurrentTenant;
  
  @FTLVariable
  public Instance instance;
  
  @FTLVariable
  public InstallationType installationType;
  
  @Context
  public Future<?> internalReset;
  
  public Locale locale;
  
  @FTLVariable
  public ReactorStatus reactorStatus;
  
  @FTLVariable
  public RuntimeMode runtimeMode;
  
  @FTLVariable
  public SearchEngineType searchEngineType;
  
  @FTLVariable
  public Boolean showAnnouncement;
  
  @Deprecated
  public UUID tenantId;
  
  public Map<UUID, Tenant> tenants;
  
  public ZoneId zoneId;
  
  protected FusionAuthClient client;
  
  protected LambdaDelegate delegate;
  
  protected FusionAuthClient superClient;
  
  protected BaseAction(FrontEndSupport paramFrontEndSupport) {
    this.frontEndSupport = paramFrontEndSupport;
    this.instance = paramFrontEndSupport.getInstance();
    this.codeCurrentUser = (User)paramFrontEndSupport.userLoginSecurityContext.getCurrentUser();
    this.ftlCurrentUser = (this.codeCurrentUser != null) ? (new User(this.codeCurrentUser)).secure() : null;
    this.installationType = paramFrontEndSupport.configuration.installationType();
    this.runtimeMode = paramFrontEndSupport.configuration.runtimeMode();
    this.systemConfiguration = paramFrontEndSupport.systemConfiguration;
    this.zoneId = paramFrontEndSupport.resolveZoneId(this.codeCurrentUser, Application.FUSIONAUTH_APP_ID);
    this.defaultMaxHitCount = paramFrontEndSupport.configuration.searchEngineDefaultMaxHitCount();
    this.searchEngineType = paramFrontEndSupport.configuration.searchEngineType();
    this.tenants = (Map<UUID, Tenant>)paramFrontEndSupport.tenantCache.getAll().stream().collect(Collectors.toMap(paramTenant -> paramTenant.id, paramTenant -> paramTenant));
    this.codeCurrentTenant = this.tenants.get(paramFrontEndSupport.fusionAuthTenantId);
    this.ftlCurrentTenant = (new Tenant(this.codeCurrentTenant)).secure();
  }
  
  @PostParameterMethod
  public void basePostParameterMethod() {
    this.internalReset = this.frontEndSupport.updateFutureMessaging(this.internalReset, "reset-in-progress");
    this.client = this.frontEndSupport.fusionAuthClientProvider.get(this.tenantId);
    this.superClient = this.frontEndSupport.fusionAuthClientProvider.get();
    Objects.requireNonNull(this.frontEndSupport);
    this.delegate = new LambdaDelegate(this.client, paramClientResponse -> paramClientResponse.successResponse, this.frontEndSupport::frontEndErrorHandling);
    this.locale = (Locale)this.frontEndSupport.localeProvider.get();
    this.currentBaseURL = this.frontEndSupport.getFusionAuthBaseURL();
    this.currentIPAddress = this.frontEndSupport.getTrustedClientIPAddress();
    this.reactorStatus = ((ReactorResponse)superDelegate().execute(FusionAuthClient::retrieveReactorStatus)).status;
    this.showAnnouncement = Boolean.valueOf(showAnnouncement());
  }
  
  @FTLVariable
  public String getCsrfToken() {
    return this.frontEndSupport.csrfProvider.getToken(this.frontEndSupport.request);
  }
  
  @FTLVariable
  public UUID getCurrentUserId() {
    return (this.codeCurrentUser != null) ? this.codeCurrentUser.id : null;
  }
  
  @FTLVariable
  public UUID getCurrentUserTenantId() {
    return (this.codeCurrentUser != null) ? this.codeCurrentUser.tenantId : null;
  }
  
  @FTLVariable
  public String getCustomHeaderColor() {
    if (this.systemConfiguration.uiConfiguration.headerColor == null)
      return null; 
    if (this.systemConfiguration.uiConfiguration.headerColor.startsWith("#"))
      return this.systemConfiguration.uiConfiguration.headerColor; 
    return "#" + this.systemConfiguration.uiConfiguration.headerColor;
  }
  
  @FTLVariable
  public String getCustomMenuFontColor() {
    if (this.systemConfiguration.uiConfiguration.menuFontColor == null)
      return null; 
    if (this.systemConfiguration.uiConfiguration.menuFontColor.startsWith("#"))
      return this.systemConfiguration.uiConfiguration.menuFontColor; 
    return "#" + this.systemConfiguration.uiConfiguration.menuFontColor;
  }
  
  @FTLVariable
  public String getFusionAuthHost() {
    return this.frontEndSupport.getFusionAuthBaseURL();
  }
  
  @PreRenderMethod
  public void preRenderSetup() {
    if (this.locale == null)
      this.locale = (Locale)this.frontEndSupport.localeProvider.get(); 
    if (this.reactorStatus == null)
      this.reactorStatus = new ReactorStatus(); 
  }
  
  protected boolean doesNotHaveRole(String... paramVarArgs) {
    SortedSet<String> sortedSet = (this.codeCurrentUser.getRegistrationForApplication(Application.FUSIONAUTH_APP_ID)).roles;
    for (String str : paramVarArgs) {
      if (sortedSet.contains(str))
        return false; 
    } 
    return true;
  }
  
  protected boolean hasRole(String... paramVarArgs) {
    SortedSet<String> sortedSet = (this.codeCurrentUser.getRegistrationForApplication(Application.FUSIONAUTH_APP_ID)).roles;
    for (String str : paramVarArgs) {
      if (sortedSet.contains(str))
        return true; 
    } 
    return false;
  }
  
  protected UUID parseUUID(String paramString) {
    return (paramString == null) ? null : StringTools.parseUUID(paramString.trim());
  }
  
  protected boolean showAnnouncement() {
    return false;
  }
  
  protected LambdaDelegate superDelegate() {
    Objects.requireNonNull(this.frontEndSupport);
    return new LambdaDelegate(this.superClient, paramClientResponse -> paramClientResponse.successResponse, this.frontEndSupport::frontEndErrorHandling);
  }
  
  protected void writeAuditLog(String paramString) {
    writeAuditLogForUpdate(paramString, null, null);
  }
  
  protected void writeAuditLogForUpdate(String paramString, Object paramObject1, Object paramObject2) {
    this.frontEndSupport.writeAuditLog((superDelegate()).client, (new AuditLog(this.codeCurrentUser.getLogin(), paramString)).with(paramAuditLog -> paramAuditLog.oldValue = paramObject)
        .with(paramAuditLog -> paramAuditLog.newValue = paramObject)
        .with(paramAuditLog -> paramAuditLog.reason = "FusionAuth User Interface"));
  }
  
  static {
    locales = LocaleTools.availableLocales();
    timezones.addAll(ZoneId.getAvailableZoneIds());
  }
}
