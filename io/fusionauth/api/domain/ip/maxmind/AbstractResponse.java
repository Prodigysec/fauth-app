package io.fusionauth.api.domain.ip.maxmind;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public abstract class AbstractResponse {
  public String toJson() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    objectMapper.configure(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS, false);
    return objectMapper.writeValueAsString(this);
  }
  
  public String toString() {
    try {
      return getClass().getName() + " [ " + getClass().getName() + " ]";
    } catch (IOException iOException) {
      throw new RuntimeException(iOException);
    } 
  }
}
