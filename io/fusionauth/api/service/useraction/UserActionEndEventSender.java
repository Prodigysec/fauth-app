package io.fusionauth.api.service.useraction;

import com.google.inject.Inject;

public class UserActionEndEventSender implements Runnable {
  private final ActionService actionService;
  
  @Inject
  public UserActionEndEventSender(ActionService paramActionService) {
    this.actionService = paramActionService;
  }
  
  public void run() {
    this.actionService.sendEndEvents(null);
  }
}
