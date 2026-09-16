package io.fusionauth.domain.message.sms;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.LocalizedStrings;
import io.fusionauth.domain.message.MessageTemplate;
import io.fusionauth.domain.message.MessageType;
import io.fusionauth.domain.util.Normalizer;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class SMSMessageTemplate extends MessageTemplate implements Buildable<SMSMessageTemplate> {
  @JSONColumn
  public String defaultTemplate;
  
  @JSONColumn
  public LocalizedStrings localizedTemplates = new LocalizedStrings();
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    if (!super.equals(paramObject))
      return false; 
    SMSMessageTemplate sMSMessageTemplate = (SMSMessageTemplate)paramObject;
    return (Objects.equals(this.defaultTemplate, sMSMessageTemplate.defaultTemplate) && Objects.equals(this.localizedTemplates, sMSMessageTemplate.localizedTemplates));
  }
  
  @JsonIgnore
  public Set<Locale> getLocalizations() {
    return new HashSet<>(this.localizedTemplates.keySet());
  }
  
  public MessageType getType() {
    return MessageType.SMS;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.defaultTemplate, this.localizedTemplates });
  }
  
  public void normalize() {
    this.defaultTemplate = Normalizer.trim(this.defaultTemplate);
    this.name = Normalizer.trim(this.name);
    if (this.localizedTemplates != null)
      this.localizedTemplates.normalize(); 
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
