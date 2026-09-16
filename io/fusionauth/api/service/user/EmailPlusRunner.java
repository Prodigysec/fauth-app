package io.fusionauth.api.service.user;

import com.google.inject.Inject;
import io.fusionauth.api.service.consent.ConsentService;

public class EmailPlusRunner implements Runnable {
  private final ConsentService consentService;
  
  @Inject
  public EmailPlusRunner(ConsentService paramConsentService) {
    this.consentService = paramConsentService;
  }
  
  public void run() {
    this.consentService.handleEmailPlusFollowup();
  }
}
