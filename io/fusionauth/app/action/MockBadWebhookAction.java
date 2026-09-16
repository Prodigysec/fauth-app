package io.fusionauth.app.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.fusionauth.app.service.FrontEndSupport;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Status;

@Action
@Status(status = 400)
public class MockBadWebhookAction extends BaseMockWebhookAction {
  @Inject
  protected MockBadWebhookAction(FrontEndSupport paramFrontEndSupport, ObjectMapper paramObjectMapper) {
    super(paramFrontEndSupport, paramObjectMapper);
  }
}
