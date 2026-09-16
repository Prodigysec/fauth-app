package io.fusionauth.app.action.legacy.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.FormDataBodyHandler;
import com.inversoft.rest.JSONResponseHandler;
import com.inversoft.rest.RESTClient;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.domain.guice.FusionAuthLocalClientURL;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.oauth2.IntrospectResponse;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action
public class IntrospectAction extends BaseLegacyAdapterAction {
  private final String fusionAuthClientURL;
  
  public String client_id;
  
  public String client_secret;
  
  @JSONResponse
  public Object response;
  
  public String token;
  
  public String token_type_hint;
  
  @Inject
  public IntrospectAction(FusionAuthConfiguration paramFusionAuthConfiguration, FusionAuthClientProvider paramFusionAuthClientProvider, HTTPRequest paramHTTPRequest, ReactorStatusService paramReactorStatusService, @FusionAuthLocalClientURL String paramString) {
    super(paramFusionAuthConfiguration, paramFusionAuthClientProvider, paramHTTPRequest, paramReactorStatusService);
    this.fusionAuthClientURL = paramString;
  }
  
  public String post() {
    if (isDisabled())
      return "not-found"; 
    if (isNotLicensed()) {
      this.response = buildNotLicensedError();
      return "input";
    } 
    ObjectMapper objectMapper = FusionAuthClient.objectMapper;
    RESTClient rESTClient = (new RESTClient(IntrospectResponse.class, OAuthError.class)).successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(IntrospectResponse.class, objectMapper)).errorResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(OAuthError.class, objectMapper)).url(this.fusionAuthClientURL).connectTimeout(5000).readTimeout(300000);
    if (this.tenantId != null)
      rESTClient.header(FusionAuthClient.TENANT_ID_HEADER, String.valueOf(this.tenantId)); 
    String str = this.httpRequest.getHeader("Authorization");
    if (str != null && !str.trim().isEmpty())
      rESTClient.header("Authorization", str); 
    rESTClient.bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(this.httpRequest.getParameters()));
    ClientResponse clientResponse = rESTClient.uri("/oauth2/introspect").post().go();
    if (clientResponse.wasSuccessful()) {
      this.response = clientResponse.successResponse;
      return "render";
    } 
    if (clientResponse.status == 401) {
      this.response = clientResponse.errorResponse;
      return "invalid-client";
    } 
    this.response = (clientResponse.errorResponse != null) ? clientResponse.errorResponse : new OAuthError();
    return "input";
  }
}
