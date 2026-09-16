package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.DisplayableRawLogin;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;

public class LoginRecordSearchResponse {
  public List<DisplayableRawLogin> logins;
  
  public long total;
  
  @JacksonConstructor
  public LoginRecordSearchResponse() {}
  
  public LoginRecordSearchResponse(SearchResults<DisplayableRawLogin> paramSearchResults) {
    this.logins = paramSearchResults.results;
    this.total = paramSearchResults.total;
  }
}
