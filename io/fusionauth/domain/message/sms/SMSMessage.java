package io.fusionauth.domain.message.sms;

import com.inversoft.json.ToString;
import io.fusionauth.domain.message.Message;
import io.fusionauth.domain.message.MessageType;
import java.util.Objects;
import java.util.UUID;

public class SMSMessage implements Message {
  public String code;
  
  public String phoneNumber;
  
  public String textMessage;
  
  public UUID userId;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    SMSMessage sMSMessage = (SMSMessage)paramObject;
    return (Objects.equals(this.code, sMSMessage.code) && 
      Objects.equals(this.textMessage, sMSMessage.textMessage) && 
      Objects.equals(this.phoneNumber, sMSMessage.phoneNumber) && 
      Objects.equals(this.userId, sMSMessage.userId));
  }
  
  public MessageType getType() {
    return MessageType.SMS;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.code, this.textMessage, this.phoneNumber, this.userId });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
