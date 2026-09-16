package io.fusionauth.api.domain.webauthn;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.webauthn.CoseKeyType;

public class RSACoseKey extends CoseKey implements Buildable<RSACoseKey> {
  @JsonProperty("1")
  public final CoseKeyType keyType = CoseKeyType.RSA;
  
  @JsonProperty("-3")
  public byte[] d;
  
  @JsonProperty("-6")
  public byte[] dP;
  
  @JsonProperty("-7")
  public byte[] dQ;
  
  @JsonProperty("-2")
  public byte[] e;
  
  @JsonProperty("-1")
  public byte[] n;
  
  @JsonProperty("-4")
  public byte[] p;
  
  @JsonProperty("-5")
  public byte[] q;
  
  @JsonProperty("-8")
  public byte[] qInv;
  
  public CoseKeyType keyType() {
    return this.keyType;
  }
}
