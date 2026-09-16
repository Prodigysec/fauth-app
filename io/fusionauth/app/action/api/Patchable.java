package io.fusionauth.app.action.api;

import org.primeframework.mvc.content.ValidContentTypes;
import org.primeframework.mvc.parameter.annotation.PreParameterMethod;

public interface Patchable {
  void loadExisting();
  
  @ValidContentTypes({"application/json", "application/json-patch+json", "application/merge-patch+json"})
  default String patch() throws Exception {
    return put();
  }
  
  @PreParameterMethod(httpMethods = {"PATCH"})
  default void patchFetch() {
    loadExisting();
  }
  
  String put() throws Exception;
}
