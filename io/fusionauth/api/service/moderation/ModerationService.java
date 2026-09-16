package io.fusionauth.api.service.moderation;

import io.fusionauth.api.service.moderation.cleanspeak.ModerateResponse;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import java.util.UUID;

public interface ModerationService {
  ModerateResponse moderate(User paramUser);
  
  ModerateResponse moderate(UserRegistration paramUserRegistration, Application paramApplication, UUID paramUUID);
}
