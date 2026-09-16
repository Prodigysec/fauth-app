package io.fusionauth.app.action.ajax.email.template;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.PreviewRequest;
import io.fusionauth.domain.api.PreviewResponse;
import io.fusionauth.domain.email.Email;
import io.fusionauth.domain.email.EmailTemplate;
import java.util.Locale;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "email_template_manager"})
public class PreviewAction extends BaseAJAXAction {
  public Email email;
  
  public EmailTemplate emailTemplate;
  
  public UUID emailTemplateId;
  
  public Errors errors;
  
  public Locale previewLocale;
  
  @Inject
  public PreviewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String post() {
    PreviewResponse previewResponse = superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveEmailTemplatePreview(new PreviewRequest(this.emailTemplate, this.previewLocale)));
    this.errors = previewResponse.errors;
    this.email = previewResponse.email;
    return "render";
  }
}
