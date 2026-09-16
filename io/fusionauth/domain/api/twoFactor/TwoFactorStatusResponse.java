package io.fusionauth.domain.api.twoFactor;

import io.fusionauth.domain.Buildable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TwoFactorStatusResponse implements Buildable<TwoFactorStatusResponse> {
  public List<TwoFactorTrust> trusts = new ArrayList<>();
  
  public String twoFactorTrustId;
  
  public static class TwoFactorTrust {
    public UUID applicationId;
    
    public ZonedDateTime expiration;
    
    public ZonedDateTime startInstant;
    
    public TwoFactorTrust() {}
    
    public TwoFactorTrust(UUID param1UUID, ZonedDateTime param1ZonedDateTime1, ZonedDateTime param1ZonedDateTime2) {
      this.applicationId = param1UUID;
      this.expiration = param1ZonedDateTime1;
      this.startInstant = param1ZonedDateTime2;
    }
    
    public boolean isExpired() {
      return this.expiration.isBefore(ZonedDateTime.now(ZoneOffset.UTC));
    }
  }
}
