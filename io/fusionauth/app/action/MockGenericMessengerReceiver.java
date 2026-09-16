package io.fusionauth.app.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.Inject;
import io.fusionauth.app.service.FrontEndSupport;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Action
@Status
public class MockGenericMessengerReceiver extends BaseAction {
  private static final Logger logger = LoggerFactory.getLogger(MockGenericMessengerReceiver.class);
  
  private final ObjectMapper objectMapper;
  
  @JSONRequest
  public Object request;
  
  @Inject
  protected MockGenericMessengerReceiver(FrontEndSupport paramFrontEndSupport, ObjectMapper paramObjectMapper) {
    super(paramFrontEndSupport);
    this.objectMapper = paramObjectMapper;
  }
  
  public String post() {
    try {
      String str = this.objectMapper.writer().with(SerializationFeature.INDENT_OUTPUT).writeValueAsString(this.request);
      logger.info("Message\n{}\n", str);
    } catch (JsonProcessingException jsonProcessingException) {
      logger.info("Message received but couldn't process the JSON.", (Throwable)jsonProcessingException);
    } 
    return "success";
  }
}
