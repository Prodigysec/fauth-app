package io.fusionauth.app.action.ajax.email.template;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.validator.Validator;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.email.SendRequest;
import io.fusionauth.domain.api.email.SendResponse;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{emailTemplateId}", requiresAuthentication = true, constraints = {"admin", "email_template_manager"})
public class TestAction extends BaseAJAXAction {
  @FTLVariable
  public UUID emailTemplateId;
  
  @FTLVariable
  public String q;
  
  @JSONResponse
  public SendResponse result;
  
  @FTLVariable
  public UUID userId;
  
  @Inject
  public TestAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    ClientResponse<SendResponse, Errors> clientResponse = this.client.sendEmail(this.emailTemplateId, new SendRequest(List.of(this.userId)));
    this.result = (SendResponse)clientResponse.successResponse;
    return "render-json";
  }
  
  @ValidationMethod
  public void validate() {
    this.frontEndSupport.transfer((new Validator())
        .notMissing(this.userId, "q", new Object[0])
        .done());
  }
}
