package io.fusionauth.api.service.moderation.cleanspeak;

import org.primeframework.mvc.ErrorException;

public class CleanSpeakFailureException extends ErrorException {
  public CleanSpeakFailureException() {
    super("cleanspeak-failure");
  }
}
