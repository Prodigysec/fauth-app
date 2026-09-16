package io.fusionauth.api.service.identity;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.JSONResponseHandler;
import com.inversoft.rest.ProxyInfo;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.rest.RESTClient;
import com.inversoft.rest.TextResponseHandler;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.IdentityProviderMapper;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.system.eventLog.Debugger;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderOauth2Configuration;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.provider.OpenIdConnectIdentityProvider;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public class OpenIdConnectIdentityProviderValidator implements IdentityProviderValidator {
  private final IdentityProviderMapper identityProviderMapper;
  
  private final ProxyInfoSupplier proxyInfoSupplier;
  
  @Inject
  public OpenIdConnectIdentityProviderValidator(IdentityProviderMapper paramIdentityProviderMapper, ProxyInfoSupplier paramProxyInfoSupplier) {
    this.identityProviderMapper = paramIdentityProviderMapper;
    this.proxyInfoSupplier = paramProxyInfoSupplier;
  }
  
  public Errors validate(BaseIdentityProvider<?> paramBaseIdentityProvider1, BaseIdentityProvider<?> paramBaseIdentityProvider2, boolean paramBoolean) {
    OpenIdConnectIdentityProvider openIdConnectIdentityProvider = (OpenIdConnectIdentityProvider)paramBaseIdentityProvider1;
    Object object = openIdConnectIdentityProvider.domains.isEmpty() ? Collections.emptyList() : this.identityProviderMapper.retrieveExistingDomains(openIdConnectIdentityProvider.domains, openIdConnectIdentityProvider.id, paramBaseIdentityProvider1.tenantId);
    return (new Validator())
      
      .ifFalse((openIdConnectIdentityProvider.oauth2.issuer == null), paramValidator -> paramValidator.validate(()))


      
      .ifTrue((openIdConnectIdentityProvider.oauth2.issuer == null), paramValidator -> paramValidator.notBlank(paramOpenIdConnectIdentityProvider.oauth2.authorization_endpoint, "identityProvider.oauth2.authorization_endpoint", new Object[] { IdentityProviderType.OpenIDConnect.name() }).ifLastCheckHadNoError(()).notBlank(paramOpenIdConnectIdentityProvider.oauth2.token_endpoint, "identityProvider.oauth2.token_endpoint", new Object[] { IdentityProviderType.OpenIDConnect.name() }).ifLastCheckHadNoError(()).notBlank(paramOpenIdConnectIdentityProvider.oauth2.userinfo_endpoint, "identityProvider.oauth2.userinfo_endpoint", new Object[] { IdentityProviderType.OpenIDConnect.name() }).ifLastCheckHadNoError(())).ifTrue((openIdConnectIdentityProvider.oauth2.clientAuthenticationMethod != IdentityProviderOauth2Configuration.ClientAuthenticationMethod.none), paramValidator -> paramValidator.notMissing(paramOpenIdConnectIdentityProvider.oauth2.client_secret, "identityProvider.oauth2.client_secret", new Object[] { IdentityProviderType.OpenIDConnect.name() })).notBlank(openIdConnectIdentityProvider.oauth2.client_id, "identityProvider.oauth2.client_id", new Object[] { IdentityProviderType.OpenIDConnect.name() }).notBlank(openIdConnectIdentityProvider.buttonText, "identityProvider.buttonText", new Object[] { IdentityProviderType.OpenIDConnect.name() }).ifFalse(paramBoolean, paramValidator -> paramValidator.notBlank(paramOpenIdConnectIdentityProvider.oauth2.emailClaim, "identityProvider.oauth2.emailClaim", new Object[0]))

      
      .ifTrue((openIdConnectIdentityProvider.domains.size() > 0), paramValidator -> paramValidator.emptyWithCode(paramList, "identityProvider.domains", "[duplicate]identityProvider.domains", new Object[] { String.join(", ", paramList) })).done();
  }
  
  private void validateIssuer(URI paramURI, Validator paramValidator) {
    ClientResponse<?, ?> clientResponse = (new RESTClient(JsonNode.class, String.class)).url(paramURI.toString() + "/.well-known/openid-configuration").connectTimeout(2000).readTimeout(5000).proxy((ProxyInfo)this.proxyInfoSupplier.get()).successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(JsonNode.class)).errorResponseHandler((RESTClient.ResponseHandler)new TextResponseHandler()).get().go();
    if (!clientResponse.wasSuccessful())
      EventLogHelper.create(new EventLog(EventLogType.Debug, Debugger.buildMessageFromResponse("Unable to resolve OpenID Connect configuration using issuer [" + String.valueOf(paramURI) + "]\nRequest to the [" + String.valueOf(paramURI) + "/.well-known/openid-configuration] endpoint failed.", clientResponse))); 
    paramValidator.valid(clientResponse.wasSuccessful(), "identityProvider.oauth2.issuer", new Object[0])
      .ifLastCheckHadNoError(paramValidator -> paramValidator.ensureWithCode(((JsonNode)paramClientResponse.successResponse).at("/authorization_endpoint").isTextual(), "identityProvider.oauth2.issuer", "[missing]identityProvider.oauth2.authorization_endpoint", new Object[] { paramURI }).ensureWithCode(((JsonNode)paramClientResponse.successResponse).at("/token_endpoint").isTextual(), "identityProvider.oauth2.issuer", "[missing]identityProvider.oauth2.token_endpoint", new Object[] { paramURI }).ensureWithCode(((JsonNode)paramClientResponse.successResponse).at("/userinfo_endpoint").isTextual(), "identityProvider.oauth2.issuer", "[missing]identityProvider.oauth2.userinfo_endpoint", new Object[] { paramURI }));
  }
}
