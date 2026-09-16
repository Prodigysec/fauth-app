package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import java.util.Map;

public class PublicKeyResponse {
  public String publicKey;
  
  public Map<String, String> publicKeys;
  
  @JacksonConstructor
  public PublicKeyResponse() {}
  
  public PublicKeyResponse(String paramString) {
    this.publicKey = paramString;
  }
  
  public PublicKeyResponse(Map<String, String> paramMap) {
    this.publicKeys = paramMap;
  }
}
