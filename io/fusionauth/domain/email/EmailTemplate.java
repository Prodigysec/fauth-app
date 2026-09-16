package io.fusionauth.domain.email;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.LocalizedStrings;
import io.fusionauth.domain.util.Normalizer;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class EmailTemplate implements Buildable<EmailTemplate> {
  public String defaultFromName;
  
  public String defaultHtmlTemplate;
  
  public String defaultSubject;
  
  public String defaultTextTemplate;
  
  public String fromEmail;
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastUpdateInstant;
  
  public LocalizedStrings localizedFromNames = new LocalizedStrings();
  
  public LocalizedStrings localizedHtmlTemplates = new LocalizedStrings();
  
  public LocalizedStrings localizedSubjects = new LocalizedStrings();
  
  public LocalizedStrings localizedTextTemplates = new LocalizedStrings();
  
  public String name;
  
  @JacksonConstructor
  public EmailTemplate() {}
  
  public EmailTemplate(UUID paramUUID, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6) {
    this.defaultFromName = paramString2;
    this.fromEmail = paramString3;
    this.defaultHtmlTemplate = paramString5;
    this.defaultSubject = paramString4;
    this.defaultTextTemplate = paramString6;
    this.id = paramUUID;
    this.name = paramString1;
  }
  
  public EmailTemplate(UUID paramUUID, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, LocalizedStrings paramLocalizedStrings1, LocalizedStrings paramLocalizedStrings2, LocalizedStrings paramLocalizedStrings3, LocalizedStrings paramLocalizedStrings4) {
    this.fromEmail = paramString3;
    this.defaultFromName = paramString2;
    this.defaultHtmlTemplate = paramString5;
    this.defaultSubject = paramString4;
    this.defaultTextTemplate = paramString6;
    this.id = paramUUID;
    this.localizedFromNames = paramLocalizedStrings1;
    this.localizedHtmlTemplates = paramLocalizedStrings3;
    this.localizedSubjects = paramLocalizedStrings2;
    this.localizedTextTemplates = paramLocalizedStrings4;
    this.name = paramString1;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof EmailTemplate))
      return false; 
    EmailTemplate emailTemplate = (EmailTemplate)paramObject;
    return (Objects.equals(this.defaultFromName, emailTemplate.defaultFromName) && 
      Objects.equals(this.defaultHtmlTemplate, emailTemplate.defaultHtmlTemplate) && 
      Objects.equals(this.defaultSubject, emailTemplate.defaultSubject) && 
      Objects.equals(this.defaultTextTemplate, emailTemplate.defaultTextTemplate) && 
      Objects.equals(this.fromEmail, emailTemplate.fromEmail) && 
      Objects.equals(this.id, emailTemplate.id) && 
      Objects.equals(this.insertInstant, emailTemplate.insertInstant) && 
      Objects.equals(this.lastUpdateInstant, emailTemplate.lastUpdateInstant) && 
      Objects.equals(this.localizedFromNames, emailTemplate.localizedFromNames) && 
      Objects.equals(this.localizedHtmlTemplates, emailTemplate.localizedHtmlTemplates) && 
      Objects.equals(this.localizedSubjects, emailTemplate.localizedSubjects) && 
      Objects.equals(this.localizedTextTemplates, emailTemplate.localizedTextTemplates) && 
      Objects.equals(this.name, emailTemplate.name));
  }
  
  @JsonIgnore
  public Set<Locale> getLocalizations() {
    HashSet<Locale> hashSet = new HashSet<>(this.localizedFromNames.keySet());
    hashSet.addAll(this.localizedHtmlTemplates.keySet());
    hashSet.addAll(this.localizedSubjects.keySet());
    hashSet.addAll(this.localizedTextTemplates.keySet());
    return hashSet;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.defaultFromName, this.defaultHtmlTemplate, this.defaultSubject, this.defaultTextTemplate, this.fromEmail, this.id, this.insertInstant, this.lastUpdateInstant, this.localizedFromNames, this.localizedHtmlTemplates, 
          this.localizedSubjects, this.localizedTextTemplates, this.name });
  }
  
  public void normalize() {
    this.defaultFromName = Normalizer.trim(this.defaultFromName);
    this.defaultHtmlTemplate = Normalizer.trim(this.defaultHtmlTemplate);
    this.defaultSubject = Normalizer.trim(this.defaultSubject);
    this.defaultTextTemplate = Normalizer.trim(this.defaultTextTemplate);
    this.fromEmail = Normalizer.trim(this.fromEmail);
    this.name = Normalizer.trim(this.name);
    if (this.localizedFromNames != null)
      this.localizedFromNames.normalize(); 
    if (this.localizedHtmlTemplates != null)
      this.localizedHtmlTemplates.normalize(); 
    if (this.localizedSubjects != null)
      this.localizedSubjects.normalize(); 
    if (this.localizedTextTemplates != null)
      this.localizedTextTemplates.normalize(); 
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
