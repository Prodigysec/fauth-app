package io.fusionauth.api.service.system;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import java.util.Set;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.content.guice.ObjectMapperProvider;

public class LambdaObjectMapperProvider extends ObjectMapperProvider {
  @Inject
  public LambdaObjectMapperProvider(Set<Module> paramSet, MVCConfiguration paramMVCConfiguration) {
    super(paramSet, paramMVCConfiguration);
  }
  
  protected ObjectMapper configure(ObjectMapper paramObjectMapper) {
    return super.configure(paramObjectMapper)
      
      .setDefaultMergeable(Boolean.valueOf(false));
  }
}
