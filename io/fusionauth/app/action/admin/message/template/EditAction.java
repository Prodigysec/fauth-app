package io.fusionauth.app.action.admin.message.template;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.MessageTemplateRequest;
import io.fusionauth.domain.api.MessageTemplateResponse;
import io.fusionauth.domain.message.MessageTemplate;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;

@Action(requiresAuthentication = true, value = "{messageTemplateId}", constraints = {"admin", "message_template_manager"})
@List({@Redirect(code = "missing", uri = "/admin/message/template/"), @Redirect(code = "success", uri = "/admin/message/template/")})
public class EditAction extends BaseFormAction {
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.messageTemplate = ((MessageTemplateResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveMessageTemplate(this.messageTemplateId))).messageTemplate;
    this.type = this.messageTemplate.getType();
    return "input";
  }
  
  public String post() {
    MessageTemplate messageTemplate1 = ((MessageTemplateResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveMessageTemplate(this.messageTemplateId))).messageTemplate;
    MessageTemplate messageTemplate2 = ((MessageTemplateResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateMessageTemplate(this.messageTemplateId, new MessageTemplateRequest(this.messageTemplate)))).messageTemplate;
    writeAuditLogForUpdate("Update the message template with Id [" + String.valueOf(this.messageTemplateId) + "] and name [" + messageTemplate2.name + "]", messageTemplate1, messageTemplate2);
    return "success";
  }
}
