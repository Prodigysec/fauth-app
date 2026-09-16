package io.fusionauth.api.domain.webauthn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.fusionauth.domain.webauthn.CoseAlgorithmIdentifier;
import io.fusionauth.domain.webauthn.CoseKeyType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "1", include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({@Type(value = ECCoseKey.class, name = "2"), @Type(value = RSACoseKey.class, name = "3")})
public abstract class CoseKey {
  @JsonProperty("3")
  public CoseAlgorithmIdentifier algorithm;
  
  public abstract CoseKeyType keyType();
}
