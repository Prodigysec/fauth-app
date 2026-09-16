package io.fusionauth.domain.api.identity.verify;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;

public class VerifySendRequest implements Buildable<VerifySendRequest> {
  public String verificationId;
  
  public VerifySendRequest(String paramString) {
    this.verificationId = paramString;
  }
  
  @JacksonConstructor
  public VerifySendRequest() {}
}
