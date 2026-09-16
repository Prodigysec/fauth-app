package io.fusionauth.app.action.ajax.message.template;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import freemarker.core.ParseException;
import freemarker.template.TemplateException;
import io.fusionauth.api.service.messenger.DefaultMessengerService;
import io.fusionauth.api.service.messenger.MessengerService;
import io.fusionauth.api.util.PhoneMessageTools;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.message.sms.SMSMessageTemplate;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "message_template_manager"})
public class ValidateLocalizationAction extends BaseAJAXAction {
  private final MessengerService messengerService;
  
  public String template;
  
  @Inject
  public ValidateLocalizationAction(FrontEndSupport paramFrontEndSupport, MessengerService paramMessengerService) {
    super(paramFrontEndSupport);
    this.messengerService = paramMessengerService;
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    return "success";
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validate() {
    Errors errors = (new Validator()).ifTrue((this.template != null), paramValidator -> validateTemplate(paramValidator, this.template)).done();
    this.frontEndSupport.transfer(errors);
  }
  
  private void validateTemplate(Validator paramValidator, String paramString) {
    SMSMessageTemplate sMSMessageTemplate = new SMSMessageTemplate();
    sMSMessageTemplate.defaultTemplate = paramString;
    DefaultMessengerService.ValidationResult validationResult = this.messengerService.validate(sMSMessageTemplate, this.locale, PhoneMessageTools.MOCK_PARAMETERS);
    if (!validationResult.wasSuccessful()) {
      String str = validationResult.parseErrors.containsKey("message") ? ((ParseException)validationResult.parseErrors.get("message")).getMessage() : ((TemplateException)validationResult.renderErrors.get("message")).getMessage();
      paramValidator.ensure(false, "template", "[invalidTemplate]", new Object[] { str });
    } 
  }
}
