package io.fusionauth.domain.jwt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.JWTConfiguration;
import io.fusionauth.domain.RefreshTokenExpirationPolicy;
import io.fusionauth.domain.Tenant;
import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class RefreshToken implements Buildable<RefreshToken>, JSONColumnable {
  public UUID applicationId;
  
  public Map<String, Object> data = new LinkedHashMap<>();
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  @JSONColumn
  public MetaData metaData = new MetaData();
  
  public ZonedDateTime startInstant;
  
  public UUID tenantId;
  
  public String token;
  
  public UUID userId;
  
  @JacksonConstructor
  public RefreshToken() {}
  
  public RefreshToken(RefreshToken paramRefreshToken) {
    this.applicationId = paramRefreshToken.applicationId;
    if (paramRefreshToken.data != null)
      this.data.putAll(paramRefreshToken.data); 
    this.id = paramRefreshToken.id;
    this.insertInstant = paramRefreshToken.insertInstant;
    this.metaData = new MetaData(paramRefreshToken.metaData);
    this.startInstant = paramRefreshToken.startInstant;
    this.tenantId = paramRefreshToken.tenantId;
    this.token = paramRefreshToken.token;
    this.userId = paramRefreshToken.userId;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof RefreshToken))
      return false; 
    RefreshToken refreshToken = (RefreshToken)paramObject;
    return (Objects.equals(this.applicationId, refreshToken.applicationId) && 
      Objects.equals(this.data, refreshToken.data) && 
      Objects.equals(this.id, refreshToken.id) && 
      Objects.equals(this.insertInstant, refreshToken.insertInstant) && 
      Objects.equals(this.metaData, refreshToken.metaData) && 
      Objects.equals(this.startInstant, refreshToken.startInstant) && 
      Objects.equals(this.tenantId, refreshToken.tenantId) && 
      Objects.equals(this.token, refreshToken.token) && 
      Objects.equals(this.userId, refreshToken.userId));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.applicationId, this.data, this.id, this.insertInstant, this.metaData, this.startInstant, this.tenantId, this.token, this.userId });
  }
  
  @JsonIgnore
  public boolean isExpired(Tenant paramTenant, Application paramApplication) {
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    if (this.tenantId != null)
      return this.startInstant.plusSeconds(paramTenant.httpSessionMaxInactiveInterval).isBefore(zonedDateTime); 
    JWTConfiguration jWTConfiguration = (paramApplication != null && paramApplication.jwtConfiguration != null && paramApplication.jwtConfiguration.enabled) ? paramApplication.jwtConfiguration : paramTenant.jwtConfiguration;
    if (this.startInstant.plusMinutes(jWTConfiguration.refreshTokenTimeToLiveInMinutes).isBefore(zonedDateTime))
      return true; 
    return (jWTConfiguration.refreshTokenExpirationPolicy == RefreshTokenExpirationPolicy.SlidingWindowWithMaximumLifetime && this.insertInstant
      .plusMinutes(jWTConfiguration.refreshTokenSlidingWindowConfiguration.maximumTimeToLiveInMinutes).isBefore(zonedDateTime));
  }
  
  public RefreshToken secure() {
    this.data = null;
    return this;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  public static class MetaData implements Buildable<MetaData> {
    public Map<String, Object> data;
    
    public DeviceInfo device = new DeviceInfo();
    
    public List<URI> resources;
    
    @JsonDeserialize(as = LinkedHashSet.class)
    public Set<String> scopes;
    
    @JacksonConstructor
    public MetaData() {}
    
    public MetaData(MetaData param1MetaData) {
      if (param1MetaData.data != null)
        this.data = new LinkedHashMap<>(param1MetaData.data); 
      this.device = new DeviceInfo(param1MetaData.device);
      if (param1MetaData.resources != null)
        this.resources = new ArrayList<>(param1MetaData.resources); 
      if (param1MetaData.scopes != null)
        this.scopes = new LinkedHashSet<>(param1MetaData.scopes); 
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (!(param1Object instanceof MetaData))
        return false; 
      MetaData metaData = (MetaData)param1Object;
      return (Objects.equals(this.data, metaData.data) && 
        Objects.equals(this.device, metaData.device) && 
        Objects.equals(this.resources, metaData.resources) && 
        Objects.equals(this.scopes, metaData.scopes));
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { this.data, this.device, this.resources, this.scopes });
    }
    
    public String toString() {
      return ToString.toString(this);
    }
  }
}
