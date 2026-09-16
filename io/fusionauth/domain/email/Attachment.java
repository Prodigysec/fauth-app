package io.fusionauth.domain.email;

import com.inversoft.json.JacksonConstructor;
import java.util.Objects;

public class Attachment {
  public byte[] attachment;
  
  public String mime;
  
  public String name;
  
  @JacksonConstructor
  public Attachment() {}
  
  public Attachment(String paramString1, String paramString2, byte[] paramArrayOfbyte) {
    this.name = paramString1;
    this.mime = paramString2;
    this.attachment = paramArrayOfbyte;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof Attachment))
      return false; 
    Attachment attachment = (Attachment)paramObject;
    return (Objects.equals(this.attachment, attachment.attachment) && 
      Objects.equals(this.mime, attachment.mime) && 
      Objects.equals(this.name, attachment.name));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.attachment, this.mime, this.name });
  }
}
