package io.fusionauth.app.action.admin.consent;

import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Consent;
import io.fusionauth.domain.api.EmailTemplateResponse;
import io.fusionauth.domain.email.EmailTemplate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;

public abstract class BaseFormAction extends BaseAction {
  public final List<EmailTemplate> emailTemplates = new ArrayList<>();
  
  public String confirm;
  
  public Consent consent = new Consent();
  
  public UUID consentId;
  
  protected BaseFormAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  @FormPrepareMethod
  public void prepareForm() {
    this.emailTemplates.addAll((Collection<? extends EmailTemplate>)Objects.requireNonNullElseGet(((EmailTemplateResponse)superDelegate().execute(FusionAuthClient::retrieveEmailTemplates)).emailTemplates, Collections::emptyList));
    if (this.consent.defaultMinimumAgeForSelfConsent == null)
      this.consent.defaultMinimumAgeForSelfConsent = Integer.valueOf(13); 
  }
}
