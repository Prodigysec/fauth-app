package io.fusionauth.domain.email;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Email implements Buildable<Email> {
  public List<Attachment> attachments = new ArrayList<>();
  
  public List<EmailAddress> bcc = new ArrayList<>();
  
  public List<EmailAddress> cc = new ArrayList<>();
  
  public EmailAddress from;
  
  public String html;
  
  public EmailAddress replyTo;
  
  public String subject;
  
  public String text;
  
  public List<EmailAddress> to = new ArrayList<>();
  
  @JacksonConstructor
  public Email() {}
  
  public Email(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7) {
    this.to.add(new EmailAddress(paramString1, paramString2));
    this.from = new EmailAddress(paramString3, paramString4);
    this.subject = paramString5;
    this.html = paramString6;
    this.text = paramString7;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof Email))
      return false; 
    Email email = (Email)paramObject;
    return (Objects.equals(this.attachments, email.attachments) && 
      Objects.equals(this.bcc, email.bcc) && 
      Objects.equals(this.cc, email.cc) && 
      Objects.equals(this.from, email.from) && 
      Objects.equals(this.html, email.html) && 
      Objects.equals(this.replyTo, email.replyTo) && 
      Objects.equals(this.subject, email.subject) && 
      Objects.equals(this.text, email.text) && 
      Objects.equals(this.to, email.to));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.attachments, this.bcc, this.cc, this.from, this.html, this.replyTo, this.subject, this.text, this.to });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
