package io.fusionauth.app.primeframework;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.google.inject.Inject;
import java.util.Set;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.content.guice.ObjectMapperProvider;

public class FusionAuthObjectMapperProvider extends ObjectMapperProvider {
  @Inject
  public FusionAuthObjectMapperProvider(Set<Module> paramSet, MVCConfiguration paramMVCConfiguration) {
    super(paramSet, paramMVCConfiguration);
  }
  
  protected ObjectMapper configure(ObjectMapper paramObjectMapper) {
    ObjectMapper objectMapper = super.configure(paramObjectMapper).setDefaultMergeable(Boolean.valueOf(true));
    objectMapper.coercionConfigFor(LogicalType.Enum).setCoercion(CoercionInputShape.EmptyString, CoercionAction.TryConvert);
    objectMapper.configure(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION, true);
    return objectMapper;
  }
}
