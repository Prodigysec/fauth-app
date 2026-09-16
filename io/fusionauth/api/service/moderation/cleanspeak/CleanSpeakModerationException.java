package io.fusionauth.api.service.moderation.cleanspeak;

import org.primeframework.mvc.ErrorException;

public class CleanSpeakModerationException extends ErrorException {
  public CleanSpeakModerationException() {
    super("cleanspeak-moderation");
  }
}
