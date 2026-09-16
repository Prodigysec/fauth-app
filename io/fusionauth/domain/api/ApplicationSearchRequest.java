package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.search.ApplicationSearchCriteria;

public class ApplicationSearchRequest extends ExpandableRequest implements Buildable<ApplicationSearchRequest> {
  public ApplicationSearchCriteria search = new ApplicationSearchCriteria();
  
  @JacksonConstructor
  public ApplicationSearchRequest() {}
  
  public ApplicationSearchRequest(ApplicationSearchCriteria paramApplicationSearchCriteria) {
    this.search = paramApplicationSearchCriteria;
  }
}
