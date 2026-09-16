package io.fusionauth.domain.message.voice;

import com.inversoft.json.ToString;
import io.fusionauth.domain.message.Message;
import io.fusionauth.domain.message.MessageType;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class VoiceMessage implements Message {
  public String code;
  
  public Locale locale;
  
  public String message;
  
  public String phoneNumber;
  
  public UUID userId;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    VoiceMessage voiceMessage = (VoiceMessage)paramObject;
    return (Objects.equals(this.code, voiceMessage.code) && 
      Objects.equals(this.locale, voiceMessage.locale) && 
      Objects.equals(this.message, voiceMessage.message) && 
      Objects.equals(this.phoneNumber, voiceMessage.phoneNumber) && 
      Objects.equals(this.userId, voiceMessage.userId));
  }
  
  public MessageType getType() {
    return MessageType.Voice;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.code, this.locale, this.message, this.phoneNumber, this.userId });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
