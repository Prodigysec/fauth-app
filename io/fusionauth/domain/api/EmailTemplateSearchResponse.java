package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.email.EmailTemplate;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;

public class EmailTemplateSearchResponse {
  public List<EmailTemplate> emailTemplates;
  
  public long total;
  
  @JacksonConstructor
  public EmailTemplateSearchResponse() {}
  
  public EmailTemplateSearchResponse(SearchResults<EmailTemplate> paramSearchResults) {
    this.emailTemplates = paramSearchResults.results;
    this.total = paramSearchResults.total;
  }
}
