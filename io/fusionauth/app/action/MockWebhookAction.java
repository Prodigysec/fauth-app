package io.fusionauth.app.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.fusionauth.app.service.FrontEndSupport;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Status;

@Action
@Status
public class MockWebhookAction extends BaseMockWebhookAction {
  @Inject
  protected MockWebhookAction(FrontEndSupport paramFrontEndSupport, ObjectMapper paramObjectMapper) {
    super(paramFrontEndSupport, paramObjectMapper);
  }
}
