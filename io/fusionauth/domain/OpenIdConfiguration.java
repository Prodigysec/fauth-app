package io.fusionauth.domain;

import com.inversoft.json.ToString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class OpenIdConfiguration implements Buildable<OpenIdConfiguration> {
  public String authorization_endpoint = "%s/oauth2/authorize";
  
  public boolean authorization_response_iss_parameter_supported = true;
  
  public boolean backchannel_logout_supported = false;
  
  public List<String> claims_supported = new ArrayList<>(Arrays.asList(new String[] { 
          "applicationId", "at_hash", "aud", "authenticationType", "birthdate", "c_hash", "email", "email_verified", "exp", "family_name", 
          "given_name", "iat", "iss", "jti", "middle_name", "name", "nbf", "nonce", "phone_number", "phone_number_verified", 
          "picture", "preferred_username", "roles", "sub" }));
  
  public List<String> code_challenge_methods_supported = new ArrayList<>(Collections.singletonList("S256"));
  
  public String device_authorization_endpoint = "%s/oauth2/device_authorize";
  
  public List<String> dpop_signing_alg_values_supported = new ArrayList<>(Arrays.asList(new String[] { 
          "Ed448", "Ed25519", "ES256", "ES384", "ES512", "PS256", "PS384", "PS512", "RS256", "RS384", 
          "RS512" }));
  
  public String end_session_endpoint = "%s/oauth2/logout";
  
  public boolean frontchannel_logout_supported = true;
  
  public List<String> grant_types_supported = new ArrayList<>(Arrays.asList(new String[] { "authorization_code", "password", "implicit", "refresh_token", "urn:ietf:params:oauth:grant-type:device_code", "client_credentials" }));
  
  public List<String> id_token_signing_alg_values_supported = new ArrayList<>(Arrays.asList(new String[] { "ES256", "ES384", "ES512", "HS256", "HS384", "HS512", "RS256", "RS384", "RS512" }));
  
  public String issuer;
  
  public String jwks_uri = "%s/.well-known/jwks.json";
  
  public List<String> response_modes_supported = new ArrayList<>(Arrays.asList(new String[] { "form_post", "fragment", "query" }));
  
  public List<String> response_types_supported = new ArrayList<>(Arrays.asList(new String[] { "code", "id_token", "token id_token" }));
  
  public List<String> scopes_supported = new ArrayList<>(Arrays.asList(new String[] { "openid", "offline_access", "email", "phone", "profile" }));
  
  public List<String> subject_types_supported = new ArrayList<>(Collections.singletonList("public"));
  
  public String token_endpoint = "%s/oauth2/token";
  
  public List<String> token_endpoint_auth_methods_supported = new ArrayList<>(Arrays.asList(new String[] { "client_secret_basic", "client_secret_post", "none" }));
  
  public String userinfo_endpoint = "%s/oauth2/userinfo";
  
  public List<String> userinfo_signing_alg_values_supported = new ArrayList<>(Arrays.asList(new String[] { "ES256", "ES384", "ES512", "RS256", "RS384", "RS512", "HS256", "HS384", "HS512" }));
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof OpenIdConfiguration))
      return false; 
    OpenIdConfiguration openIdConfiguration = (OpenIdConfiguration)paramObject;
    return (this.authorization_response_iss_parameter_supported == openIdConfiguration.authorization_response_iss_parameter_supported && this.backchannel_logout_supported == openIdConfiguration.backchannel_logout_supported && this.frontchannel_logout_supported == openIdConfiguration.frontchannel_logout_supported && 

      
      Objects.equals(this.authorization_endpoint, openIdConfiguration.authorization_endpoint) && 
      Objects.equals(this.claims_supported, openIdConfiguration.claims_supported) && 
      Objects.equals(this.code_challenge_methods_supported, openIdConfiguration.code_challenge_methods_supported) && 
      Objects.equals(this.device_authorization_endpoint, openIdConfiguration.device_authorization_endpoint) && 
      Objects.equals(this.dpop_signing_alg_values_supported, openIdConfiguration.dpop_signing_alg_values_supported) && 
      Objects.equals(this.end_session_endpoint, openIdConfiguration.end_session_endpoint) && 
      Objects.equals(this.grant_types_supported, openIdConfiguration.grant_types_supported) && 
      Objects.equals(this.id_token_signing_alg_values_supported, openIdConfiguration.id_token_signing_alg_values_supported) && 
      Objects.equals(this.issuer, openIdConfiguration.issuer) && 
      Objects.equals(this.jwks_uri, openIdConfiguration.jwks_uri) && 
      Objects.equals(this.response_modes_supported, openIdConfiguration.response_modes_supported) && 
      Objects.equals(this.response_types_supported, openIdConfiguration.response_types_supported) && 
      Objects.equals(this.scopes_supported, openIdConfiguration.scopes_supported) && 
      Objects.equals(this.subject_types_supported, openIdConfiguration.subject_types_supported) && 
      Objects.equals(this.token_endpoint, openIdConfiguration.token_endpoint) && 
      Objects.equals(this.token_endpoint_auth_methods_supported, openIdConfiguration.token_endpoint_auth_methods_supported) && 
      Objects.equals(this.userinfo_endpoint, openIdConfiguration.userinfo_endpoint) && 
      Objects.equals(this.userinfo_signing_alg_values_supported, openIdConfiguration.userinfo_signing_alg_values_supported));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.authorization_endpoint, Boolean.valueOf(this.authorization_response_iss_parameter_supported), Boolean.valueOf(this.backchannel_logout_supported), this.claims_supported, this.code_challenge_methods_supported, this.device_authorization_endpoint, this.dpop_signing_alg_values_supported, this.end_session_endpoint, Boolean.valueOf(this.frontchannel_logout_supported), this.grant_types_supported, 
          this.id_token_signing_alg_values_supported, this.issuer, this.jwks_uri, this.response_modes_supported, this.response_types_supported, this.scopes_supported, this.subject_types_supported, this.token_endpoint, this.token_endpoint_auth_methods_supported, this.userinfo_endpoint, 
          this.userinfo_signing_alg_values_supported });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
