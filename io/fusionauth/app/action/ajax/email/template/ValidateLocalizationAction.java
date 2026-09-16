package io.fusionauth.app.action.ajax.email.template;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import freemarker.core.ParseException;
import freemarker.template.TemplateException;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import java.util.Collections;
import org.primeframework.email.domain.RawEmailTemplates;
import org.primeframework.email.domain.ValidateResult;
import org.primeframework.email.service.EmailService;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "email_template_manager"})
public class ValidateLocalizationAction extends BaseAJAXAction {
  private final EmailService emailService;
  
  public String fromName;
  
  public String htmlTemplate;
  
  public String subject;
  
  public String textTemplate;
  
  @Inject
  public ValidateLocalizationAction(FrontEndSupport paramFrontEndSupport, EmailService paramEmailService) {
    super(paramFrontEndSupport);
    this.emailService = paramEmailService;
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    Errors errors = (new Validator()).ifTrue((this.fromName != null), paramValidator -> validateTemplate(paramValidator, this.fromName, "fromName")).ifTrue((this.subject != null), paramValidator -> validateTemplate(paramValidator, this.subject, "subject")).ifTrue((this.textTemplate != null), paramValidator -> validateTemplate(paramValidator, this.textTemplate, "textTemplate")).ifTrue((this.htmlTemplate != null), paramValidator -> validateTemplate(paramValidator, this.htmlTemplate, "htmlTemplate")).done();
    if (errors.size() > 0) {
      this.frontEndSupport.transfer(errors);
      return "input";
    } 
    return "success";
  }
  
  private void validateTemplate(Validator paramValidator, String paramString1, String paramString2) {
    RawEmailTemplates rawEmailTemplates = new RawEmailTemplates();
    rawEmailTemplates.text = paramString1;
    ValidateResult validateResult = this.emailService.validate(null, rawEmailTemplates, Collections.emptyMap());
    if (!validateResult.wasSuccessful()) {
      String str = validateResult.parseErrors.containsKey("text") ? ((ParseException)validateResult.parseErrors.get("text")).getMessage() : ((TemplateException)validateResult.renderErrors.get("text")).getMessage();
      paramValidator.ensure(false, paramString2, "[invalidTemplate]", new Object[] { str });
    } 
  }
}
