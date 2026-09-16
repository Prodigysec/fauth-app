package io.fusionauth.api.service.connector;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.JSONBodyHandler;
import com.inversoft.rest.JSONResponseHandler;
import com.inversoft.rest.ProxyInfo;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.rest.RESTClient;
import com.inversoft.util.StringTools;
import io.fusionauth.api.domain.UserMapper;
import io.fusionauth.api.security.KeyCache;
import io.fusionauth.api.service.authentication.AuthenticationService;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.email.EmailProxy;
import io.fusionauth.api.service.group.GroupReaderService;
import io.fusionauth.api.service.group.GroupService;
import io.fusionauth.api.service.reactor.ReactorService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.eventLog.Debugger;
import io.fusionauth.api.service.user.IdentityTypeHelper;
import io.fusionauth.api.service.user.PasswordService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.LoginRequest;
import io.fusionauth.domain.api.LoginResponse;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.connector.ConnectorPolicy;
import io.fusionauth.domain.connector.GenericConnectorConfiguration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GenericConnector extends BaseExternalConnector {
  private final KeyCache keyCache;
  
  private final MetricRegistry metricRegistry;
  
  private final ProxyInfoSupplier proxyInfoSupplier;
  
  @Inject
  public GenericConnector(EmailProxy paramEmailProxy, PasswordService paramPasswordService, ReactorService paramReactorService, UserMapper paramUserMapper, UserService paramUserService, ApplicationCache paramApplicationCache, ApplicationReaderService paramApplicationReaderService, GroupReaderService paramGroupReaderService, GroupService paramGroupService, UserReaderService paramUserReaderService, KeyCache paramKeyCache, MetricRegistry paramMetricRegistry, ProxyInfoSupplier paramProxyInfoSupplier) {
    super(paramEmailProxy, paramPasswordService, paramReactorService, paramUserMapper, paramUserService, paramApplicationCache, paramApplicationReaderService, paramGroupReaderService, paramGroupService, paramUserReaderService);
    this.keyCache = paramKeyCache;
    this.metricRegistry = paramMetricRegistry;
    this.proxyInfoSupplier = paramProxyInfoSupplier;
  }
  
  public AuthenticationService.AuthenticationResult authenticate(BaseConnectorConfiguration paramBaseConnectorConfiguration, ConnectorPolicy paramConnectorPolicy, Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, List<IdentityType> paramList, String paramString2, EventInfo paramEventInfo) {
    if (!canHandle(paramConnectorPolicy, paramString1))
      return null; 
    GenericConnectorConfiguration genericConnectorConfiguration = (GenericConnectorConfiguration)paramBaseConnectorConfiguration;
    UUID uUID = (paramApplication != null) ? paramApplication.id : null;
    Debugger debugger = new Debugger(genericConnectorConfiguration.debug, String.valueOf(genericConnectorConfiguration.getType()) + " Connector Debug Log for [" + String.valueOf(genericConnectorConfiguration.getType()) + "] with Id [" + genericConnectorConfiguration.name + "].");
    debugger.log("Attempting authentication request " + (
        (uUID != null) ? ("to application with Id [" + String.valueOf(uUID) + "] ") : "") + (
        (paramEventInfo != null && paramEventInfo.ipAddress != null) ? ("from IP address [" + paramEventInfo.ipAddress + "] ") : "") + "for [" + paramString1 + "] against the URL [" + String.valueOf(genericConnectorConfiguration.authenticationURL) + "].");
    long l = System.currentTimeMillis();
    Meter meter = this.metricRegistry.meter("fusionauth.connector.[" + String.valueOf(paramBaseConnectorConfiguration.id) + "].errors");
    Timer.Context context = this.metricRegistry.timer("fusionauth.connector.[" + String.valueOf(paramBaseConnectorConfiguration.id) + "].requests").time();
    Map map = (Map)genericConnectorConfiguration.headers.keySet().stream().collect(Collectors.toMap(paramString -> paramString, paramString -> new ArrayList(Collections.singletonList(paramGenericConnectorConfiguration.headers.get(paramString)))));
    ClientResponse<?, ?> clientResponse = (new RESTClient(LoginResponse.class, Errors.class)).url(genericConnectorConfiguration.authenticationURL.toString()).certificate(extractSSLCertificate(genericConnectorConfiguration)).basicAuthorization(genericConnectorConfiguration.httpAuthenticationUsername, genericConnectorConfiguration.httpAuthenticationPassword).connectTimeout(genericConnectorConfiguration.connectTimeout).readTimeout(genericConnectorConfiguration.readTimeout).headers(map).proxy((ProxyInfo)this.proxyInfoSupplier.get()).bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(new LoginRequest(paramEventInfo, uUID, paramString1, IdentityTypeHelper.stringify(paramList), paramString2))).successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(LoginResponse.class)).errorResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(Errors.class)).post().go();
    context.stop();
    debugger.log("Endpoint returned status code [" + clientResponse.status + "].");
    if (!clientResponse.wasSuccessful()) {
      if (clientResponse.status == 404) {
        debugger.done();
      } else {
        long l1 = System.currentTimeMillis() - l;
        debugger.handleError(clientResponse, "Request to connector [" + paramBaseConnectorConfiguration.name + "] at the [" + String.valueOf(genericConnectorConfiguration.authenticationURL) + "] endpoint failed when authenticating user with loginId [" + paramString1 + "].\nTotal request duration: [" + l1 + "] ms");
        meter.mark();
      } 
      return null;
    } 
    User user = ((LoginResponse)clientResponse.successResponse).user;
    if (user == null) {
      debugger.logObjectToJSON("Connector User:\n", new User())
        .log("\nWARNING DISCARDING Connector User because the user object was not provided.")
        .done();
      return null;
    } 
    user.identities.clear();
    List<String> list = getUserFailures(user);
    if (!list.isEmpty()) {
      String str = list.stream().map(paramString -> "- " + paramString).collect(Collectors.joining("\n"));
      debugger.logObjectToJSON("Connector User:\n", (new User(user)).secure())
        .log("\nWARNING DISCARDING Connector User due to the following:\n" + str)
        .done();
      return null;
    } 
    debugger.logObjectToJSON("Connector User:\n", (new User(((LoginResponse)clientResponse.successResponse).user)).secure());
    return (new AuthenticationService.AuthenticationResult(AuthenticationType.GENERIC_CONNECTOR, paramBaseConnectorConfiguration.id, ((LoginResponse)clientResponse.successResponse).user))
      .with(paramAuthenticationResult -> paramAuthenticationResult.connector = this)
      .with(paramAuthenticationResult -> paramAuthenticationResult.connectorConfiguration = paramBaseConnectorConfiguration)
      .with(paramAuthenticationResult -> paramAuthenticationResult.connectorPolicy = paramConnectorPolicy)
      .with(paramAuthenticationResult -> paramAuthenticationResult.debugger = paramDebugger);
  }
  
  private String extractSSLCertificate(GenericConnectorConfiguration paramGenericConnectorConfiguration) {
    return (paramGenericConnectorConfiguration.sslCertificateKeyId != null) ? ((Key)this.keyCache.get(paramGenericConnectorConfiguration.sslCertificateKeyId)).certificate : null;
  }
  
  private List<String> getUserFailures(User paramUser) {
    ArrayList<String> arrayList = new ArrayList();
    if (paramUser.id == null)
      arrayList.add("missing a unique id in [user.id]"); 
    if (StringTools.isTrimmedEmpty(paramUser.email) && 
      StringTools.isTrimmedEmpty(paramUser.username) && 
      StringTools.isTrimmedEmpty(paramUser.phoneNumber))
      arrayList.add("missing enough information to store in FusionAuth (i.e. an email, phoneNumber, or username)"); 
    return arrayList;
  }
}
