package io.fusionauth.app.action.admin.email.template;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.admin.BaseSearchAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.EmailTemplateResponse;
import io.fusionauth.domain.api.EmailTemplateSearchRequest;
import io.fusionauth.domain.api.EmailTemplateSearchResponse;
import io.fusionauth.domain.email.EmailTemplate;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.EmailTemplateSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "email_template_manager"})
public class IndexAction extends BaseSearchAction<EmailTemplate, EmailTemplateSearchCriteria> {
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  protected EmailTemplateSearchCriteria defaultSearchCriteria() {
    return new EmailTemplateSearchCriteria();
  }
  
  protected SearchResults<EmailTemplate> search() {
    UUID uUID = parseUUID(this.s.name);
    if (uUID != null) {
      ClientResponse<EmailTemplateResponse, Void> clientResponse = this.superClient.retrieveEmailTemplate(uUID);
      if (clientResponse.wasSuccessful())
        return new SearchResults<>(List.of(((EmailTemplateResponse)clientResponse.getSuccessResponse()).emailTemplate), 1L); 
      return null;
    } 
    EmailTemplateSearchResponse emailTemplateSearchResponse = superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.searchEmailTemplates(new EmailTemplateSearchRequest(this.s)));
    return new SearchResults<>(emailTemplateSearchResponse.emailTemplates, emailTemplateSearchResponse.total);
  }
}
