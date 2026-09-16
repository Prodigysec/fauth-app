package io.fusionauth.api.domain.guice;

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.google.inject.Provider;

public class CBORObjectMapperProvider implements Provider<CBORMapper> {
  public CBORMapper get() {
    return new CBORMapper();
  }
}
