package io.fusionauth.app.service.identityProvider;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.service.samlv2.SAMLv2Helper;
import io.fusionauth.api.service.system.KeyReaderService;
import io.fusionauth.api.service.system.eventLog.Debugger;
import io.fusionauth.api.util.ParameterTools;
import io.fusionauth.api.util.XMLTools;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.api.identityProvider.IdentityProviderStartLoginRequest;
import io.fusionauth.domain.api.identityProvider.IdentityProviderStartLoginResponse;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.SAMLv2IdentityProvider;
import io.fusionauth.pem.domain.PEM;
import io.fusionauth.samlv2.domain.Algorithm;
import io.fusionauth.samlv2.domain.AuthenticationRequest;
import io.fusionauth.samlv2.domain.Binding;
import io.fusionauth.samlv2.domain.SAMLException;
import io.fusionauth.samlv2.service.SAMLv2Service;
import io.fusionauth.samlv2.util.SAMLTools;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;

public class SAMLv2IdentityProviderFrontendService implements IdentityProviderFrontendService {
  private final FusionAuthClientProvider fusionAuthClientProvider;
  
  private final KeyReaderService keyReader;
  
  private final SAMLv2Service samlv2Service;
  
  @Inject
  public SAMLv2IdentityProviderFrontendService(FusionAuthClientProvider paramFusionAuthClientProvider, KeyReaderService paramKeyReaderService, SAMLv2Service paramSAMLv2Service) {
    this.fusionAuthClientProvider = paramFusionAuthClientProvider;
    this.keyReader = paramKeyReaderService;
    this.samlv2Service = paramSAMLv2Service;
  }
  
  public IdentityProviderFrontendService.PostDataToExternalIDP buildPostData(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    SAMLv2IdentityProvider sAMLv2IdentityProvider = (SAMLv2IdentityProvider)paramFrontendRequestContext.identityProvider;
    Debugger debugger = new Debugger(sAMLv2IdentityProvider.debug, "SAML v2 IdP AuthN Request Debug Log for [" + sAMLv2IdentityProvider.name + "] [" + String.valueOf(sAMLv2IdentityProvider.id) + "]");
    try {
      debugger.log("Build the AuthN SAML v2 request.");
      AuthenticationRequest authenticationRequest = makeAuthnRequest(paramFrontendRequestContext.fusionAuthURI, sAMLv2IdentityProvider);
      convertLoginPromptToForceAuthn(authenticationRequest, paramFrontendRequestContext);
      storeIDPState(debugger, paramTenant, paramFrontendRequestContext.identityProvider, paramFrontendRequestContext.client_id, paramFrontendRequestContext.connectionTestId, authenticationRequest);
      PrivateKey privateKey = null;
      X509Certificate x509Certificate = null;
      Algorithm algorithm = null;
      String str1 = null;
      if (sAMLv2IdentityProvider.signRequest) {
        Key key = this.keyReader.retrieveById(sAMLv2IdentityProvider.requestSigningKeyId);
        x509Certificate = (X509Certificate)(PEM.decode(key.certificate)).certificate;
        privateKey = (PEM.decode(key.privateKey)).privateKey;
        algorithm = toSAMLAlgorithm(key.algorithm);
        str1 = sAMLv2IdentityProvider.xmlSignatureC14nMethod.getURI();
        authenticationRequest.destination = sAMLv2IdentityProvider.idpEndpoint.toString();
      } 
      authenticationRequest.nameIdFormat = sAMLv2IdentityProvider.nameIdFormat;
      String str2 = this.samlv2Service.buildPostAuthnRequest(authenticationRequest, sAMLv2IdentityProvider.signRequest, privateKey, x509Certificate, algorithm, str1);
      IdentityProviderFrontendService.PostDataToExternalIDP postDataToExternalIDP = new IdentityProviderFrontendService.PostDataToExternalIDP();
      postDataToExternalIDP.uri = sAMLv2IdentityProvider.idpEndpoint;
      postDataToExternalIDP.formData.put("SAMLRequest", str2);
      postDataToExternalIDP.formData.put("RelayState", paramFrontendRequestContext.state);
      if (sAMLv2IdentityProvider.debug)
        debugger.log("AuthN request being sent to the identity provider.")
          .log(sAMLv2IdentityProvider.idpEndpoint.toString())
          .log("Binding: " + Binding.HTTP_POST.toSAMLFormat())
          .log("Encoded request: \n" + str2)
          .log("Relay state: \n" + paramFrontendRequestContext.state)
          .log("Un-encoded XML request:" + XMLTools.prettyPrint(new String(SAMLTools.decode(str2), StandardCharsets.UTF_8))); 
      debugger.done();
      return postDataToExternalIDP;
    } catch (SAMLException sAMLException) {
      debugger.log((Exception)sAMLException)
        .done();
      throw new ErrorException("Invalid AuthRequest construction", sAMLException, new Object[0]);
    } 
  }
  
  public String buildRedirectURI(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    SAMLv2IdentityProvider sAMLv2IdentityProvider = (SAMLv2IdentityProvider)paramFrontendRequestContext.identityProvider;
    Debugger debugger = new Debugger(sAMLv2IdentityProvider.debug, "SAML v2 IdP AuthN Request Debug Log for [" + sAMLv2IdentityProvider.name + "] [" + String.valueOf(sAMLv2IdentityProvider.id) + "]");
    try {
      String str1 = sAMLv2IdentityProvider.idpEndpoint.toString();
      debugger.log("Build the AuthN SAML v2 request.");
      AuthenticationRequest authenticationRequest = makeAuthnRequest(paramFrontendRequestContext.fusionAuthURI, sAMLv2IdentityProvider);
      convertLoginPromptToForceAuthn(authenticationRequest, paramFrontendRequestContext);
      storeIDPState(debugger, paramTenant, paramFrontendRequestContext.identityProvider, paramFrontendRequestContext.client_id, paramFrontendRequestContext.connectionTestId, authenticationRequest);
      PrivateKey privateKey = null;
      Algorithm algorithm = null;
      if (sAMLv2IdentityProvider.signRequest) {
        Key key = this.keyReader.retrieveById(sAMLv2IdentityProvider.requestSigningKeyId);
        privateKey = (PEM.decode(key.privateKey)).privateKey;
        algorithm = toSAMLAlgorithm(key.algorithm);
        authenticationRequest.destination = str1;
      } 
      authenticationRequest.nameIdFormat = sAMLv2IdentityProvider.nameIdFormat;
      String str2 = this.samlv2Service.buildRedirectAuthnRequest(authenticationRequest, paramFrontendRequestContext.state, sAMLv2IdentityProvider.signRequest, privateKey, algorithm);
      if (paramFrontendRequestContext.loginId != null && sAMLv2IdentityProvider.loginHintConfiguration.enabled)
        str2 = str2 + str2; 
      if (sAMLv2IdentityProvider.debug) {
        String str = URLDecoder.decode(str2.substring("SAMLRequest=".length()).split("&")[0], StandardCharsets.UTF_8);
        debugger.log("AuthN request being sent to the identity provider.")
          .log(str1)
          .log("Binding: " + Binding.HTTP_Redirect.toSAMLFormat())
          .log("Query string: " + str2)
          .log("Deflated and encoded request: \n" + str)
          .log("Relay state: \n" + paramFrontendRequestContext.state)
          .log("Un-encoded XML request:" + XMLTools.prettyPrint(new String(SAMLTools.decodeAndInflate(str), StandardCharsets.UTF_8)))
          .done();
      } 
      if (str1.contains("?"))
        return str1 + "&" + str1; 
      return str1 + "?" + str1;
    } catch (SAMLException sAMLException) {
      debugger.log((Exception)sAMLException)
        .done();
      throw new ErrorException("Invalid AuthRequest construction", sAMLException, new Object[0]);
    } 
  }
  
  private void convertLoginPromptToForceAuthn(AuthenticationRequest paramAuthenticationRequest, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    if (paramFrontendRequestContext.prompt != null) {
      Set<String> set = ParameterTools.splitSpaceSeparated(paramFrontendRequestContext.prompt);
      if (set.contains("login"))
        paramAuthenticationRequest.forceAuthn = Boolean.valueOf(true); 
    } 
  }
  
  private AuthenticationRequest makeAuthnRequest(String paramString, SAMLv2IdentityProvider paramSAMLv2IdentityProvider) {
    AuthenticationRequest authenticationRequest = new AuthenticationRequest();
    authenticationRequest.acsURL = SAMLv2Helper.getACS(paramString);
    authenticationRequest.issuer = SAMLv2Helper.getServiceProviderEntityId(paramString, paramSAMLv2IdentityProvider);
    return authenticationRequest;
  }
  
  private void storeIDPState(Debugger paramDebugger, Tenant paramTenant, BaseIdentityProvider<?> paramBaseIdentityProvider, String paramString1, String paramString2, AuthenticationRequest paramAuthenticationRequest) {
    IdentityProviderStartLoginRequest identityProviderStartLoginRequest = (new IdentityProviderStartLoginRequest(UUID.fromString(paramString1), Map.of("samlDestination", paramAuthenticationRequest.acsURL, "samlAudience", paramAuthenticationRequest.issuer), paramBaseIdentityProvider.id)).with(paramIdentityProviderStartLoginRequest -> paramIdentityProviderStartLoginRequest.connectionTestId = paramString);
    paramDebugger.log("Begin AuthN request by calling /api/identity-provider/start to register a SAML v2 request Id.");
    ClientResponse<IdentityProviderStartLoginResponse, Errors> clientResponse = this.fusionAuthClientProvider.get(paramTenant.id).startIdentityProviderLogin(identityProviderStartLoginRequest);
    if (!clientResponse.wasSuccessful()) {
      paramDebugger.log("Failed to complete the IdP Start request.");
      if (clientResponse.exception == null) {
        paramDebugger.log("API returned status code [" + clientResponse.status + "]");
        if (clientResponse.status == 400)
          paramDebugger.logObjectToJSON(clientResponse.errorResponse); 
        paramDebugger.done();
        throw new ErrorException("Unable to begin SAML request");
      } 
      paramDebugger.log("Failed to complete the IdP Start request.")
        .log(clientResponse.exception)
        .done();
      throw new ErrorException("Unable to begin SAML request", clientResponse.exception, new Object[0]);
    } 
    paramDebugger.log("Start request returned request Id [" + ((IdentityProviderStartLoginResponse)clientResponse.successResponse).code + "]");
    paramAuthenticationRequest.id = ((IdentityProviderStartLoginResponse)clientResponse.successResponse).code;
  }
  
  private Algorithm toSAMLAlgorithm(Key.KeyAlgorithm paramKeyAlgorithm) {
    switch (paramKeyAlgorithm) {
      case ES256:
      
      case ES512:
      
      case RS256:
      
      case RS384:
      
      case RS512:
      
    } 
    throw new IllegalStateException("Invalid key used for SAML request signing. This is a validation error that the FusionAuth team needs to fix");
  }
}
