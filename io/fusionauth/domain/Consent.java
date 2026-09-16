package io.fusionauth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.ExcludeFromJSONColumn;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.domain.util.Normalizer;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Consent implements Buildable<Consent>, JSONColumnable {
  public final Map<String, Object> data = new LinkedHashMap<>();
  
  public UUID consentEmailTemplateId;
  
  @JSONColumn
  public LocalizedIntegers countryMinimumAgeForSelfConsent = new LocalizedIntegers();
  
  @JSONColumn
  public Integer defaultMinimumAgeForSelfConsent;
  
  @JSONColumn
  public EmailPlus emailPlus = new EmailPlus();
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastUpdateInstant;
  
  @JSONColumn
  public boolean multipleValuesAllowed;
  
  public String name;
  
  @JSONColumn
  public List<String> values = new ArrayList<>();
  
  @JsonIgnore
  public boolean canSelfConsent(User paramUser) {
    Integer integer = getMinimumSelfConsentAge(paramUser);
    if (integer.intValue() == 0)
      return true; 
    return (paramUser.getAge() >= integer.intValue());
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof Consent))
      return false; 
    Consent consent = (Consent)paramObject;
    return (this.multipleValuesAllowed == consent.multipleValuesAllowed && 
      Objects.equals(this.data, consent.data) && 
      Objects.equals(this.consentEmailTemplateId, consent.consentEmailTemplateId) && 
      Objects.equals(this.countryMinimumAgeForSelfConsent, consent.countryMinimumAgeForSelfConsent) && 
      Objects.equals(this.defaultMinimumAgeForSelfConsent, consent.defaultMinimumAgeForSelfConsent) && 
      Objects.equals(this.emailPlus, consent.emailPlus) && 
      Objects.equals(this.id, consent.id) && 
      Objects.equals(this.insertInstant, consent.insertInstant) && 
      Objects.equals(this.lastUpdateInstant, consent.lastUpdateInstant) && 
      Objects.equals(this.name, consent.name) && 
      Objects.equals(this.values, consent.values));
  }
  
  @JsonIgnore
  public Integer getMinimumSelfConsentAge(User paramUser) {
    for (Locale locale : paramUser.preferredLanguages) {
      Integer integer = this.countryMinimumAgeForSelfConsent.get(locale);
      if (integer != null)
        return integer; 
    } 
    return this.defaultMinimumAgeForSelfConsent;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.data, this.consentEmailTemplateId, this.countryMinimumAgeForSelfConsent, this.defaultMinimumAgeForSelfConsent, this.emailPlus, this.id, this.insertInstant, this.lastUpdateInstant, Boolean.valueOf(this.multipleValuesAllowed), this.name, 
          this.values });
  }
  
  public void normalize() {
    if (this.values != null)
      Normalizer.removeEmpty(this.values); 
    if (this.countryMinimumAgeForSelfConsent != null)
      this.countryMinimumAgeForSelfConsent.removeEmpty(); 
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  public static class EmailPlus extends Enableable {
    @ExcludeFromJSONColumn
    public UUID emailTemplateId;
    
    public int maximumTimeToSendEmailInHours = 48;
    
    public int minimumTimeToSendEmailInHours = 24;
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (!(param1Object instanceof EmailPlus))
        return false; 
      if (!super.equals(param1Object))
        return false; 
      EmailPlus emailPlus = (EmailPlus)param1Object;
      return (this.maximumTimeToSendEmailInHours == emailPlus.maximumTimeToSendEmailInHours && this.minimumTimeToSendEmailInHours == emailPlus.minimumTimeToSendEmailInHours && 
        
        Objects.equals(this.emailTemplateId, emailPlus.emailTemplateId));
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.emailTemplateId, Integer.valueOf(this.maximumTimeToSendEmailInHours), Integer.valueOf(this.minimumTimeToSendEmailInHours) });
    }
    
    public String toString() {
      return ToString.toString(this);
    }
  }
}
