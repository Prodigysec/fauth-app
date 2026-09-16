package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Theme;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;

public class ThemeSearchResponse {
  public List<Theme> themes;
  
  public long total;
  
  @JacksonConstructor
  public ThemeSearchResponse() {}
  
  public ThemeSearchResponse(SearchResults<Theme> paramSearchResults) {
    this.themes = paramSearchResults.results;
    this.total = paramSearchResults.total;
  }
}
