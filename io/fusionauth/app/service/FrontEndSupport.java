package io.fusionauth.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.inversoft.error.Error;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.RESTClient;
import com.inversoft.util.SecurityTools;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.Instance;
import io.fusionauth.api.domain.api.ExternalIdentifierResponse;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.domain.guice.FusionAuthTenantId;
import io.fusionauth.api.domain.json.introspector.MaskAnnotationIntrospector;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.api.service.cache.InstanceCache;
import io.fusionauth.api.service.cache.SystemConfigurationCache;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.api.service.user.PasswordlessService;
import io.fusionauth.api.util.NetworkTools;
import io.fusionauth.api.util.URITools;
import io.fusionauth.app.Cookies;
import io.fusionauth.app.action.oauth2.BaseOAuthAction;
import io.fusionauth.app.primeframework.exceptions.FusionAuthMissingFormatArgumentException;
import io.fusionauth.app.primeframework.exceptions.InvalidCSRFTokenException;
import io.fusionauth.app.service.identityProvider.IdentityProviderFrontendService;
import io.fusionauth.app.service.user.UserPreferencesFrontendService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.AuditLog;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.SystemConfiguration;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.AuditLogRequest;
import io.fusionauth.domain.api.AuditLogResponse;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.reactor.ReactorStatus;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.HTTPMethod;
import io.fusionauth.http.server.HTTPContext;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import io.fusionauth.jwt.Verifier;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.locale.LocaleProvider;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.message.l10n.MissingMessageException;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.security.JWTRequestAdapter;
import org.primeframework.mvc.security.UnauthenticatedException;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.csrf.CSRFProvider;
import org.primeframework.mvc.util.Compressor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrontEndSupport {
  private static final Logger logger = LoggerFactory.getLogger(FrontEndSupport.class);
  
  public final ActionInvocationStore actionInvocationStore;
  
  public final FusionAuthConfiguration configuration;
  
  public final HTTPContext context;
  
  public final CSRFProvider csrfProvider;
  
  public final Encryptor encryptor;
  
  public final ExpressionEvaluator expressionEvaluator;
  
  public final FusionAuthClientProvider fusionAuthClientProvider;
  
  public final UUID fusionAuthTenantId;
  
  public final Map<IdentityProviderType, IdentityProviderFrontendService> identityProviderFrontendServices;
  
  public final LocaleProvider localeProvider;
  
  public final MessageProvider messageProvider;
  
  public final MessageStore messageStore;
  
  public final HTTPMethod method;
  
  public final PasswordlessService passwordlessService;
  
  public final ReactorStatusService reactorStatusService;
  
  public final HTTPRequest request;
  
  public final JWTRequestAdapter requestAdapter;
  
  public final SystemConfiguration systemConfiguration;
  
  public final TenantCache tenantCache;
  
  public final TenantReaderService tenantReader;
  
  public final UserLoginSecurityContext userLoginSecurityContext;
  
  public final Provider<UserPreferencesFrontendService> userPreferencesServiceProvider;
  
  public final Provider<Map<String, Verifier>> verifierProvider;
  
  private final InstanceCache instanceCache;
  
  public Consumer<Errors> customPreTransferErrorConsumer;
  
  public Function<Error, Error> errorMapperFunction;
  
  public Map<String, String> errorMapping = new HashMap<>(0);
  
  public Function<String, String> fieldMapperFunction;
  
  public ObjectMapper objectMapper;
  
  public HTTPResponse response;
  
  @Inject
  public FrontEndSupport(ActionInvocationStore paramActionInvocationStore, FusionAuthConfiguration paramFusionAuthConfiguration, CSRFProvider paramCSRFProvider, Encryptor paramEncryptor, ExpressionEvaluator paramExpressionEvaluator, FusionAuthClientProvider paramFusionAuthClientProvider, @FusionAuthTenantId UUID paramUUID, LocaleProvider paramLocaleProvider, MessageProvider paramMessageProvider, MessageStore paramMessageStore, HTTPMethod paramHTTPMethod, PasswordlessService paramPasswordlessService, ReactorStatusService paramReactorStatusService, JWTRequestAdapter paramJWTRequestAdapter, HTTPResponse paramHTTPResponse, HTTPRequest paramHTTPRequest, Map<IdentityProviderType, IdentityProviderFrontendService> paramMap, ObjectMapper paramObjectMapper, HTTPContext paramHTTPContext, SystemConfigurationCache paramSystemConfigurationCache, TenantCache paramTenantCache, TenantReaderService paramTenantReaderService, UserLoginSecurityContext paramUserLoginSecurityContext, Provider<UserPreferencesFrontendService> paramProvider, Provider<Map<String, Verifier>> paramProvider1, InstanceCache paramInstanceCache) {
    this.actionInvocationStore = paramActionInvocationStore;
    this.configuration = paramFusionAuthConfiguration;
    this.csrfProvider = paramCSRFProvider;
    this.encryptor = paramEncryptor;
    this.expressionEvaluator = paramExpressionEvaluator;
    this.localeProvider = paramLocaleProvider;
    this.messageProvider = paramMessageProvider;
    this.messageStore = paramMessageStore;
    this.method = paramHTTPMethod;
    this.fusionAuthClientProvider = paramFusionAuthClientProvider;
    this.fusionAuthTenantId = paramUUID;
    this.reactorStatusService = paramReactorStatusService;
    this.requestAdapter = paramJWTRequestAdapter;
    this.identityProviderFrontendServices = paramMap;
    this.objectMapper = paramObjectMapper;
    this.passwordlessService = paramPasswordlessService;
    this.response = paramHTTPResponse;
    this.request = paramHTTPRequest;
    this.context = paramHTTPContext;
    this.systemConfiguration = paramSystemConfigurationCache.get();
    this.tenantCache = paramTenantCache;
    this.tenantReader = paramTenantReaderService;
    this.userLoginSecurityContext = paramUserLoginSecurityContext;
    this.userPreferencesServiceProvider = paramProvider;
    this.verifierProvider = paramProvider1;
    this.instanceCache = paramInstanceCache;
  }
  
  public void addFieldError(String paramString1, String paramString2, Object... paramVarArgs) throws MissingMessageException {
    addFieldError(paramString1, paramString2, null, paramVarArgs);
  }
  
  public void addFieldError(String paramString1, String paramString2, Map<String, Object> paramMap, Object... paramVarArgs) throws MissingMessageException {
    String str = null;
    if (paramString2.startsWith("[invalidJSON]")) {
      str = this.messageProvider.getOptionalMessage(paramString2, paramVarArgs);
      if (str == null)
        str = this.messageProvider.getOptionalMessage(paramString2.replace("[invalidJSON]", "[couldNotConvert]"), new Object[0]); 
    } 
    if (str == null)
      try {
        str = this.messageProvider.getMessage(paramString2, paramVarArgs);
      } catch (Exception exception) {
        if (exception instanceof java.util.MissingFormatArgumentException)
          throw new FusionAuthMissingFormatArgumentException("Failed to format message [" + paramString2 + "].", exception); 
        throw exception;
      }  
    this.messageStore.add((Message)new SimpleFieldMessage(MessageType.ERROR, paramString1, paramString2, str, paramMap));
  }
  
  public void addGeneralError(String paramString, Object... paramVarArgs) {
    String str = this.messageProvider.getMessage(paramString, paramVarArgs);
    this.messageStore.add((Message)new SimpleMessage(MessageType.ERROR, paramString, str));
  }
  
  public void addGeneralError(MessageScope paramMessageScope, String paramString, Object... paramVarArgs) {
    String str = this.messageProvider.getMessage(paramString, paramVarArgs);
    this.messageStore.add(paramMessageScope, (Message)new SimpleMessage(MessageType.ERROR, paramString, str));
  }
  
  public void addGeneralInfo(String paramString, Object... paramVarArgs) {
    String str = this.messageProvider.getMessage(paramString, paramVarArgs);
    this.messageStore.add((Message)new SimpleMessage(MessageType.INFO, paramString, str));
  }
  
  public void addGeneralInfo(MessageScope paramMessageScope, String paramString, Object... paramVarArgs) {
    String str = this.messageProvider.getMessage(paramString, paramVarArgs);
    this.messageStore.add(paramMessageScope, (Message)new SimpleMessage(MessageType.INFO, paramString, str));
  }
  
  public void addGeneralWarning(String paramString, Object... paramVarArgs) {
    String str = this.messageProvider.getMessage(paramString, paramVarArgs);
    this.messageStore.add((Message)new SimpleMessage(MessageType.WARNING, paramString, str));
  }
  
  public void addHttpOnlyPersistentCookie(String paramString1, String paramString2, String paramString3) {
    Cookies.addHttpOnlyPersistent(this.request, this.response, paramString1, paramString2, Long.valueOf(2147483647L), paramString3);
  }
  
  public void addHttpOnlyPersistentCookie(String paramString1, String paramString2, long paramLong) {
    Cookies.addHttpOnlyPersistent(this.request, this.response, paramString1, paramString2, Long.valueOf(paramLong), null);
  }
  
  public void addHttpOnlyPersistentCookie(String paramString1, String paramString2) {
    Cookies.addHttpOnlyPersistent(this.request, this.response, paramString1, paramString2, Long.valueOf(2147483647L), null);
  }
  
  public void addHttpOnlySessionCookie(String paramString1, String paramString2) {
    Cookies.addHttpOnlySession(this.request, this.response, paramString1, paramString2, null);
  }
  
  public void addHttpOnlySessionCookie(String paramString1, String paramString2, String paramString3) {
    Cookies.addHttpOnlySession(this.request, this.response, paramString1, paramString2, paramString3);
  }
  
  public void addPersistentCookie(String paramString1, String paramString2, String paramString3) {
    Cookies.addPersistent(this.request, this.response, paramString1, paramString2, Long.valueOf(2147483647L), paramString3);
  }
  
  public void addPersistentCookie(String paramString1, String paramString2, long paramLong, String paramString3) {
    Cookies.addPersistent(this.request, this.response, paramString1, paramString2, Long.valueOf(paramLong), paramString3);
  }
  
  public void addSessionCookie(String paramString1, String paramString2, String paramString3) {
    Cookies.addSession(this.request, this.response, paramString1, paramString2, paramString3);
  }
  
  public <T, U> void ajaxJSONErrorHandling(ClientResponse<T, U> paramClientResponse) {
    if (paramClientResponse.status == 404)
      throw new NotFoundException(); 
    if (paramClientResponse.exception != null) {
      addGeneralError("[APIError]", new Object[0]);
      throw new ErrorException("render-error-json", false);
    } 
    if (paramClientResponse.errorResponse instanceof Errors) {
      transfer((Errors)paramClientResponse.errorResponse);
      throw new ErrorException("render-input-json", false);
    } 
  }
  
  public EventInfo buildEventInfo(RefreshToken.MetaData paramMetaData) {
    EventInfo eventInfo = new EventInfo(paramMetaData);
    eventInfo.ipAddress = getTrustedClientIPAddress();
    eventInfo.userAgent = this.request.getHeader("User-Agent");
    return eventInfo;
  }
  
  public EventInfo buildEventInfo() {
    return buildEventInfo(null);
  }
  
  public String decrypt(String paramString) {
    try {
      return new String(this.encryptor.decrypt(Base64.getUrlDecoder().decode(paramString)), StandardCharsets.UTF_8);
    } catch (Exception exception) {
      throw new ErrorException("error", exception, new Object[0]);
    } 
  }
  
  public void deleteCookies(String... paramVarArgs) {
    Cookies.delete(this.response, paramVarArgs);
  }
  
  public void deleteCookiesWithDomain(String paramString, String... paramVarArgs) {
    Cookies.delete(paramString, this.response, paramVarArgs);
  }
  
  public void deleteCookiesWithPrefix(String paramString) {
    String str = paramString + ".";
    for (Cookie cookie : this.request.getCookies()) {
      if (cookie.getName().startsWith(str))
        deleteCookies(new String[] { cookie.getName() }); 
    } 
  }
  
  public <T> T deserializeBase64EncodedObject(String paramString, Class<T> paramClass) {
    try {
      byte[] arrayOfByte = Base64.getUrlDecoder().decode(paramString);
      return (T)this.objectMapper.readValue(arrayOfByte, paramClass);
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    } 
  }
  
  public <T> T deserializeBase64EncodedObjectOrDefault(String paramString, Class<T> paramClass, Supplier<T> paramSupplier) {
    try {
      return deserializeBase64EncodedObject(paramString, paramClass);
    } catch (Exception exception) {
      return paramSupplier.get();
    } 
  }
  
  public BaseOAuthAction.OAuthContext deserializeOAuthContext(String paramString) {
    try {
      byte[] arrayOfByte1 = Base64.getUrlDecoder().decode(paramString);
      byte[] arrayOfByte2 = this.encryptor.decrypt(arrayOfByte1);
      byte[] arrayOfByte3 = Compressor.decompress(arrayOfByte2);
      return (BaseOAuthAction.OAuthContext)this.objectMapper.readerFor(BaseOAuthAction.OAuthContext.class).readValue(arrayOfByte3);
    } catch (Exception exception) {
      logger.debug("Failed to deserialize the oauth_context.", exception);
      throw new ErrorException("error", exception, new Object[0]);
    } 
  }
  
  public String encrypt(String paramString) {
    try {
      return Base64.getUrlEncoder().encodeToString(this.encryptor.encrypt(paramString.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception exception) {
      throw new ErrorException("error", exception, new Object[0]);
    } 
  }
  
  public <T, U> void frontEndErrorHandling(ClientResponse<T, U> paramClientResponse) {
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
      this.request.setAttribute("apiErrorResponse", copyErrors(errors));
      transfer(errors);
      if (errors.containsError("[notLicensed]"))
        throw new ErrorException("not-licensed", false); 
      throw new ErrorException("input", false);
    } 
  }
  
  public Cookie getCookie(String paramString) {
    return Cookies.get(this.request, paramString);
  }
  
  public ExternalIdentifier getExternalId(UUID paramUUID, String paramString, ExternalIdentifier.ExternalIdType... paramVarArgs) {
    RESTClient rESTClient = this.fusionAuthClientProvider.<T, U>newRESTClient((Class)ExternalIdentifierResponse.class, (Class)void.class).uri("/api/system/external-identifier").urlSegment(paramString);
    if (paramUUID != null)
      rESTClient.header(FusionAuthClient.TENANT_ID_HEADER, paramUUID.toString()); 
    for (ExternalIdentifier.ExternalIdType externalIdType : paramVarArgs)
      rESTClient.urlParameter("type", externalIdType); 
    ClientResponse clientResponse = rESTClient.get().go();
    if (clientResponse.wasSuccessful())
      return ((ExternalIdentifierResponse)clientResponse.successResponse).externalIdentifier; 
    return null;
  }
  
  public String getFusionAuthBaseURL() {
    return this.request.getBaseURL();
  }
  
  public Instance getInstance() {
    return (new Instance(this.instanceCache.get())).secure();
  }
  
  public String getOrigin() {
    String str = this.request.getHeader("Origin");
    if (str == null) {
      String str1 = this.request.getHeader("Referer");
      if (str1 != null) {
        URI uRI = URI.create(str1);
        str = uRI.getHost();
        if ((uRI.getPort() != 80 && uRI.getScheme().equals("http")) || (uRI.getPort() != 443 && uRI.getScheme().equals("https")))
          str = str + ":" + str; 
      } 
    } 
    return (str != null) ? str : this.request.getIPAddress();
  }
  
  public ReactorStatus getReactorStatus() {
    return this.reactorStatusService.retrieveStatus();
  }
  
  public String getTrustedClientIPAddress() {
    return NetworkTools.getTrustedClientIPAddress(this.request, this.configuration, this.systemConfiguration);
  }
  
  public boolean hasErrorMessages() {
    return this.messageStore.get().stream().anyMatch(paramMessage -> (paramMessage.getType() == MessageType.ERROR));
  }
  
  public boolean hasNoErrorMessages() {
    return !hasErrorMessages();
  }
  
  public boolean isDELETE() {
    return (this.method == HTTPMethod.DELETE);
  }
  
  public boolean isGET() {
    return (this.method == HTTPMethod.GET);
  }
  
  public boolean isInternalRedirect() {
    return URITools.isSameBaseURL(this.request.getHeader("Referer"), getFusionAuthBaseURL());
  }
  
  public boolean isPATCH() {
    return (this.method == HTTPMethod.PATCH);
  }
  
  public boolean isPOST() {
    return (this.method == HTTPMethod.POST);
  }
  
  public boolean isPUT() {
    return (this.method == HTTPMethod.PUT);
  }
  
  public void moveFieldErrorToGeneral(Errors paramErrors, String paramString1, String paramString2, String paramString3) {
    Iterator<String> iterator = paramErrors.fieldErrors.keySet().iterator();
    while (iterator.hasNext()) {
      String str = iterator.next();
      if (str.equals(paramString1)) {
        List list = (List)paramErrors.fieldErrors.get(str);
        boolean bool = false;
        for (Error error : list) {
          if (error.code.equals(paramString2)) {
            bool = true;
            paramErrors.addGeneralError(paramString3, null, new Object[0]);
          } 
        } 
        if (bool)
          iterator.remove(); 
      } 
    } 
  }
  
  public void moveFieldErrorToGeneral(Errors paramErrors, String paramString1, String paramString2) {
    moveFieldErrorToGeneral(paramErrors, paramString1, paramString2, paramString2);
  }
  
  public void moveFieldErrorsToGeneral(Errors paramErrors, String... paramVarArgs) {
    List<String> list = Arrays.asList(paramVarArgs);
    Iterator<String> iterator = paramErrors.fieldErrors.keySet().iterator();
    while (iterator.hasNext()) {
      String str1 = iterator.next();
      String str2 = list.stream().filter(paramString2 -> paramString2.equals(paramString1)).findFirst().orElse(null);
      if (str2 != null) {
        List list1 = (List)paramErrors.fieldErrors.get(str1);
        for (Error error : list1)
          paramErrors.addGeneralError(error.code, null, new Object[0]); 
        iterator.remove();
      } 
    } 
  }
  
  public String prettyPrint(Map<String, Object> paramMap) {
    if (paramMap == null)
      return null; 
    try {
      return this.objectMapper.writer().with(SerializationFeature.INDENT_OUTPUT).writeValueAsString(paramMap);
    } catch (Exception exception) {
      return null;
    } 
  }
  
  public ZoneId resolveZoneId(User paramUser, UUID paramUUID) {
    if (paramUser == null)
      return this.systemConfiguration.reportTimezone; 
    ZoneId zoneId = null;
    if (paramUUID != null) {
      UserRegistration userRegistration = paramUser.getRegistrationForApplication(paramUUID);
      if (userRegistration != null)
        zoneId = userRegistration.timezone; 
    } 
    if (zoneId == null)
      zoneId = paramUser.timezone; 
    if (zoneId == null) {
      Cookie cookie = Cookies.get(this.request, "fusionauth.timezone");
      if (cookie != null)
        try {
          zoneId = ZoneId.of(cookie.value);
        } catch (Exception exception) {} 
    } 
    if (zoneId == null)
      zoneId = this.systemConfiguration.reportTimezone; 
    return zoneId;
  }
  
  public String serializeOAuthContext(BaseOAuthAction.OAuthContext paramOAuthContext) {
    try {
      byte[] arrayOfByte1 = this.objectMapper.writeValueAsBytes(paramOAuthContext);
      byte[] arrayOfByte2 = Compressor.compress(arrayOfByte1);
      byte[] arrayOfByte3 = this.encryptor.encrypt(arrayOfByte2);
      return Base64.getUrlEncoder().encodeToString(arrayOfByte3);
    } catch (Exception exception) {
      logger.debug("Failed to serialize the oauth_context. This is likely a programming error.", exception);
      throw new ErrorException("error", exception, new Object[0]);
    } 
  }
  
  public String serializeObjectToBase64(Object paramObject) {
    try {
      return Base64.getUrlEncoder().encodeToString(this.objectMapper.writeValueAsBytes(paramObject));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    } 
  }
  
  public String storeCSRFToken() {
    return storeCSRFToken(null);
  }
  
  public String storeCSRFToken(Map<String, String> paramMap) {
    Cookie cookie = (paramMap == null) ? getCookie("fusionauth.csrf") : null;
    if (cookie == null) {
      String str = SecurityTools.secureRandom();
      if (paramMap != null && paramMap.size() > 0)
        try {
          str = str + "." + str;
        } catch (Exception exception) {
          throw new RuntimeException(exception);
        }  
      addHttpOnlySessionCookie("fusionauth.csrf", str);
      return str;
    } 
    return cookie.value;
  }
  
  public void transfer(Errors paramErrors) {
    if (paramErrors == null || paramErrors.size() == 0)
      return; 
    if (this.customPreTransferErrorConsumer != null)
      this.customPreTransferErrorConsumer.accept(paramErrors); 
    if (this.fieldMapperFunction != null && this.errorMapperFunction != null) {
      transfer(paramErrors, this.fieldMapperFunction, this.errorMapperFunction);
    } else if (this.errorMapping.size() > 0) {
      transfer(paramErrors, this.errorMapping);
    } else {
      paramErrors.fieldErrors.forEach((paramString, paramList) -> paramList.forEach(()));
      paramErrors.generalErrors.forEach(paramError -> addGeneralError(paramError.code, paramError.values));
    } 
  }
  
  public void transfer(Errors paramErrors, EventLog paramEventLog) {
    transfer(paramErrors);
    if (paramEventLog != null) {
      paramEventLog.message += paramEventLog.message;
      EventLogHelper.create(paramEventLog);
    } 
  }
  
  public void transfer(Errors paramErrors, Map<String, String> paramMap) {
    if (paramErrors == null || paramErrors.size() == 0)
      return; 
    paramErrors.fieldErrors.forEach((paramString, paramList) -> paramList.forEach(()));
    paramErrors.generalErrors.forEach(paramError -> addGeneralError(paramError.code, paramError.values));
  }
  
  public void transfer(Errors paramErrors, Function<String, String> paramFunction, Function<Error, Error> paramFunction1) {
    if (paramErrors == null || paramErrors.size() == 0)
      return; 
    paramErrors.fieldErrors.forEach((paramString, paramList) -> paramList.forEach(()));
    paramErrors.generalErrors.forEach(paramError -> addGeneralError(paramError.code, paramError.values));
  }
  
  public Future<?> updateFutureMessaging(Future<?> paramFuture, String paramString) {
    if (paramFuture == null || paramFuture.isDone())
      return null; 
    if (this.messageStore.getGeneralMessages().stream().noneMatch(paramMessage -> paramMessage.getCode().equals(paramString)))
      addGeneralInfo(paramString, new Object[0]); 
    return paramFuture;
  }
  
  public void validateCSRFToken(String paramString) {
    validateCSRFToken(paramString, null);
  }
  
  public void validateCSRFToken(String paramString, Consumer<Map<String, String>> paramConsumer) {
    Cookie cookie = getCookie("fusionauth.csrf");
    if (cookie == null) {
      this.userLoginSecurityContext.logout(buildEventInfo(null));
      throw new InvalidCSRFTokenException();
    } 
    deleteCookies(new String[] { "fusionauth.csrf" });
    String str = cookie.value;
    if (str == null || !str.equals(paramString)) {
      this.userLoginSecurityContext.logout(buildEventInfo(null));
      throw new InvalidCSRFTokenException();
    } 
    if (paramConsumer != null) {
      int i = str.indexOf('.');
      if (i != -1) {
        String str1 = str.substring(i + 1);
        byte[] arrayOfByte = Base64.getUrlDecoder().decode(str1);
        try {
          Map<String, String> map = (Map)this.objectMapper.readerForMapOf(String.class).readValue(arrayOfByte);
          paramConsumer.accept(map);
        } catch (IOException iOException) {
          this.userLoginSecurityContext.logout(buildEventInfo(null));
          throw new InvalidCSRFTokenException();
        } 
      } 
    } 
  }
  
  public void writeAuditLog(FusionAuthClient paramFusionAuthClient, AuditLog paramAuditLog) {
    FusionAuthClient fusionAuthClient = paramFusionAuthClient.setObjectMapper(FusionAuthClient.objectMapper.copy().setAnnotationIntrospector((AnnotationIntrospector)new MaskAnnotationIntrospector()));
    ClientResponse<AuditLogResponse, Errors> clientResponse = fusionAuthClient.createAuditLog(new AuditLogRequest(buildEventInfo(null), paramAuditLog));
    if (!clientResponse.wasSuccessful())
      logger.error("Audit log API call failed.\nCode=[" + clientResponse.status + "]\nErrors=[" + String.valueOf(clientResponse.errorResponse) + "]\nException=[" + String.valueOf(clientResponse.exception) + "]"); 
  }
  
  public String writeToPrettyString(Object paramObject) {
    try {
      return this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(paramObject);
    } catch (JsonProcessingException jsonProcessingException) {
      throw new ErrorException("error", jsonProcessingException, new Object[0]);
    } 
  }
  
  private Errors copyErrors(Errors paramErrors) {
    Errors errors = new Errors();
    paramErrors.fieldErrors.keySet()
      .forEach(paramString -> paramErrors1.fieldErrors.put(paramString, (List)((List)paramErrors2.fieldErrors.get(paramString)).stream().map(()).collect(Collectors.toList())));
    paramErrors.generalErrors.forEach(paramError -> paramErrors.generalErrors.add(new Error(paramError.code, paramError.message, paramError.data, paramError.values)));
    return errors;
  }
}
