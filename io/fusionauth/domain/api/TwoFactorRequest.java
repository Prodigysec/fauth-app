package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import java.util.UUID;

public class TwoFactorRequest extends BaseEventRequest implements Buildable<TwoFactorRequest> {
  public UUID applicationId;
  
  public String authenticatorId;
  
  public String code;
  
  public String email;
  
  public String method;
  
  public String mobilePhone;
  
  public String name;
  
  public String secret;
  
  public String secretBase32Encoded;
  
  public String twoFactorId;
  
  @JacksonConstructor
  public TwoFactorRequest() {}
  
  public TwoFactorRequest(String paramString1, String paramString2) {
    this(paramString1, null, paramString2);
  }
  
  public TwoFactorRequest(String paramString1, String paramString2, String paramString3) {
    this.code = paramString1;
    this.method = paramString2;
    this.secret = paramString3;
  }
  
  public TwoFactorRequest(EventInfo paramEventInfo, UUID paramUUID, String paramString1, String paramString2) {
    this(paramEventInfo, paramUUID, paramString1, null, paramString2);
  }
  
  public TwoFactorRequest(EventInfo paramEventInfo, UUID paramUUID, String paramString1, String paramString2, String paramString3) {
    super(paramEventInfo);
    this.applicationId = paramUUID;
    this.code = paramString1;
    this.method = paramString2;
    this.secret = paramString3;
  }
}
