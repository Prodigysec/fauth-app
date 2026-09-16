package io.fusionauth.domain.api.twoFactor;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.message.MessageType;
import java.util.UUID;

public class TwoFactorSendRequest implements Buildable<TwoFactorSendRequest> {
  public UUID applicationId;
  
  public String email;
  
  public MessageType messageType;
  
  public String method;
  
  public String methodId;
  
  public String mobilePhone;
  
  public UUID userId;
  
  @JacksonConstructor
  public TwoFactorSendRequest() {}
  
  public TwoFactorSendRequest(String paramString) {
    this.methodId = paramString;
  }
  
  public TwoFactorSendRequest(String paramString, UUID paramUUID) {
    this.method = paramString;
    this.userId = paramUUID;
  }
  
  public TwoFactorSendRequest(UUID paramUUID) {
    this.userId = paramUUID;
  }
}
