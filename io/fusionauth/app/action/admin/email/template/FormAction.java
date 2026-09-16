package io.fusionauth.app.action.admin.email.template;

import com.google.inject.Inject;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.email.EmailTemplate;
import java.util.UUID;

public abstract class FormAction extends BaseAction {
  public EmailTemplate emailTemplate = new EmailTemplate();
  
  public UUID emailTemplateId;
  
  @Inject
  public FormAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
}
