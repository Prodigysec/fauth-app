package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.api.domain.annotation.InternalUse;
import io.fusionauth.api.domain.json.annotation.MaskString;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SystemConfiguration implements Buildable<SystemConfiguration>, JSONColumnable {
  @JSONColumn
  public AuditLogConfiguration auditLogConfiguration = new AuditLogConfiguration();
  
  @MaskString
  @InternalUse
  @JSONColumn
  public String cookieEncryptionKey;
  
  @JSONColumn
  public CORSConfiguration corsConfiguration = new CORSConfiguration();
  
  public Map<String, Object> data = new HashMap<>();
  
  @JSONColumn
  public EventLogConfiguration eventLogConfiguration = new EventLogConfiguration();
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastUpdateInstant;
  
  @JSONColumn
  public LoginRecordConfiguration loginRecordConfiguration = new LoginRecordConfiguration();
  
  public ZoneId reportTimezone;
  
  @JSONColumn
  public SystemTrustedProxyConfiguration trustedProxyConfiguration = new SystemTrustedProxyConfiguration();
  
  @JSONColumn
  public UIConfiguration uiConfiguration = new UIConfiguration();
  
  @JSONColumn
  public UsageDataConfiguration usageDataConfiguration = new UsageDataConfiguration();
  
  @JSONColumn
  public WebhookEventLogConfiguration webhookEventLogConfiguration = new WebhookEventLogConfiguration();
  
  @JacksonConstructor
  public SystemConfiguration() {}
  
  public SystemConfiguration(SystemConfiguration paramSystemConfiguration) {
    this.auditLogConfiguration = new AuditLogConfiguration(paramSystemConfiguration.auditLogConfiguration);
    this.cookieEncryptionKey = paramSystemConfiguration.cookieEncryptionKey;
    this.corsConfiguration = new CORSConfiguration(paramSystemConfiguration.corsConfiguration);
    if (paramSystemConfiguration.data != null)
      this.data.putAll(paramSystemConfiguration.data); 
    this.eventLogConfiguration = new EventLogConfiguration(paramSystemConfiguration.eventLogConfiguration);
    this.insertInstant = paramSystemConfiguration.insertInstant;
    this.lastUpdateInstant = paramSystemConfiguration.lastUpdateInstant;
    this.loginRecordConfiguration = new LoginRecordConfiguration(paramSystemConfiguration.loginRecordConfiguration);
    this.reportTimezone = paramSystemConfiguration.reportTimezone;
    this.trustedProxyConfiguration = new SystemTrustedProxyConfiguration(paramSystemConfiguration.trustedProxyConfiguration);
    this.uiConfiguration = new UIConfiguration(paramSystemConfiguration.uiConfiguration);
    this.usageDataConfiguration = new UsageDataConfiguration(paramSystemConfiguration.usageDataConfiguration);
    this.webhookEventLogConfiguration = new WebhookEventLogConfiguration(paramSystemConfiguration.webhookEventLogConfiguration);
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    SystemConfiguration systemConfiguration = (SystemConfiguration)paramObject;
    return (Objects.equals(this.auditLogConfiguration, systemConfiguration.auditLogConfiguration) && 
      Objects.equals(this.cookieEncryptionKey, systemConfiguration.cookieEncryptionKey) && 
      Objects.equals(this.corsConfiguration, systemConfiguration.corsConfiguration) && 
      Objects.equals(this.data, systemConfiguration.data) && 
      Objects.equals(this.eventLogConfiguration, systemConfiguration.eventLogConfiguration) && 
      Objects.equals(this.insertInstant, systemConfiguration.insertInstant) && 
      Objects.equals(this.lastUpdateInstant, systemConfiguration.lastUpdateInstant) && 
      Objects.equals(this.loginRecordConfiguration, systemConfiguration.loginRecordConfiguration) && 
      Objects.equals(this.trustedProxyConfiguration, systemConfiguration.trustedProxyConfiguration) && 
      Objects.equals(this.reportTimezone, systemConfiguration.reportTimezone) && 
      Objects.equals(this.uiConfiguration, systemConfiguration.uiConfiguration) && 
      Objects.equals(this.usageDataConfiguration, systemConfiguration.usageDataConfiguration) && 
      Objects.equals(this.webhookEventLogConfiguration, systemConfiguration.webhookEventLogConfiguration));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.auditLogConfiguration, this.cookieEncryptionKey, this.corsConfiguration, this.data, this.eventLogConfiguration, this.insertInstant, this.lastUpdateInstant, this.loginRecordConfiguration, this.reportTimezone, this.trustedProxyConfiguration, 
          this.uiConfiguration, this.usageDataConfiguration, this.webhookEventLogConfiguration });
  }
  
  public void normalize() {
    if (this.uiConfiguration != null)
      this.uiConfiguration.normalize(); 
    if (this.corsConfiguration != null)
      this.corsConfiguration.normalize(); 
    if (this.trustedProxyConfiguration != null)
      this.trustedProxyConfiguration.normalize(); 
  }
  
  public SystemConfiguration secure() {
    this.cookieEncryptionKey = null;
    return this;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  public static class AuditLogConfiguration {
    public SystemConfiguration.DeleteConfiguration delete = new SystemConfiguration.DeleteConfiguration(365);
    
    @JacksonConstructor
    public AuditLogConfiguration() {}
    
    public AuditLogConfiguration(AuditLogConfiguration param1AuditLogConfiguration) {
      this.delete = new SystemConfiguration.DeleteConfiguration(param1AuditLogConfiguration.delete);
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (!(param1Object instanceof AuditLogConfiguration))
        return false; 
      AuditLogConfiguration auditLogConfiguration = (AuditLogConfiguration)param1Object;
      return Objects.equals(this.delete, auditLogConfiguration.delete);
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { this.delete });
    }
    
    public String toString() {
      return ToString.toString(this);
    }
  }
  
  public static class DeleteConfiguration extends Enableable {
    public int numberOfDaysToRetain = 365;
    
    @JacksonConstructor
    public DeleteConfiguration() {}
    
    public DeleteConfiguration(DeleteConfiguration param1DeleteConfiguration) {
      this.enabled = param1DeleteConfiguration.enabled;
      this.numberOfDaysToRetain = param1DeleteConfiguration.numberOfDaysToRetain;
    }
    
    public DeleteConfiguration(int param1Int) {
      this.numberOfDaysToRetain = param1Int;
    }
    
    public DeleteConfiguration(int param1Int, boolean param1Boolean) {
      this.enabled = param1Boolean;
      this.numberOfDaysToRetain = param1Int;
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (!(param1Object instanceof DeleteConfiguration))
        return false; 
      if (!super.equals(param1Object))
        return false; 
      DeleteConfiguration deleteConfiguration = (DeleteConfiguration)param1Object;
      return Objects.equals(Integer.valueOf(this.numberOfDaysToRetain), Integer.valueOf(deleteConfiguration.numberOfDaysToRetain));
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), Integer.valueOf(this.numberOfDaysToRetain) });
    }
    
    public String toString() {
      return ToString.toString(this);
    }
  }
  
  public static class EventLogConfiguration {
    public int numberToRetain = 10000;
    
    @JacksonConstructor
    public EventLogConfiguration() {}
    
    public EventLogConfiguration(EventLogConfiguration param1EventLogConfiguration) {
      this.numberToRetain = param1EventLogConfiguration.numberToRetain;
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (!(param1Object instanceof EventLogConfiguration))
        return false; 
      EventLogConfiguration eventLogConfiguration = (EventLogConfiguration)param1Object;
      return (this.numberToRetain == eventLogConfiguration.numberToRetain);
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { Integer.valueOf(this.numberToRetain) });
    }
    
    public String toString() {
      return ToString.toString(this);
    }
  }
  
  public static class LoginRecordConfiguration {
    public SystemConfiguration.DeleteConfiguration delete = new SystemConfiguration.DeleteConfiguration(365);
    
    @JacksonConstructor
    public LoginRecordConfiguration() {}
    
    public LoginRecordConfiguration(LoginRecordConfiguration param1LoginRecordConfiguration) {
      this.delete = new SystemConfiguration.DeleteConfiguration(param1LoginRecordConfiguration.delete);
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (!(param1Object instanceof LoginRecordConfiguration))
        return false; 
      LoginRecordConfiguration loginRecordConfiguration = (LoginRecordConfiguration)param1Object;
      return Objects.equals(this.delete, loginRecordConfiguration.delete);
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { this.delete });
    }
    
    public String toString() {
      return ToString.toString(this);
    }
  }
  
  public static class UIConfiguration implements Buildable<UIConfiguration> {
    public String headerColor;
    
    public String logoURL;
    
    public String menuFontColor;
    
    @JacksonConstructor
    public UIConfiguration() {}
    
    public UIConfiguration(UIConfiguration param1UIConfiguration) {
      this.headerColor = param1UIConfiguration.headerColor;
      this.logoURL = param1UIConfiguration.logoURL;
      this.menuFontColor = param1UIConfiguration.menuFontColor;
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (!(param1Object instanceof UIConfiguration))
        return false; 
      UIConfiguration uIConfiguration = (UIConfiguration)param1Object;
      return (Objects.equals(this.logoURL, uIConfiguration.logoURL) && 
        Objects.equals(this.headerColor, uIConfiguration.headerColor) && 
        Objects.equals(this.menuFontColor, uIConfiguration.menuFontColor));
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { this.logoURL, this.headerColor, this.menuFontColor });
    }
    
    public void normalize() {}
  }
}
