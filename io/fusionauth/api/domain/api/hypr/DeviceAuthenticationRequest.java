package io.fusionauth.api.domain.api.hypr;

public class DeviceAuthenticationRequest {
  public String actionId;
  
  public String appId;
  
  public String clientType;
  
  public String deviceId;
  
  public String deviceNonce;
  
  public String machine = "WEB";
  
  public String machineId;
  
  public String namedUser;
  
  public String serviceHmac;
  
  public String serviceNonce;
  
  public String sessionNonce;
  
  public DeviceAuthenticationRequest(String paramString1, String paramString2) {
    this.appId = paramString1;
    this.namedUser = paramString2;
    this.sessionNonce = HYPRUtils.nonce();
    this.deviceNonce = HYPRUtils.nonce();
    this.serviceHmac = HYPRUtils.nonce();
    this.serviceNonce = HYPRUtils.nonce();
  }
}
