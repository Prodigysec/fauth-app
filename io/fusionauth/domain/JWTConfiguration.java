package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.ExcludeFromJSONColumn;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class JWTConfiguration extends Enableable implements Buildable<JWTConfiguration> {
  @ExcludeFromJSONColumn
  public UUID accessTokenKeyId;
  
  @ExcludeFromJSONColumn
  public List<UUID> accessTokenVerificationKeyIds = new ArrayList<>();
  
  @ExcludeFromJSONColumn
  public UUID idTokenKeyId;
  
  @ExcludeFromJSONColumn
  public List<UUID> idTokenVerificationKeyIds = new ArrayList<>();
  
  public RefreshTokenExpirationPolicy refreshTokenExpirationPolicy = RefreshTokenExpirationPolicy.Fixed;
  
  public RefreshTokenOneTimeUseConfiguration refreshTokenOneTimeUseConfiguration = new RefreshTokenOneTimeUseConfiguration();
  
  public RefreshTokenRevocationPolicy refreshTokenRevocationPolicy = new RefreshTokenRevocationPolicy(true, true);
  
  public RefreshTokenSlidingWindowConfiguration refreshTokenSlidingWindowConfiguration = new RefreshTokenSlidingWindowConfiguration();
  
  public int refreshTokenTimeToLiveInMinutes = 43200;
  
  public RefreshTokenUsagePolicy refreshTokenUsagePolicy = RefreshTokenUsagePolicy.Reusable;
  
  public int timeToLiveInSeconds = 3600;
  
  @JacksonConstructor
  public JWTConfiguration() {}
  
  public JWTConfiguration(JWTConfiguration paramJWTConfiguration) {
    this.accessTokenKeyId = paramJWTConfiguration.accessTokenKeyId;
    this.accessTokenVerificationKeyIds.addAll(paramJWTConfiguration.accessTokenVerificationKeyIds);
    this.enabled = paramJWTConfiguration.enabled;
    this.idTokenKeyId = paramJWTConfiguration.idTokenKeyId;
    this.idTokenVerificationKeyIds.addAll(paramJWTConfiguration.idTokenVerificationKeyIds);
    this.refreshTokenExpirationPolicy = paramJWTConfiguration.refreshTokenExpirationPolicy;
    this.refreshTokenRevocationPolicy = new RefreshTokenRevocationPolicy(paramJWTConfiguration.refreshTokenRevocationPolicy);
    this.refreshTokenOneTimeUseConfiguration = new RefreshTokenOneTimeUseConfiguration(paramJWTConfiguration.refreshTokenOneTimeUseConfiguration);
    this.refreshTokenSlidingWindowConfiguration = new RefreshTokenSlidingWindowConfiguration(paramJWTConfiguration.refreshTokenSlidingWindowConfiguration);
    this.refreshTokenTimeToLiveInMinutes = paramJWTConfiguration.refreshTokenTimeToLiveInMinutes;
    this.refreshTokenUsagePolicy = paramJWTConfiguration.refreshTokenUsagePolicy;
    this.timeToLiveInSeconds = paramJWTConfiguration.timeToLiveInSeconds;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    if (!super.equals(paramObject))
      return false; 
    JWTConfiguration jWTConfiguration = (JWTConfiguration)paramObject;
    return (Objects.equals(this.accessTokenKeyId, jWTConfiguration.accessTokenKeyId) && 
      Objects.equals(this.accessTokenVerificationKeyIds, jWTConfiguration.accessTokenVerificationKeyIds) && 
      Objects.equals(this.idTokenKeyId, jWTConfiguration.idTokenKeyId) && 
      Objects.equals(this.idTokenVerificationKeyIds, jWTConfiguration.idTokenVerificationKeyIds) && this.refreshTokenExpirationPolicy == jWTConfiguration.refreshTokenExpirationPolicy && 
      
      Objects.equals(this.refreshTokenRevocationPolicy, jWTConfiguration.refreshTokenRevocationPolicy) && 
      Objects.equals(this.refreshTokenOneTimeUseConfiguration, jWTConfiguration.refreshTokenOneTimeUseConfiguration) && 
      Objects.equals(this.refreshTokenSlidingWindowConfiguration, jWTConfiguration.refreshTokenSlidingWindowConfiguration) && this.refreshTokenTimeToLiveInMinutes == jWTConfiguration.refreshTokenTimeToLiveInMinutes && this.refreshTokenUsagePolicy == jWTConfiguration.refreshTokenUsagePolicy && this.timeToLiveInSeconds == jWTConfiguration.timeToLiveInSeconds);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          Integer.valueOf(super.hashCode()), this.accessTokenKeyId, this.accessTokenVerificationKeyIds, this.idTokenKeyId, this.idTokenVerificationKeyIds, this.refreshTokenExpirationPolicy, this.refreshTokenRevocationPolicy, this.refreshTokenOneTimeUseConfiguration, this.refreshTokenSlidingWindowConfiguration, 







          
          Integer.valueOf(this.refreshTokenTimeToLiveInMinutes), 
          this.refreshTokenUsagePolicy, 
          
          Integer.valueOf(this.timeToLiveInSeconds) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
