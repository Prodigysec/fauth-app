package io.fusionauth.api.service.samlv2;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.User;
import io.fusionauth.samlv2.domain.Assertion;
import io.fusionauth.samlv2.domain.AuthenticationRequest;
import io.fusionauth.samlv2.domain.AuthenticationResponse;
import io.fusionauth.samlv2.domain.Binding;
import io.fusionauth.samlv2.domain.LogoutRequest;
import io.fusionauth.samlv2.domain.LogoutResponse;
import io.fusionauth.samlv2.domain.SAMLException;
import io.fusionauth.samlv2.domain.SAMLRequest;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

public interface SAMLv2ProviderService {
  String buildIdPMetaDataResponse(UUID paramUUID, String paramString) throws SAMLException;
  
  String buildPostLogoutResponse(FusionAuthLogoutResponse paramFusionAuthLogoutResponse) throws SAMLException;
  
  String buildRedirectLogoutResponse(FusionAuthLogoutResponse paramFusionAuthLogoutResponse, String paramString) throws SAMLException;
  
  String buildResponse(FusionAuthAuthenticationResponse paramFusionAuthAuthenticationResponse) throws SAMLException;
  
  String buildSPMetaDataResponse(UUID paramUUID, String paramString) throws SAMLException;
  
  Map<UUID, URI> getSessionParticipantLogoutURLs(UUID paramUUID, Application paramApplication, String paramString) throws SAMLException;
  
  void logSAMLRequest(boolean paramBoolean, Binding paramBinding, String paramString1, SAMLRequest paramSAMLRequest, String paramString2);
  
  void logSAMLResponse(boolean paramBoolean, Binding paramBinding, String paramString1, SAMLRequest paramSAMLRequest, String paramString2);
  
  AuthenticationRequest parseAuthNPostRequest(UUID paramUUID, String paramString) throws SAMLException;
  
  AuthenticationRequest parseAuthNRedirectRequest(UUID paramUUID, String paramString) throws SAMLException;
  
  LogoutRequest parseLogoutPostRequest(UUID paramUUID, String paramString) throws SAMLException;
  
  LogoutRequest parseLogoutRedirectRequest(UUID paramUUID, String paramString) throws SAMLException;
  
  void populateErrorResponse(FusionAuthAuthenticationResponse paramFusionAuthAuthenticationResponse, AuthenticationRequest paramAuthenticationRequest, String paramString);
  
  void populateResponse(FusionAuthAuthenticationResponse paramFusionAuthAuthenticationResponse, AuthenticationRequest paramAuthenticationRequest, String paramString, User paramUser);
  
  String resolveACS(Application paramApplication, AuthenticationRequest paramAuthenticationRequest);
  
  String resolveACSDuringIdPInitiatedLoginRequest(Application paramApplication, String paramString1, String paramString2);
  
  Application resolveApplication(UUID paramUUID, String paramString);
  
  FusionAuthAuthenticationResponse validateAuthnRequest(SAMLRequest paramSAMLRequest, UUID paramUUID);
  
  FusionAuthAuthenticationResponse validateInitiatedLoginRequest(Application paramApplication, String paramString1, String paramString2);
  
  FusionAuthLogoutResponse validateLogoutRequest(SAMLRequest paramSAMLRequest, UUID paramUUID);
  
  String validateRedirectBinding(String paramString1, String paramString2);
  
  public static class FusionAuthAuthenticationResponse extends AuthenticationResponse {
    public Application application;
    
    public Assertion assertion;
  }
  
  public static class FusionAuthLogoutResponse extends LogoutResponse {
    public Application application;
  }
  
  public static class SAMLv2State implements Buildable<SAMLv2State> {
    public String acs;
    
    public UUID ai;
    
    public String atc;
    
    public String csrf;
    
    public String id;
    
    public String nf;
    
    public String rs;
    
    @JacksonConstructor
    public SAMLv2State() {}
    
    public SAMLv2State(String param1String1, UUID param1UUID, String param1String2, String param1String3, String param1String4, String param1String5) {
      this.acs = param1String1;
      this.ai = param1UUID;
      this.id = param1String2;
      this.nf = param1String3;
      this.rs = param1String4;
      this.csrf = param1String5;
    }
  }
}
