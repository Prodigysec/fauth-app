package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.search.ThemeSearchCriteria;

public class ThemeSearchRequest implements Buildable<ThemeSearchRequest> {
  public ThemeSearchCriteria search = new ThemeSearchCriteria();
  
  @JacksonConstructor
  public ThemeSearchRequest() {}
  
  public ThemeSearchRequest(ThemeSearchCriteria paramThemeSearchCriteria) {
    this.search = paramThemeSearchCriteria;
  }
}
