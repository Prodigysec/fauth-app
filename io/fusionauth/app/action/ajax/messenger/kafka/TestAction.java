package io.fusionauth.app.action.ajax.messenger.kafka;

import com.google.inject.Inject;
import com.inversoft.util.CollectionTools;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.messenger.MessengerTestService;
import io.fusionauth.app.action.ajax.messenger.BaseTestAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.messenger.KafkaMessengerConfiguration;
import java.util.Map;
import java.util.Objects;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "system_manager"})
public class TestAction extends BaseTestAJAXAction {
  public final KafkaMessengerConfiguration messenger = new KafkaMessengerConfiguration();
  
  private final MessengerTestService messengerTestService;
  
  public String producerConfiguration;
  
  @JSONResponse
  public MessengerTestService.MessengerTestResult response;
  
  private Map<String, String> properties;
  
  @Inject
  public TestAction(FrontEndSupport paramFrontEndSupport, MessengerTestService paramMessengerTestService) {
    super(paramFrontEndSupport);
    this.messengerTestService = paramMessengerTestService;
  }
  
  public String post() {
    this.messenger.producer = this.properties;
    this.messenger.id = this.messengerId;
    this.response = this.messengerTestService.testKafka(this.messenger);
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    this.properties = CollectionTools.stringToMap(this.producerConfiguration);
    Objects.requireNonNull(this.frontEndSupport);
    (new Validator()).ensure((this.properties != null), "producerConfiguration", "[invalid]", new Object[0]).notBlank(this.messenger.defaultTopic, "messenger.defaultTopic", new Object[0]).done(this.frontEndSupport::transfer);
  }
}
