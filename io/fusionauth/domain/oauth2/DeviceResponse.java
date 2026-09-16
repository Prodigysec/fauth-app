package io.fusionauth.domain.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inversoft.json.JacksonConstructor;
import java.net.URI;

public class DeviceResponse implements OAuthResponse {
  @JsonProperty("device_code")
  public String deviceCode;
  
  @JsonProperty("expires_in")
  public Integer expiresIn;
  
  @JsonProperty("interval")
  public Integer interval;
  
  @JsonProperty("user_code")
  public String userCode;
  
  @JsonProperty("verification_uri")
  public URI verificationURI;
  
  @JsonProperty("verification_uri_complete")
  public URI verificationURIComplete;
  
  @JacksonConstructor
  public DeviceResponse() {}
  
  public DeviceResponse(String paramString1, Integer paramInteger1, Integer paramInteger2, String paramString2, URI paramURI1, URI paramURI2) {
    this.deviceCode = paramString1;
    this.expiresIn = paramInteger1;
    this.interval = paramInteger2;
    this.userCode = paramString2;
    this.verificationURI = paramURI1;
    this.verificationURIComplete = paramURI2;
  }
}
