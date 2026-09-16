package io.fusionauth.app.action;

import com.inversoft.error.Error;
import com.inversoft.error.Errors;
import com.inversoft.json.ToString;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.Pair;
import com.inversoft.util.StringTools;
import io.fusionauth.api.domain.CachedTheme;
import io.fusionauth.api.domain.ConfirmationRequiredReason;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.RuntimeMode;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.api.service.security.CaptchaResult;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.primeframework.ThemedForward.List;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.client.LambdaDelegate;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.Location;
import io.fusionauth.domain.SystemConfiguration;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Theme;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.BaseLoginRequest;
import io.fusionauth.domain.api.LoginRequest;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.api.passwordless.PasswordlessLoginRequest;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.server.HTTPRequest;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.action.result.annotation.Status.List;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.message.l10n.MissingMessageException;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.scope.annotation.Context;
import org.primeframework.mvc.scope.annotation.ManagedCookie;
import org.primeframework.mvc.scope.annotation.ManagedSessionCookie;
import org.primeframework.mvc.security.UnauthenticatedException;
import org.primeframework.mvc.util.QueryStringBuilder;

@List({@Status(code = "not-allowed", status = 405), @Status(code = "not-implemented", status = 501)})
@List({@ThemedForward(code = "*", cacheControl = "no-store"), @ThemedForward(code = "access-control-denied", page = "/unauthorized.ftl", status = 403, cacheControl = "no-store"), @ThemedForward(code = "input", cacheControl = "no-store"), @ThemedForward(code = "error", page = "/oauth2/error.ftl", status = 500, cacheControl = "no-store"), @ThemedForward(code = "render-error", page = "/oauth2/error.ftl", cacheControl = "no-store"), @ThemedForward(code = "unauthenticated", page = "/unauthorized.ftl", status = 401, cacheControl = "no-store"), @ThemedForward(code = "unauthorized", page = "/unauthorized.ftl", status = 403, cacheControl = "no-store"), @ThemedForward(code = "webhook-transaction-failed", cacheControl = "no-store", status = 500)})
@Redirect(code = "confirmation-required", uri = "${redirectToConfirmationRequiredURI}")
public abstract class BaseThemedAction {
  @FTLVariable
  public static final UUID fusionAuthId = Application.FUSIONAUTH_APP_ID;
  
  @FTLVariable
  public static final SortedSet<String> timezones = new TreeSet<>();
  
  protected static final List<String> StandardLoginIdTypes = List.of(IdentityType.email.name, IdentityType.phoneNumber.name, IdentityType.username.name);
  
  protected final FrontEndSupport frontEndSupport;
  
  protected final FrontEndThemeResolver frontEndThemeResolver;
  
  @ManagedSessionCookie(name = "fa.bypass-c", encrypt = false)
  public Cookie _confirmationBypassCookie;
  
  @FTLVariable
  public Application application;
  
  public boolean bypassTheme;
  
  public String client_id;
  
  @FTLVariable
  public String csrfToken;
  
  @ManagedSessionCookie(name = "fa.bypass-c.csrf", encrypt = false)
  public Cookie csrfTokenCookie;
  
  @FTLVariable
  public String currentBaseURL;
  
  @FTLVariable
  public String currentIPAddress;
  
  @FTLVariable
  public Location currentLocation;
  
  @FTLVariable
  public User currentUser;
  
  public Map<String, String> errorMapping = new HashMap<>(0);
  
  @Context
  public Future<?> internalReset;
  
  public Locale locale;
  
  @ManagedSessionCookie(name = "fusionauth.li")
  public Cookie loginIntentCookie;
  
  @FTLVariable
  public String oauthJSONError;
  
  public String redirectToConfirmationRequiredURI;
  
  @ManagedCookie(name = "fusionauth.sso", encrypt = false)
  public Cookie ssoCookie;
  
  @FTLVariable
  public Tenant tenant;
  
  public UUID tenantId;
  
  public LocaleResolvedCachedTheme theme;
  
  public UUID themeId;
  
  @ManagedCookie(name = "fusionauth.trust", encrypt = false)
  public Cookie twoFactorTrustCookie;
  
  @FTLVariable
  public ZoneId zoneId;
  
  protected FusionAuthClient client;
  
  protected Application codeApplication;
  
  protected Tenant codeTenant;
  
  protected User codeUser;
  
  protected String codeUserEmail;
  
  protected UUID codeUserId;
  
  protected LambdaDelegate delegate;
  
  protected LoginIntentService loginIntentService;
  
  protected SSOService ssoService;
  
  protected SSOService.SSOSession ssoSession;
  
  protected SystemConfiguration systemConfiguration;
  
  protected ThreatDetectionService threatDetectionService;
  
  private boolean themeResolved;
  
  protected BaseThemedAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    this.frontEndSupport = paramFrontEndSupport;
    this.frontEndThemeResolver = paramFrontEndThemeResolver;
    this.loginIntentService = paramLoginIntentService;
    this.ssoService = paramSSOService;
    this.threatDetectionService = paramThreatDetectionService;
  }
  
  public Theme getTheme() {
    if (this.themeResolved)
      return this.theme; 
    try {
      resolveApplicationTenantAndTheme();
    } catch (ErrorException errorException) {
      throw errorException;
    } catch (Exception exception) {}
    return this.theme;
  }
  
  @PostParameterMethod
  public void resolveApplicationTenantAndTheme() {
    this.themeResolved = true;
    preResolveApplicationTenantAndTheme();
    if (this.locale == null)
      this.locale = (Locale)this.frontEndSupport.localeProvider.get(); 
    FrontEndThemeResolver.Result result = this.frontEndThemeResolver.resolve(this.frontEndSupport.request, this.client_id, this.tenantId, this.bypassTheme);
    this.codeApplication = result.application;
    this.codeTenant = result.tenant;
    this.theme = new LocaleResolvedCachedTheme(this.locale, result.theme, result.defaultProperties, this.frontEndSupport.messageProvider);
    this.themeId = this.theme.id;
    if (this.codeTenant == null) {
      this.oauthJSONError = this.frontEndSupport.writeToPrettyString(
          (this.tenantId == null) ? 
          new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.missing_tenant_id, "The request is missing a required parameter: tenantId") : 
          new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_tenant_id, String.format("tenantId: %s is not valid.", new Object[] { this.tenantId })));
      throw new ErrorException("render-error", false);
    } 
    this.tenantId = this.codeTenant.id;
    if (this.client_id == null)
      this.client_id = (result.application != null) ? result.application.oauthConfiguration.clientId : null; 
    this.tenant = new Tenant(this.codeTenant);
    this.tenant.secure();
    if (this.codeApplication != null)
      this.application = (new Application(this.codeApplication)).secure(); 
    this.internalReset = this.frontEndSupport.updateFutureMessaging(this.internalReset, "reset-in-progress");
    this.client = this.frontEndSupport.fusionAuthClientProvider.get(this.codeTenant.id);
    this.delegate = new LambdaDelegate(this.client, paramClientResponse -> paramClientResponse.successResponse, this::themeErrorHandling);
    this.systemConfiguration = this.frontEndSupport.systemConfiguration;
    this.currentBaseURL = this.frontEndSupport.getFusionAuthBaseURL();
    this.currentIPAddress = this.frontEndSupport.getTrustedClientIPAddress();
    this.threatDetectionService.handleBlockedIPAddress(this.codeTenant, this.codeApplication, this.currentIPAddress);
    this.ssoSession = this.ssoService.getSession(this.codeTenant, this.ssoCookie);
    if (this.ssoCookie.value == null)
      throw new UnauthenticatedException(); 
  }
  
  public void resolveUserAndZoneId() {
    setUserVariables(this.ssoSession.user);
  }
  
  public <T, U> void themeErrorHandling(ClientResponse<T, U> paramClientResponse) {
    if (paramClientResponse.status == 401)
      throw new UnauthenticatedException(); 
    if (paramClientResponse.status == 404)
      throw new NotFoundException(); 
    if (paramClientResponse.exception != null) {
      addGeneralError("[APIError]", new Object[0]);
      throw new ErrorException("api-error", false);
    } 
    Object object = paramClientResponse.errorResponse;
    if (object instanceof Errors) {
      Errors errors = (Errors)object;
      transferErrors(errors);
      if (errors.containsError("[notLicensed]"))
        throw new ErrorException("not-licensed", false); 
      throw new ErrorException("input", false);
    } 
  }
  
  protected void addFieldError(String paramString1, String paramString2, Object... paramVarArgs) throws MissingMessageException {
    String str = this.theme.message(paramString2, paramVarArgs);
    this.frontEndSupport.messageStore.add((Message)new SimpleFieldMessage(MessageType.ERROR, paramString1, paramString2, str));
  }
  
  protected void addGeneralError(String paramString, Object... paramVarArgs) {
    String str = this.theme.message(paramString, paramVarArgs);
    this.frontEndSupport.messageStore.add((Message)new SimpleMessage(MessageType.ERROR, paramString, str));
  }
  
  protected void addGeneralInfo(String paramString, Object... paramVarArgs) {
    String str = this.theme.message(paramString, paramVarArgs);
    this.frontEndSupport.messageStore.add((Message)new SimpleMessage(MessageType.INFO, paramString, str));
  }
  
  protected void allowConfirmationBypass() {
    this._confirmationBypassCookie.value = "a";
  }
  
  protected SSOService.NewDeviceResult handleNewDevice(BaseLoginRequest paramBaseLoginRequest, UUID paramUUID) {
    paramBaseLoginRequest.newDevice = true;
    if (paramUUID != null) {
      SSOService.NewDeviceResult newDeviceResult = this.ssoService.handleNewDevice(this.frontEndSupport.request.getCookies(), this.codeTenant, paramUUID);
      paramBaseLoginRequest.newDevice = newDeviceResult.isNewDevice();
      return newDeviceResult;
    } 
    return null;
  }
  
  protected void mapWebAuthnFieldErrorsToGeneralErrors() {
    this.frontEndSupport.customPreTransferErrorConsumer = (paramErrors -> {
        this.frontEndSupport.moveFieldErrorToGeneral(paramErrors, "credential.response.attestationObject", "[invalid]credential.response.attestationObject", "[InvalidWebAuthnAuthenticatorResponse]");
        this.frontEndSupport.moveFieldErrorToGeneral(paramErrors, "credential.response.authenticatorData", "[invalid]credential.response.authenticatorData", "[InvalidWebAuthnAuthenticatorResponse]");
        this.frontEndSupport.moveFieldErrorToGeneral(paramErrors, "credential.response.userHandle", "[invalid]credential.response.userHandle", "[InvalidWebAuthnAuthenticatorResponse]");
        this.frontEndSupport.moveFieldErrorToGeneral(paramErrors, "credential.id", "[invalid]credential.id", "[InvalidWebAuthnBrowserResponse]");
        this.frontEndSupport.moveFieldErrorToGeneral(paramErrors, "credential.type", "[blank]credential.type", "[InvalidWebAuthnBrowserResponse]");
        this.frontEndSupport.moveFieldErrorToGeneral(paramErrors, "credential.type", "[invalid]credential.type", "[InvalidWebAuthnBrowserResponse]");
        this.frontEndSupport.moveFieldErrorToGeneral(paramErrors, "credential.response.clientDataJSON", "[invalid]credential.response.clientDataJSON", "[InvalidWebAuthnAuthenticatorResponse]");
        this.frontEndSupport.moveFieldErrorToGeneral(paramErrors, "workflow", "[disabled]workflow", "[WebAuthnDisabled]");
      });
  }
  
  protected void preResolveApplicationTenantAndTheme() {}
  
  protected String redirectToConfirmationRequired(ConfirmationRequiredReason paramConfirmationRequiredReason) {
    QueryStringBuilder queryStringBuilder1 = QueryStringBuilder.builder("/confirmation-required");
    String str1 = this.frontEndSupport.request.getQueryString();
    String str2 = this.frontEndSupport.request.getPath();
    if (str1 != null)
      str2 = str2 + "?" + str2; 
    QueryStringBuilder queryStringBuilder2 = queryStringBuilder1.with("url", str2).with("reason", paramConfirmationRequiredReason.name());
    Stream.<String>of(new String[] { "tenantId", "client_id" }).map(paramString -> new Pair(paramString, this.frontEndSupport.request.getParameter(paramString)))
      .filter(paramPair -> (paramPair.second != null))
      .forEach(paramPair -> paramQueryStringBuilder.with((String)paramPair.first, paramPair.second));
    this
      .redirectToConfirmationRequiredURI = queryStringBuilder2.build();
    return "confirmation-required";
  }
  
  protected void removeConfirmationBypass() {
    this._confirmationBypassCookie.value = null;
  }
  
  protected boolean requireUserConfirmation() {
    return (this._confirmationBypassCookie.value == null);
  }
  
  protected UUID resolveUserId(BaseLoginRequest paramBaseLoginRequest) {
    UUID uUID = null;
    if (paramBaseLoginRequest instanceof PasswordlessLoginRequest) {
      PasswordlessLoginRequest passwordlessLoginRequest = (PasswordlessLoginRequest)paramBaseLoginRequest;
      ExternalIdentifier externalIdentifier = this.frontEndSupport.getExternalId(this.codeTenant.id, passwordlessLoginRequest.code, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.PasswordlessLogin });
      if (externalIdentifier != null)
        uUID = externalIdentifier.userId; 
    } else if (paramBaseLoginRequest instanceof LoginRequest) {
      LoginRequest loginRequest = (LoginRequest)paramBaseLoginRequest;
      if (loginRequest.oneTimePassword != null) {
        ExternalIdentifier externalIdentifier = this.frontEndSupport.getExternalId(this.codeTenant.id, loginRequest.oneTimePassword, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.OneTimePassword });
        if (externalIdentifier != null)
          uUID = externalIdentifier.userId; 
      } else {
        ClientResponse<UserResponse, Errors> clientResponse = this.client.retrieveUserByLoginIdWithLoginIdTypes(loginRequest.loginId, StandardLoginIdTypes);
        if (clientResponse.wasSuccessful())
          uUID = ((UserResponse)clientResponse.successResponse).user.id; 
      } 
    } 
    return uUID;
  }
  
  protected void resolveZoneId() {
    this.zoneId = this.frontEndSupport.resolveZoneId(this.codeUser, (this.codeApplication != null) ? this.codeApplication.id : null);
  }
  
  protected void setUserVariables(User paramUser) {
    if (paramUser != null) {
      this.codeUser = (new User(paramUser)).secure().sort();
      this.currentUser = new User(this.codeUser);
      this.codeUserEmail = this.codeUser.email;
      this.codeUserId = this.codeUser.id;
    } else {
      this.currentUser = null;
    } 
    resolveZoneId();
  }
  
  protected void setupAccountRequestAttributes(Application paramApplication, HTTPRequest paramHTTPRequest) {
    paramHTTPRequest.setAttribute("securityContext.applicationId", paramApplication.id);
    paramHTTPRequest.setAttribute("securityContext.clientId", paramApplication.oauthConfiguration.clientId);
    paramHTTPRequest.setAttribute("securityContext.tenantId", this.codeTenant.id);
    paramHTTPRequest.setAttribute("securityContext.universalApplication", Boolean.valueOf(paramApplication.universalConfiguration.universal));
  }
  
  protected boolean showCaptchaOnInitialPageRender(UUID paramUUID) {
    return (this.threatDetectionService.isCaptchaEnabled(this.codeTenant, (this.frontEndSupport.getReactorStatus()).threatDetection) && this.ssoService
      .getTrustedDeviceState(this.frontEndSupport.request.getCookies(), this.codeTenant, null, paramUUID) == SSOService.TrustedDeviceState.Untrusted);
  }
  
  protected void transferErrors(Errors paramErrors) {
    if (paramErrors == null || paramErrors.size() == 0)
      return; 
    if (this.frontEndSupport.customPreTransferErrorConsumer != null)
      this.frontEndSupport.customPreTransferErrorConsumer.accept(paramErrors); 
    if (this.errorMapping.size() > 0) {
      transferErrors(paramErrors, this.errorMapping);
    } else {
      paramErrors.fieldErrors.forEach((paramString, paramList) -> paramList.forEach(()));
      paramErrors.generalErrors.forEach(this::addGeneralErrorWithMessageFallback);
    } 
  }
  
  protected boolean validateCaptcha(Tenant paramTenant, String paramString1, UUID paramUUID, String paramString2, boolean paramBoolean) {
    CaptchaResult captchaResult = this.threatDetectionService.checkCaptchaChallenge(this.codeTenant, this.codeApplication, paramString2, this.frontEndSupport.getTrustedClientIPAddress(), (this.frontEndSupport.getReactorStatus()).threatDetection);
    if (captchaResult == CaptchaResult.ChallengeSuccess) {
      boolean bool = this instanceof io.fusionauth.app.action.oauth2.AuthorizeAction;
      if (paramUUID != null) {
        SSOService.CookieValue cookieValue = this.ssoService.buildTrustedDeviceCookie(paramTenant, null, paramUUID, bool);
        this.frontEndSupport.addHttpOnlyPersistentCookie(cookieValue.name, cookieValue.value);
      } else if (paramBoolean) {
        SSOService.CookieValue cookieValue = this.ssoService.buildTrustedDeviceCookie(paramTenant, paramString1, null, bool);
        this.frontEndSupport.addHttpOnlyPersistentCookie(cookieValue.name, cookieValue.value);
      } 
      return true;
    } 
    if (captchaResult == CaptchaResult.ChallengeFailed) {
      addFieldError("captcha_token", "[invalid]captcha_token", new Object[0]);
    } else if (captchaResult == CaptchaResult.TokenRequired) {
      addFieldError("captcha_token", "[blank]captcha_token", new Object[0]);
    } 
    return false;
  }
  
  protected void validateCaptchaAfterAttemptingToResolveUser(String paramString1, String paramString2, UUID paramUUID, Runnable paramRunnable) {
    if (StringTools.isTrimmedEmpty(paramString2) && paramUUID == null)
      return; 
    if (this.threatDetectionService.isCaptchaEnabled(this.codeTenant, (this.frontEndSupport.getReactorStatus()).threatDetection)) {
      this.codeUserId = paramUUID;
      if (this.codeUserId == null) {
        ClientResponse<UserResponse, Errors> clientResponse = this.client.retrieveUserByLoginIdWithLoginIdTypes(paramString2, StandardLoginIdTypes);
        if (clientResponse.wasSuccessful())
          this.codeUserId = ((UserResponse)clientResponse.successResponse).user.id; 
      } 
      List<Cookie> list = this.frontEndSupport.request.getCookies();
      SSOService.TrustedDeviceState trustedDeviceState = this.ssoService.getTrustedDeviceState(list, this.codeTenant, paramString2, this.codeUserId);
      boolean bool = (this instanceof io.fusionauth.app.action.oauth2.AuthorizeAction) ? ((trustedDeviceState == SSOService.TrustedDeviceState.TrustedAndVerified) ? true : false) : ((trustedDeviceState == SSOService.TrustedDeviceState.Trusted || trustedDeviceState == SSOService.TrustedDeviceState.TrustedAndVerified) ? true : false);
      if (!bool && 
        !validateCaptcha(this.tenant, paramString2, this.codeUserId, paramString1, true))
        paramRunnable.run(); 
    } 
  }
  
  private void addFieldErrorWithMessageFallback(String paramString1, String paramString2, Error paramError) {
    String str = resolveMessageFallbackToDefaultMessage(paramError.message, paramString2, paramError.values);
    this.frontEndSupport.messageStore.add((Message)new SimpleFieldMessage(MessageType.ERROR, paramString1, paramString2, str));
  }
  
  private void addGeneralErrorWithMessageFallback(Error paramError) {
    String str = resolveMessageFallbackToDefaultMessage(paramError.message, paramError.code, paramError.values);
    this.frontEndSupport.messageStore.add((Message)new SimpleMessage(MessageType.ERROR, paramError.code, str));
  }
  
  private String resolveMessageFallbackToDefaultMessage(String paramString1, String paramString2, Object... paramVarArgs) {
    String str = this.theme.message(paramString2, paramVarArgs);
    if (paramString2.equals(str)) {
      if (this.frontEndSupport.configuration.runtimeMode() == RuntimeMode.Testing)
        throw new IllegalStateException("Missing Theme message for key [" + paramString2 + "]"); 
      str = (paramString1 != null) ? paramString1 : paramString2;
    } 
    return str;
  }
  
  private void transferErrors(Errors paramErrors, Map<String, String> paramMap) {
    paramErrors.fieldErrors.forEach((paramString, paramList) -> paramList.forEach(()));
    paramErrors.generalErrors.forEach(this::addGeneralErrorWithMessageFallback);
  }
  
  public static class LocaleResolvedCachedTheme extends CachedTheme {
    private final Properties fileSystemProperties;
    
    private final Locale locale;
    
    private final MessageProvider messageProvider;
    
    public LocaleResolvedCachedTheme(Locale param1Locale, CachedTheme param1CachedTheme, Properties param1Properties, MessageProvider param1MessageProvider) {
      super(param1CachedTheme);
      this.fileSystemProperties = param1Properties;
      this.locale = param1Locale;
      this.messageProvider = param1MessageProvider;
    }
    
    public Set<Locale> additionalLocales() {
      TreeSet<Locale> treeSet = new TreeSet(Comparator.comparing(param1Locale -> param1Locale.getDisplayLanguage(this.locale)));
      treeSet.addAll(this.localizedProperties.keySet());
      return treeSet;
    }
    
    public String message(String param1String, Object... param1VarArgs) {
      String str = lookupMessage(param1String, param1VarArgs);
      if (str != null)
        return str; 
      return param1String;
    }
    
    public String optionalMessage(String param1String, Object... param1VarArgs) {
      String str = lookupMessage(param1String, param1VarArgs);
      if (str != null)
        return str; 
      return param1String;
    }
    
    public String toString() {
      return ToString.toString(Map.of("name", this.name, "themeId", this.id));
    }
    
    private List<Properties> buildChain(Locale param1Locale) {
      ArrayList<Properties> arrayList = new ArrayList();
      Properties properties = this.localizedProperties.get(param1Locale);
      if (properties != null)
        arrayList.add(properties); 
      if (!param1Locale.getCountry().equals("")) {
        properties = this.localizedProperties.get(new Locale(param1Locale.getLanguage()));
        if (properties != null)
          arrayList.add(properties); 
      } 
      arrayList.add(this.defaultProperties);
      arrayList.add(this.fileSystemProperties);
      return arrayList;
    }
    
    private String lookupMessage(String param1String, Object... param1VarArgs) {
      List<Properties> list = buildChain(this.locale);
      String str = null;
      for (Properties properties : list) {
        str = properties.getProperty(param1String, null);
        if (str != null)
          break; 
      } 
      if (str == null && 
        param1String.indexOf('[') == 0) {
        int i = param1String.indexOf(']', 1);
        if (i != -1) {
          String str1 = param1String.substring(0, i + 1);
          for (Properties properties : list) {
            str = properties.getProperty(str1, null);
            if (str != null)
              break; 
          } 
        } 
      } 
      if (str != null) {
        Formatter formatter = new Formatter();
        formatter.format(this.locale, str, param1VarArgs);
        return formatter.out().toString();
      } 
      return this.messageProvider.getOptionalMessage(param1String, param1VarArgs);
    }
  }
  
  static {
    timezones.addAll(ZoneId.getAvailableZoneIds());
  }
}
