package io.fusionauth.api.domain.webauthn;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.webauthn.CoseEllipticCurve;
import io.fusionauth.domain.webauthn.CoseKeyType;

public class ECCoseKey extends CoseKey implements Buildable<ECCoseKey> {
  @JsonProperty("1")
  public final CoseKeyType keyType = CoseKeyType.EC2;
  
  @JsonProperty("-1")
  public CoseEllipticCurve curveId;
  
  @JsonProperty("-2")
  public byte[] x;
  
  @JsonProperty("-3")
  public byte[] y;
  
  public CoseKeyType keyType() {
    return this.keyType;
  }
}
