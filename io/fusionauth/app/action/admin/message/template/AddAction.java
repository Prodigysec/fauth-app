package io.fusionauth.app.action.admin.message.template;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.MessageTemplateRequest;
import io.fusionauth.domain.api.MessageTemplateResponse;
import io.fusionauth.domain.message.MessageTemplate;
import io.fusionauth.domain.message.sms.SMSMessageTemplate;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;

@Action(requiresAuthentication = true, constraints = {"admin", "message_template_manager"})
@List({@Redirect(code = "success", uri = "/admin/message/template/"), @Redirect(code = "api-error", uri = "/admin/message/template/")})
public class AddAction extends BaseFormAction {
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    if (this.messageTemplateId != null) {
      this.messageTemplate = ((MessageTemplateResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveMessageTemplate(this.messageTemplateId))).messageTemplate;
      this.messageTemplate.id = null;
      this.messageTemplateId = null;
      this.messageTemplate.name += " - copy";
    } else {
      this.messageTemplate = new SMSMessageTemplate();
    } 
    return "input";
  }
  
  public String post() {
    MessageTemplate messageTemplate = ((MessageTemplateResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.createMessageTemplate(this.messageTemplateId, new MessageTemplateRequest(this.messageTemplate)))).messageTemplate;
    writeAuditLog("Created the message template with Id [" + String.valueOf(messageTemplate.id) + "] and name [" + this.messageTemplate.name + "]");
    return "success";
  }
}
