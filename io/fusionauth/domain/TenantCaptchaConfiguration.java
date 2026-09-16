package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.api.domain.json.annotation.MaskString;
import java.util.Objects;

public class TenantCaptchaConfiguration extends Enableable implements Buildable<TenantCaptchaConfiguration> {
  public CaptchaMethod captchaMethod = CaptchaMethod.GoogleRecaptchaV3;
  
  @MaskString
  public String secretKey;
  
  public String siteKey;
  
  public double threshold = 0.5D;
  
  @JacksonConstructor
  public TenantCaptchaConfiguration() {}
  
  public TenantCaptchaConfiguration(TenantCaptchaConfiguration paramTenantCaptchaConfiguration) {
    this.captchaMethod = paramTenantCaptchaConfiguration.captchaMethod;
    this.enabled = paramTenantCaptchaConfiguration.enabled;
    this.secretKey = paramTenantCaptchaConfiguration.secretKey;
    this.siteKey = paramTenantCaptchaConfiguration.siteKey;
    this.threshold = paramTenantCaptchaConfiguration.threshold;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    if (!super.equals(paramObject))
      return false; 
    TenantCaptchaConfiguration tenantCaptchaConfiguration = (TenantCaptchaConfiguration)paramObject;
    return (Double.compare(tenantCaptchaConfiguration.threshold, this.threshold) == 0 && this.captchaMethod == tenantCaptchaConfiguration.captchaMethod && 
      
      Objects.equals(this.secretKey, tenantCaptchaConfiguration.secretKey) && 
      Objects.equals(this.siteKey, tenantCaptchaConfiguration.siteKey));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.captchaMethod, this.secretKey, this.siteKey, Double.valueOf(this.threshold) });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
