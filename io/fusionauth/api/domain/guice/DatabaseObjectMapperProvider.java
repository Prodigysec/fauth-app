package io.fusionauth.api.domain.guice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.inversoft.mybatis.ExcludeFromJSONColumnFilter;
import java.util.Set;

public class DatabaseObjectMapperProvider implements Provider<ObjectMapper> {
  private final Set<Module> jacksonModules;
  
  @Inject
  public DatabaseObjectMapperProvider(Set<Module> paramSet) {
    this.jacksonModules = paramSet;
  }
  
  public ObjectMapper get() {
    ObjectMapper objectMapper = (new ObjectMapper()).configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true).configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false).configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true).configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false).configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true).configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true).setDefaultMergeable(Boolean.valueOf(true)).setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.setAnnotationIntrospector((AnnotationIntrospector)new ExcludeFromJSONColumnFilter());
    if (this.jacksonModules.size() > 0)
      objectMapper.registerModules(this.jacksonModules); 
    return objectMapper;
  }
}
