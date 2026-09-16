package io.fusionauth.api.service;

public class BlockedIPAddressException extends FusionAuthAccessDeniedException {
  public String ipAddress;
  
  public BlockedIPAddressException(String paramString) {
    this.ipAddress = paramString;
  }
}
