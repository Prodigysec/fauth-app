package io.fusionauth.api.service.messenger;

import com.inversoft.error.Errors;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;

public interface MessengerValidator {
  Errors validate(BaseMessengerConfiguration paramBaseMessengerConfiguration);
}
