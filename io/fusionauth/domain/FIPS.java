package io.fusionauth.domain;

public class FIPS {
  public static final int FIPS_MIN_PASSWORD_LENGTH = 14;
  
  public static final int FIPS_RECOVERY_CODE_LENGTH = 14;
  
  public static final int STANDARD_MIN_PASSWORD_LENGTH = 8;
  
  public static final int STANDARD_RECOVERY_CODE_LENGTH = 10;
  
  private static volatile Boolean Enabled;
  
  public static boolean isEnabled() {
    if (Enabled != null)
      return Enabled.booleanValue(); 
    Enabled = Boolean.valueOf(Boolean.getBoolean("fusionauth.fips.enabled"));
    return Boolean.TRUE.equals(Enabled);
  }
  
  public static int minimumPasswordLength() {
    return isEnabled() ? 14 : 8;
  }
  
  public static int minimumRecoveryCodeLength() {
    return isEnabled() ? 14 : 10;
  }
}
