package io.fusionauth.api.service.connector;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import io.fusionauth.api.domain.UserMapper;
import io.fusionauth.api.domain.api.service.ImmutableLambdaArgument;
import io.fusionauth.api.domain.api.service.LambdaArgument;
import io.fusionauth.api.domain.api.service.MutableLambdaArgument;
import io.fusionauth.api.service.authentication.AuthenticationService;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.email.EmailProxy;
import io.fusionauth.api.service.group.GroupReaderService;
import io.fusionauth.api.service.group.GroupService;
import io.fusionauth.api.service.lambda.LambdaInvocationService;
import io.fusionauth.api.service.reactor.ReactorService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.eventLog.Debugger;
import io.fusionauth.api.service.user.PasswordService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.connector.ConnectorPolicy;
import io.fusionauth.domain.connector.LDAPConnectorConfiguration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.naming.AuthenticationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;

public class LDAPConnector extends BaseExternalConnector {
  private final LambdaInvocationService lambdaInvocationService;
  
  private final MetricRegistry metricRegistry;
  
  @Inject
  public LDAPConnector(EmailProxy paramEmailProxy, PasswordService paramPasswordService, ReactorService paramReactorService, UserMapper paramUserMapper, UserService paramUserService, ApplicationCache paramApplicationCache, ApplicationReaderService paramApplicationReaderService, GroupReaderService paramGroupReaderService, GroupService paramGroupService, UserReaderService paramUserReaderService, LambdaInvocationService paramLambdaInvocationService, MetricRegistry paramMetricRegistry) {
    super(paramEmailProxy, paramPasswordService, paramReactorService, paramUserMapper, paramUserService, paramApplicationCache, paramApplicationReaderService, paramGroupReaderService, paramGroupService, paramUserReaderService);
    this.lambdaInvocationService = paramLambdaInvocationService;
    this.metricRegistry = paramMetricRegistry;
  }
  
  public AuthenticationService.AuthenticationResult authenticate(BaseConnectorConfiguration paramBaseConnectorConfiguration, ConnectorPolicy paramConnectorPolicy, Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, List<IdentityType> paramList, String paramString2, EventInfo paramEventInfo) {
    if (!canHandle(paramConnectorPolicy, paramString1))
      return null; 
    LDAPConnectorConfiguration lDAPConnectorConfiguration = (LDAPConnectorConfiguration)paramBaseConnectorConfiguration;
    UUID uUID = (paramApplication != null) ? paramApplication.id : null;
    Debugger debugger = new Debugger(lDAPConnectorConfiguration.debug, String.valueOf(lDAPConnectorConfiguration.getType()) + " Connector Debug Log for [" + String.valueOf(lDAPConnectorConfiguration.getType()) + "] with Id [" + lDAPConnectorConfiguration.name + "].");
    debugger.log("Attempting authentication request " + (
        (uUID != null) ? ("to application with Id [" + String.valueOf(uUID) + "] ") : "") + (
        (paramEventInfo != null && paramEventInfo.ipAddress != null) ? ("from IP address [" + paramEventInfo.ipAddress + "] ") : "") + "for [" + paramString1 + "] against the URL [" + String.valueOf(lDAPConnectorConfiguration.authenticationURL) + "].");
    AuthenticationService.AuthenticationResult authenticationResult = authenticate(lDAPConnectorConfiguration, paramConnectorPolicy, paramString1, paramString2, debugger);
    if (authenticationResult == null)
      debugger.done(); 
    return authenticationResult;
  }
  
  private AuthenticationService.AuthenticationResult authenticate(LDAPConnectorConfiguration paramLDAPConnectorConfiguration, ConnectorPolicy paramConnectorPolicy, String paramString1, String paramString2, Debugger paramDebugger) {
    LDAPConnection lDAPConnection1 = bind(paramLDAPConnectorConfiguration, paramLDAPConnectorConfiguration.systemAccountDN, paramLDAPConnectorConfiguration.systemAccountPassword, paramDebugger);
    if (lDAPConnection1 == null)
      return null; 
    LDAPConnection lDAPConnection2 = null;
    User user = new User();
    long l = System.currentTimeMillis();
    Meter meter = this.metricRegistry.meter("fusionauth.connector.[" + String.valueOf(paramLDAPConnectorConfiguration.id) + "].errors");
    try {
      Timer.Context context = this.metricRegistry.timer("fusionauth.connector.[" + String.valueOf(paramLDAPConnectorConfiguration.id) + "].requests").time();
      NamingEnumeration<?> namingEnumeration = searchDIT(paramLDAPConnectorConfiguration, lDAPConnection1.ctx, paramString1, paramDebugger);
      if (namingEnumeration != null && namingEnumeration.hasMore()) {
        SearchResult searchResult = (SearchResult)namingEnumeration.next();
        String str = searchResult.getNameInNamespace();
        lDAPConnection2 = bind(paramLDAPConnectorConfiguration, str, paramString2, paramDebugger);
        context.stop();
        if (lDAPConnection2 == null)
          return null; 
        Attributes attributes = lDAPConnection2.ctx.getAttributes(str, paramLDAPConnectorConfiguration.requestedAttributes.<String>toArray(new String[0]));
        Map<String, Object> map = getAllAttributes(attributes);
        paramDebugger.logObjectToJSON("Invoking lambda with Id [" + String.valueOf(paramLDAPConnectorConfiguration.lambdaConfiguration.reconcileId) + "] with attributes returned from connector:\n", map);
        this.lambdaInvocationService.invoke(paramLDAPConnectorConfiguration.lambdaConfiguration.reconcileId, new LambdaArgument[] { new MutableLambdaArgument(user), new ImmutableLambdaArgument(map) });
        user.identities.clear();
      } else {
        paramDebugger.log("The search did not yield any results.");
        return null;
      } 
      paramDebugger.logObjectToJSON("Resolved Connector User:\n", (new User(user)).secure().sort());
      if (user.id == null) {
        paramDebugger.log("Warning, discarding user because it was missing a unique id in the [user.id] property.");
        return null;
      } 
      return (new AuthenticationService.AuthenticationResult(AuthenticationType.LDAP_CONNECTOR, paramLDAPConnectorConfiguration.id, user))
        .with(paramAuthenticationResult -> paramAuthenticationResult.connector = this)
        .with(paramAuthenticationResult -> paramAuthenticationResult.connectorConfiguration = paramLDAPConnectorConfiguration)
        .with(paramAuthenticationResult -> paramAuthenticationResult.connectorPolicy = paramConnectorPolicy)
        .with(paramAuthenticationResult -> paramAuthenticationResult.debugger = paramDebugger);
    } catch (NamingException namingException) {
      long l1 = System.currentTimeMillis() - l;
      handleLDAPError(namingException, "Search request to connector [" + paramLDAPConnectorConfiguration.name + "] at [" + String.valueOf(paramLDAPConnectorConfiguration.authenticationURL) + "] failed while searching for a user with loginId [" + paramString1 + "].\nTotal request duration: [" + l1 + "] ms\n", paramDebugger);
      meter.mark();
      return null;
    } finally {
      try {
        paramDebugger.log("Closing the LDAP connection for system DN [" + paramLDAPConnectorConfiguration.systemAccountDN + "].");
        lDAPConnection1.close();
        if (lDAPConnection2 != null) {
          paramDebugger.log("Closing the LDAP connection for user authentication.");
          lDAPConnection2.close();
        } 
      } catch (IOException|NamingException iOException) {
        handleLDAPError(iOException, "Exception occurred while closing the LDAP connection.\n", paramDebugger);
      } 
    } 
  }
  
  private LDAPConnection bind(LDAPConnectorConfiguration paramLDAPConnectorConfiguration, String paramString1, String paramString2, Debugger paramDebugger) {
    Properties properties = buildBindEnv(paramLDAPConnectorConfiguration, paramString1, paramString2, paramDebugger);
    try {
      paramDebugger.log("Attempting bind against DN [" + paramString1 + "].");
      InitialLdapContext initialLdapContext = new InitialLdapContext(properties, null);
      paramDebugger.log("Bind against DN [" + paramString1 + "] was successful.");
      StartTlsResponse startTlsResponse = null;
      if (paramLDAPConnectorConfiguration.securityMethod == LDAPConnectorConfiguration.LDAPSecurityMethod.StartTLS) {
        paramDebugger.log("Initiate the StartTLS request.");
        startTlsResponse = (StartTlsResponse)initialLdapContext.extendedOperation(new StartTlsRequest());
        paramDebugger.log("Negotiate the TLS handshake.");
        startTlsResponse.negotiate();
        initialLdapContext.addToEnvironment("java.naming.security.authentication", "simple");
        initialLdapContext.addToEnvironment("java.naming.security.principal", paramString1);
        initialLdapContext.addToEnvironment("java.naming.security.credentials", paramString2);
      } 
      return new LDAPConnection(initialLdapContext, startTlsResponse);
    } catch (AuthenticationException authenticationException) {
      handleLDAPError(authenticationException, "Bind request to connector [" + paramLDAPConnectorConfiguration.name + "] at [" + String.valueOf(paramLDAPConnectorConfiguration.authenticationURL) + "] failed authentication for the system account DN [" + paramLDAPConnectorConfiguration.systemAccountDN + "].\n", paramDebugger);
    } catch (NamingException namingException) {
      handleLDAPError(namingException, "Bind request to connector [" + paramLDAPConnectorConfiguration.name + "] at [" + String.valueOf(paramLDAPConnectorConfiguration.authenticationURL) + "] failed for a user with DN [" + paramString1 + "].\n", paramDebugger);
    } catch (IOException iOException) {
      handleLDAPError(iOException, "Bind request to connector [" + paramLDAPConnectorConfiguration.name + "] at [" + String.valueOf(paramLDAPConnectorConfiguration.authenticationURL) + "] failed for a user with DN [" + paramString1 + "] during the StartTLS negotiation.\n", paramDebugger);
    } 
    return null;
  }
  
  private Properties buildBindEnv(LDAPConnectorConfiguration paramLDAPConnectorConfiguration, String paramString1, String paramString2, Debugger paramDebugger) {
    Properties properties = new Properties();
    properties.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
    properties.put("java.naming.provider.url", paramLDAPConnectorConfiguration.authenticationURL.toString());
    properties.put("java.naming.security.authentication", "simple");
    if (paramLDAPConnectorConfiguration.securityMethod == LDAPConnectorConfiguration.LDAPSecurityMethod.LDAPS) {
      paramDebugger.log("Connect to [" + String.valueOf(paramLDAPConnectorConfiguration.authenticationURL) + "] using SSL security protocol.");
      properties.put("java.naming.security.protocol", "ssl");
    } 
    properties.put("com.sun.jndi.ldap.read.timeout", String.valueOf(paramLDAPConnectorConfiguration.readTimeout));
    properties.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(paramLDAPConnectorConfiguration.connectTimeout));
    properties.put("java.naming.security.principal", paramString1);
    properties.put("java.naming.security.credentials", paramString2);
    return properties;
  }
  
  private Map<String, Object> getAllAttributes(Attributes paramAttributes) throws NamingException {
    HashMap<Object, Object> hashMap = new HashMap<>();
    for (NamingEnumeration<? extends Attribute> namingEnumeration = paramAttributes.getAll(); namingEnumeration.hasMore(); ) {
      Attribute attribute = namingEnumeration.next();
      if (attribute.size() > 1) {
        ArrayList arrayList = new ArrayList();
        for (NamingEnumeration<?> namingEnumeration1 = attribute.getAll(); namingEnumeration1.hasMore();)
          arrayList.add(namingEnumeration1.next()); 
        hashMap.put(attribute.getID(), arrayList);
        continue;
      } 
      hashMap.put(attribute.getID(), attribute.get());
    } 
    return (Map)hashMap;
  }
  
  private void handleLDAPError(Exception paramException, String paramString, Debugger paramDebugger) {
    paramDebugger.log("The response was not successful, see the error event log.")
      .done();
    handleException(paramException, paramString);
  }
  
  private NamingEnumeration<?> searchDIT(LDAPConnectorConfiguration paramLDAPConnectorConfiguration, LdapContext paramLdapContext, String paramString, Debugger paramDebugger) throws NamingException {
    String[] arrayOfString = { paramLDAPConnectorConfiguration.identifyingAttribute };
    SearchControls searchControls = new SearchControls();
    searchControls.setReturningAttributes(arrayOfString);
    searchControls.setSearchScope(2);
    searchControls.setTimeLimit(paramLDAPConnectorConfiguration.readTimeout);
    String str = "(" + paramLDAPConnectorConfiguration.loginIdAttribute + "=" + paramString + ")";
    paramDebugger.log("Attempting search with filter [" + str + "].");
    return paramLdapContext.search(paramLDAPConnectorConfiguration.baseStructure, str, searchControls);
  }
  
  private static class LDAPConnection {
    public LdapContext ctx;
    
    public StartTlsResponse startTLSResponse;
    
    public LDAPConnection(LdapContext param1LdapContext, StartTlsResponse param1StartTlsResponse) {
      this.ctx = param1LdapContext;
      this.startTLSResponse = param1StartTlsResponse;
    }
    
    public void close() throws IOException, NamingException {
      if (this.startTLSResponse != null)
        this.startTLSResponse.close(); 
      this.ctx.close();
    }
  }
  
  static {
    System.setProperty("com.sun.jndi.ldap.object.trustSerialData", "false");
    System.setProperty("jdk.jndi.object.factoriesFilter", "!*");
  }
}
