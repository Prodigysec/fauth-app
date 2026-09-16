package io.fusionauth.app.service;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.inversoft.cache.Cache;
import com.inversoft.util.StringTools;
import io.fusionauth.api.domain.CachedTheme;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.guice.FusionAuthTenantId;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.app.action.oauth1.RequestTokenAction;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Theme;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.jwt.JWTUtils;
import io.fusionauth.jwt.domain.JWT;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;
import org.primeframework.mvc.http.HTTPTools;
import org.primeframework.mvc.scope.BrowserActionSessionScope;

public class FrontEndThemeResolver {
  private final ApplicationCache applicationCache;
  
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private final UUID fusionAuthTenantId;
  
  private final Injector injector;
  
  private final ReactorStatusService reactorStatusService;
  
  private final TenantCache tenantCache;
  
  private final Cache<UUID, CachedTheme> themeCache;
  
  @Inject
  public FrontEndThemeResolver(ApplicationCache paramApplicationCache, ExternalIdentifierReaderService paramExternalIdentifierReaderService, @FusionAuthTenantId UUID paramUUID, Injector paramInjector, ReactorStatusService paramReactorStatusService, TenantCache paramTenantCache, Cache<UUID, CachedTheme> paramCache) {
    this.applicationCache = paramApplicationCache;
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    this.fusionAuthTenantId = paramUUID;
    this.injector = paramInjector;
    this.reactorStatusService = paramReactorStatusService;
    this.tenantCache = paramTenantCache;
    this.themeCache = paramCache;
  }
  
  public Result resolve(HTTPRequest paramHTTPRequest, String paramString, UUID paramUUID, boolean paramBoolean) {
    Result result = new Result();
    if (paramUUID != null) {
      result.tenant = (Tenant)this.tenantCache.get(paramUUID);
      if (result.tenant == null) {
        Tenant tenant = (Tenant)this.tenantCache.get(this.fusionAuthTenantId);
        if (tenant.themeId != null)
          result.theme = (CachedTheme)this.themeCache.get(tenant.themeId); 
        if (result.theme == null)
          result.theme = (CachedTheme)this.themeCache.get(Theme.FUSIONAUTH_THEME_ID); 
        result.defaultProperties = result.theme.defaultProperties;
        return result;
      } 
    } 
    if (paramString != null)
      result.application = resolveFromOAuthClientId(paramString); 
    if (!result.complete()) {
      String str1 = paramHTTPRequest.getParameter("changePasswordId");
      resolveFromExternalId(str1, result);
    } 
    if (!result.complete()) {
      String str1 = paramHTTPRequest.getParameter("changePasswordId");
      resolveFromExternalId(str1, result);
    } 
    if (!result.complete()) {
      String str1 = paramHTTPRequest.getParameter("verificationId");
      resolveFromExternalId(str1, result);
    } 
    if (!result.complete()) {
      String str1 = paramHTTPRequest.getParameter("user_code");
      if (str1 != null) {
        str1 = ExternalIdentifier.normalizeDeviceUserCodeId(str1);
        resolveFromExternalId(str1, result);
      } 
    } 
    String str = HTTPTools.getRequestURI(paramHTTPRequest);
    if (!result.complete()) {
      String str1 = null;
      if (str.startsWith("/password/change/") && str.length() > "/password/change/".length() + 1) {
        str1 = str.substring("/password/change/".length());
      } else if (str.startsWith("/registration/verify/") && str.length() > "/registration/verify/".length() + 1) {
        str1 = str.substring("/registration/verify/".length());
      } else if (str.startsWith("/email/verify/") && str.length() > "/email/verify/".length() + 1) {
        str1 = str.substring("/email/verify/".length());
      } 
      resolveFromExternalId(str1, result);
    } 
    if (!result.complete())
      if (str.startsWith("/samlv2/callback/") && str.length() > "/samlv2/callback/".length() + 1) {
        String str1 = paramHTTPRequest.getParameter("code");
        if (str1 != null)
          resolveFromExternalId(str1, result); 
        if (result.tenant == null)
          result.tenant = safelyRetrieveById(str.substring("/samlv2/callback/".length())); 
      } else if (str.startsWith("/samlv2/login/") && str.length() > "/samlv2/login/".length() + 1) {
        result.tenant = safelyRetrieveById(str.substring("/samlv2/login/".length()));
      } else if (str.startsWith("/samlv2/logout/") && str.length() > "/samlv2/logout/".length() + 1) {
        UUID uUID = StringTools.parseUUID(str.substring("/samlv2/logout/".length()));
        if (uUID != null) {
          result.tenant = (Tenant)this.tenantCache.get(uUID);
          if (!result.complete())
            result.application = (Application)this.applicationCache.get(uUID); 
        } 
      }  
    if (result.application == null && (
      str.startsWith("/samlv2/acs") || str.startsWith("/oauth2/callback") || str.startsWith("/oauth2/device"))) {
      String str1 = paramHTTPRequest.getParameter("state");
      if (str1 == null)
        str1 = paramHTTPRequest.getParameter("RelayState"); 
      if (str1 != null)
        result.application = resolveFromEncodedState(str1); 
      if (result.application == null) {
        RequestTokenAction.OAuth1 oAuth1 = (RequestTokenAction.OAuth1)BrowserActionSessionScope.get(this.injector, paramHTTPRequest, "state", RequestTokenAction.OAuth1.class, RequestTokenAction.class);
        if (oAuth1 != null)
          result.application = resolveFromOAuthClientId(oAuth1.client_id); 
      } 
    } 
    if (result.application == null) {
      String str1 = paramHTTPRequest.getParameter("id_token_hint");
      if (str1 != null)
        try {
          JWT jWT = JWTUtils.decodePayload(str1);
          String str2 = jWT.getString("aud");
          result.application = resolveFromOAuthClientId(str2);
          if (result.tenant == null && result.application != null && result.application.universalConfiguration.universal) {
            String str3 = jWT.getString("tid");
            if (str3 != null) {
              UUID uUID = StringTools.parseUUID(str3);
              if (uUID != null)
                result.tenant = (Tenant)this.tenantCache.get(uUID); 
            } 
          } 
        } catch (Exception exception) {} 
    } 
    if (result.tenant != null && result.application != null && !result.application.universalConfiguration.universal && 

      
      !result.tenant.id.equals(result.application.tenantId))
      result.application = null; 
    if (result.tenant == null)
      if (result.application != null) {
        if (!result.application.universalConfiguration.universal)
          result.tenant = (Tenant)this.tenantCache.get(result.application.tenantId); 
      } else {
        result.tenant = (Tenant)this.tenantCache.get(this.fusionAuthTenantId);
      }  
    boolean bool = ReactorStatusValidator.isLicensedFor(this.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.applicationThemes);
    if (paramBoolean) {
      result.theme = (CachedTheme)this.themeCache.get(Theme.FUSIONAUTH_THEME_ID);
    } else if (result.application != null && result.application.themeId != null && bool) {
      result.theme = (CachedTheme)this.themeCache.get(result.application.themeId);
    } 
    if (result.theme == null && result.tenant != null)
      result.theme = (CachedTheme)this.themeCache.get(result.tenant.themeId); 
    if (result.theme == null)
      result.theme = (CachedTheme)this.themeCache.get(Theme.FUSIONAUTH_THEME_ID); 
    result.defaultProperties = ((CachedTheme)this.themeCache.get(Theme.FUSIONAUTH_THEME_ID)).defaultProperties;
    return result;
  }
  
  private Application resolveFromEncodedState(String paramString) {
    try {
      paramString = new String(Base64.getUrlDecoder().decode(paramString.getBytes(StandardCharsets.UTF_8)));
      String[] arrayOfString = paramString.split("&");
      for (String str : arrayOfString) {
        String[] arrayOfString1 = str.split("=", 2);
        if (arrayOfString1[0].equals("client_id"))
          return resolveFromOAuthClientId(URLDecoder.decode(arrayOfString1[1], "UTF-8")); 
      } 
    } catch (Exception exception) {}
    return null;
  }
  
  private void resolveFromExternalId(String paramString, Result paramResult) {
    if (paramString == null)
      return; 
    ExternalIdentifier externalIdentifier = this.externalIdentifierReader.retrieveById(paramString);
    if (externalIdentifier == null)
      return; 
    paramResult.tenant = (Tenant)this.tenantCache.get(externalIdentifier.tenantId);
    if (externalIdentifier.applicationId != null && paramResult.application == null)
      paramResult.application = (Application)this.applicationCache.get(externalIdentifier.applicationId); 
  }
  
  private Application resolveFromOAuthClientId(String paramString) {
    try {
      return (Application)this.applicationCache.get(UUID.fromString(paramString));
    } catch (Exception exception) {
      return null;
    } 
  }
  
  private Tenant safelyRetrieveById(String paramString) {
    try {
      return (Tenant)this.tenantCache.get(UUID.fromString(paramString));
    } catch (Exception exception) {
      return null;
    } 
  }
  
  public static class Result {
    public Application application;
    
    public Properties defaultProperties;
    
    public Tenant tenant;
    
    public CachedTheme theme;
    
    public boolean complete() {
      return (this.tenant != null && this.application != null);
    }
    
    public boolean invalid() {
      return (this.tenant == null || (this.application != null && !this.tenant.id.equals(this.application.tenantId)));
    }
  }
}
