package io.fusionauth.app.action.ajax.messenger.generic;

import com.google.inject.Inject;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.messenger.MessengerTestService;
import io.fusionauth.app.action.ajax.messenger.BaseTestAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.messenger.GenericMessengerConfiguration;
import java.util.Objects;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "messenger_manager"})
public class TestAction extends BaseTestAJAXAction {
  public final GenericMessengerConfiguration messenger = new GenericMessengerConfiguration();
  
  private final MessengerTestService messengerTestService;
  
  @JSONResponse
  public MessengerTestService.MessengerTestResult response;
  
  @Inject
  public TestAction(FrontEndSupport paramFrontEndSupport, MessengerTestService paramMessengerTestService) {
    super(paramFrontEndSupport);
    this.messengerTestService = paramMessengerTestService;
  }
  
  public String post() {
    this.messenger.id = this.messengerId;
    this.response = this.messengerTestService.testGeneric(this.messenger);
    return "render-json";
  }
  
  @ValidationMethod
  public void validate() {
    Objects.requireNonNull(this.frontEndSupport);
    (new Validator()).notMissing(this.messenger.url, "messenger.url", new Object[0]).notMissing(this.messenger.connectTimeout, "messenger.connectTimeout", new Object[0]).notMissing(this.messenger.readTimeout, "messenger.readTimeout", new Object[0]).done(this.frontEndSupport::transfer);
  }
}
