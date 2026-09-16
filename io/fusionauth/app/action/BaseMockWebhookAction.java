package io.fusionauth.app.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.Inject;
import io.fusionauth.api.domain.RuntimeMode;
import io.fusionauth.app.service.FrontEndSupport;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Status(code = "missing", status = 404)
public abstract class BaseMockWebhookAction extends BaseAction {
  private static final Logger logger = LoggerFactory.getLogger("MockWebhook");
  
  private final ObjectMapper objectMapper;
  
  @JSONRequest
  public Object request;
  
  @Inject
  protected BaseMockWebhookAction(FrontEndSupport paramFrontEndSupport, ObjectMapper paramObjectMapper) {
    super(paramFrontEndSupport);
    this.objectMapper = paramObjectMapper;
  }
  
  public String get() {
    if (this.frontEndSupport.configuration.runtimeMode() == RuntimeMode.Production)
      return "missing"; 
    return "success";
  }
  
  public String post() {
    if (this.frontEndSupport.configuration.runtimeMode() == RuntimeMode.Production)
      return "missing"; 
    char c = (getClass() == MockWebhookAction.class) ? 'È' : 'Ɛ';
    try {
      String str = this.objectMapper.writer().with(SerializationFeature.INDENT_OUTPUT).writeValueAsString(this.request);
      logger.info("Event received. Return status code [{}].\n{}\n", Integer.valueOf(c), str);
    } catch (JsonProcessingException jsonProcessingException) {
      logger.info("Event received but couldn't process the JSON.", (Throwable)jsonProcessingException);
    } 
    return "success";
  }
}
