package io.fusionauth.app.action.ajax.message.template;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.MessageTemplateResponse;
import io.fusionauth.domain.message.MessageTemplate;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{messageTemplateId}", requiresAuthentication = true, constraints = {"admin", "message_template_deleter"})
public class DeleteAction extends BaseAJAXAction {
  public UUID messageTemplateId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    MessageTemplate messageTemplate = ((MessageTemplateResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveMessageTemplate(this.messageTemplateId))).messageTemplate;
    superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.deleteMessageTemplate(this.messageTemplateId));
    writeAuditLog("Deleted the message template with id[ [" + String.valueOf(this.messageTemplateId) + "] and name [" + messageTemplate.name + "]");
    return "success";
  }
}
