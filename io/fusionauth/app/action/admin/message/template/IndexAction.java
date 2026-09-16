package io.fusionauth.app.action.admin.message.template;

import com.google.inject.Inject;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.MessageTemplateResponse;
import io.fusionauth.domain.message.MessageTemplate;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "message_template_manager"})
public class IndexAction extends BaseAction {
  public List<MessageTemplate> messageTemplates;
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.messageTemplates = ((MessageTemplateResponse)superDelegate().execute(FusionAuthClient::retrieveMessageTemplates)).messageTemplates;
    return "input";
  }
}
