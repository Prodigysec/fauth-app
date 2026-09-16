package io.fusionauth.client.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.fusionauth.domain.IdentityType;

public class FusionAuthJacksonModule extends SimpleModule {
  public FusionAuthJacksonModule() {
    addSerializer(IdentityType.class, new IdentityTypeSerializer());
    addDeserializer(IdentityType.class, new IdentityTypeDeserializer());
  }
}
