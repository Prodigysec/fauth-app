package io.fusionauth.domain.oauth2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class AccessToken implements OAuthResponse, Buildable<AccessToken> {
  @JsonIgnore
  public String clientId;
  
  @JsonProperty("expires_in")
  public Integer expiresIn;
  
  @JsonProperty("id_token")
  public String idToken;
  
  @JsonIgnore
  public URI redirectURI;
  
  @JsonProperty("refresh_token")
  public String refreshToken;
  
  @JsonProperty("refresh_token_id")
  public UUID refreshTokenId;
  
  public String scope;
  
  @JsonProperty("access_token")
  public String token;
  
  @JsonProperty("token_type")
  public TokenType tokenType;
  
  public UUID userId;
  
  @JsonIgnore
  private ZonedDateTime createInstant;
  
  @JacksonConstructor
  public AccessToken() {}
  
  public AccessToken(String paramString1, String paramString2, Integer paramInteger, URI paramURI, TokenType paramTokenType, UUID paramUUID) {
    this.clientId = paramString2;
    this.createInstant = ZonedDateTime.now(ZoneOffset.UTC);
    this.expiresIn = paramInteger;
    this.redirectURI = paramURI;
    this.token = paramString1;
    this.tokenType = paramTokenType;
    this.userId = paramUUID;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    AccessToken accessToken = (AccessToken)paramObject;
    return (Objects.equals(this.clientId, accessToken.clientId) && 
      Objects.equals(this.expiresIn, accessToken.expiresIn) && 
      Objects.equals(this.idToken, accessToken.idToken) && 
      Objects.equals(this.redirectURI, accessToken.redirectURI) && 
      Objects.equals(this.refreshToken, accessToken.refreshToken) && 
      Objects.equals(this.refreshTokenId, accessToken.refreshTokenId) && 
      Objects.equals(this.scope, accessToken.scope) && 
      Objects.equals(this.token, accessToken.token) && this.tokenType == accessToken.tokenType && 
      
      Objects.equals(this.userId, accessToken.userId) && 
      Objects.equals(this.createInstant, accessToken.createInstant));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.clientId, this.expiresIn, this.idToken, this.redirectURI, this.refreshToken, this.refreshTokenId, this.scope, this.token, this.tokenType, this.userId, 
          this.createInstant });
  }
  
  public void setTokenType(String paramString) {
    this.tokenType = TokenType.fromName(paramString);
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
