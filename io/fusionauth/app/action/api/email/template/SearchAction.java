package io.fusionauth.app.action.api.email.template;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.email.EmailTemplateService;
import io.fusionauth.app.action.api.BaseSearchAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.EmailTemplateSearchRequest;
import io.fusionauth.domain.api.EmailTemplateSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.EmailTemplateSearchCriteria;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class SearchAction extends BaseSearchAPIAction<EmailTemplateSearchCriteria> {
  @JSONRequest
  public final EmailTemplateSearchRequest request = new EmailTemplateSearchRequest();
  
  private final EmailTemplateService emailTemplateService;
  
  @JSONResponse
  public EmailTemplateSearchResponse response;
  
  @Inject
  public SearchAction(AuthenticationKeyCache paramAuthenticationKeyCache, FrontEndSupport paramFrontEndSupport, EmailTemplateService paramEmailTemplateService) {
    super(paramAuthenticationKeyCache, paramFrontEndSupport);
    this.emailTemplateService = paramEmailTemplateService;
  }
  
  public String name() {
    return (criteria()).name;
  }
  
  public void setName(String paramString) {
    (criteria()).name = paramString;
  }
  
  protected EmailTemplateSearchCriteria criteria() {
    return this.request.search;
  }
  
  protected String search() {
    this.response = new EmailTemplateSearchResponse(this.emailTemplateService.search(this.request.search));
    return "render";
  }
}
