package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.search.EmailTemplateSearchCriteria;

public class EmailTemplateSearchRequest implements Buildable<EmailTemplateSearchRequest> {
  public EmailTemplateSearchCriteria search = new EmailTemplateSearchCriteria();
  
  @JacksonConstructor
  public EmailTemplateSearchRequest() {}
  
  public EmailTemplateSearchRequest(EmailTemplateSearchCriteria paramEmailTemplateSearchCriteria) {
    this.search = paramEmailTemplateSearchCriteria;
  }
}
