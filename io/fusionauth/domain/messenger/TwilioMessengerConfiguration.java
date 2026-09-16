package io.fusionauth.domain.messenger;

import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import io.fusionauth.api.domain.json.annotation.MaskString;
import io.fusionauth.domain.Buildable;
import java.net.URI;
import java.util.Objects;

public class TwilioMessengerConfiguration extends BaseMessengerConfiguration implements Buildable<TwilioMessengerConfiguration> {
  @JSONColumn
  public String accountSID;
  
  @MaskString
  @JSONColumn
  public String authToken;
  
  @JSONColumn
  public String fromPhoneNumber;
  
  @JSONColumn
  public String messagingServiceSid;
  
  @JSONColumn
  public URI url;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof TwilioMessengerConfiguration))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    TwilioMessengerConfiguration twilioMessengerConfiguration = (TwilioMessengerConfiguration)paramObject;
    return (super.equals(paramObject) && 
      Objects.equals(this.accountSID, twilioMessengerConfiguration.accountSID) && 
      Objects.equals(this.authToken, twilioMessengerConfiguration.authToken) && 
      Objects.equals(this.fromPhoneNumber, twilioMessengerConfiguration.fromPhoneNumber) && 
      Objects.equals(this.messagingServiceSid, twilioMessengerConfiguration.messagingServiceSid) && 
      Objects.equals(this.url, twilioMessengerConfiguration.url));
  }
  
  public MessengerType getType() {
    return MessengerType.Twilio;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.accountSID, this.authToken, this.fromPhoneNumber, this.messagingServiceSid, this.url });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
