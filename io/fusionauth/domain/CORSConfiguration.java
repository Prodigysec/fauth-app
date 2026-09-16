package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.domain.util.HTTPMethod;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class CORSConfiguration extends Enableable implements Buildable<CORSConfiguration> {
  public boolean allowCredentials;
  
  public List<String> allowedHeaders = new ArrayList<>();
  
  public List<HTTPMethod> allowedMethods = new ArrayList<>();
  
  public List<URI> allowedOrigins = new ArrayList<>();
  
  public boolean debug;
  
  public List<String> exposedHeaders = new ArrayList<>();
  
  public int preflightMaxAgeInSeconds;
  
  @JacksonConstructor
  public CORSConfiguration() {}
  
  public CORSConfiguration(CORSConfiguration paramCORSConfiguration) {
    this.allowCredentials = paramCORSConfiguration.allowCredentials;
    this.allowedHeaders.addAll(paramCORSConfiguration.allowedHeaders);
    this.allowedMethods.addAll(paramCORSConfiguration.allowedMethods);
    this.allowedOrigins.addAll(paramCORSConfiguration.allowedOrigins);
    this.debug = paramCORSConfiguration.debug;
    this.enabled = paramCORSConfiguration.enabled;
    this.exposedHeaders.addAll(paramCORSConfiguration.exposedHeaders);
    this.preflightMaxAgeInSeconds = paramCORSConfiguration.preflightMaxAgeInSeconds;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    if (!super.equals(paramObject))
      return false; 
    CORSConfiguration cORSConfiguration = (CORSConfiguration)paramObject;
    return (this.allowCredentials == cORSConfiguration.allowCredentials && this.debug == cORSConfiguration.debug && this.preflightMaxAgeInSeconds == cORSConfiguration.preflightMaxAgeInSeconds && Objects.equals(this.allowedHeaders, cORSConfiguration.allowedHeaders) && Objects.equals(this.allowedMethods, cORSConfiguration.allowedMethods) && Objects.equals(this.allowedOrigins, cORSConfiguration.allowedOrigins) && Objects.equals(this.exposedHeaders, cORSConfiguration.exposedHeaders));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), Boolean.valueOf(this.allowCredentials), this.allowedHeaders, this.allowedMethods, this.allowedOrigins, Boolean.valueOf(this.debug), this.exposedHeaders, Integer.valueOf(this.preflightMaxAgeInSeconds) });
  }
  
  public void normalize() {
    HashSet<HTTPMethod> hashSet = new HashSet<>(this.allowedMethods);
    this.allowedMethods.clear();
    this.allowedMethods.addAll(hashSet);
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
