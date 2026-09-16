package io.fusionauth.app.action.ajax.integration.kafka;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.util.CollectionTools;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.messaging.KafkaProducerException;
import io.fusionauth.api.service.messaging.KafkaService;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.KafkaConfiguration;
import io.fusionauth.domain.event.TestEvent;
import java.util.Map;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "system_manager"})
@JSON(code = "input", status = 400)
public class TestAction extends BaseAJAXAction {
  private final KafkaService kafkaService;
  
  public KafkaConfiguration configuration = new KafkaConfiguration();
  
  public String producerConfiguration;
  
  @JSONResponse
  public KafkaTestResponse response;
  
  @Inject
  public TestAction(FrontEndSupport paramFrontEndSupport, KafkaService paramKafkaService) {
    super(paramFrontEndSupport);
    this.kafkaService = paramKafkaService;
  }
  
  @ValidationMethod
  public void validate() {
    Errors errors = (new Validator()).notMissing(this.configuration.defaultTopic, "integrations.kafka.defaultTopic", new Object[0]).done();
    this.frontEndSupport.transfer(errors);
  }
  
  public String post() {
    Map<String, String> map = CollectionTools.stringToMap(this.producerConfiguration);
    if (map == null) {
      this.response = new KafkaTestResponse(this.frontEndSupport.messageProvider.getMessage("[invalid]producerConfiguration", new Object[0]), 400);
      return "render-json";
    } 
    String str = this.frontEndSupport.messageProvider.getMessage("success-message", new Object[0]);
    try {
      this.configuration.producer = map;
      this.kafkaService.testConfiguration(this.configuration, new TestEvent(str));
      this.response = new KafkaTestResponse(200);
    } catch (KafkaProducerException kafkaProducerException) {
      this.response = new KafkaTestResponse(kafkaProducerException.getMessage(), 400);
    } 
    return "render-json";
  }
  
  public static class KafkaTestResponse {
    public String message;
    
    public int status;
    
    public KafkaTestResponse(int param1Int) {
      this.status = param1Int;
    }
    
    public KafkaTestResponse(String param1String, int param1Int) {
      this.message = param1String;
      this.status = param1Int;
    }
  }
}
