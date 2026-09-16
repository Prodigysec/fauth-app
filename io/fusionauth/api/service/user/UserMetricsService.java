package io.fusionauth.api.service.user;

import io.fusionauth.api.service.authentication.LoginQueue;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import java.time.ZonedDateTime;
import java.util.UUID;

public interface UserMetricsService {
  void addToLoginQueue(LoginQueue.LoginQueueRawLogin paramLoginQueueRawLogin);
  
  LoginQueue.LoginQueueRawLogin buildRawLogin(User paramUser, UserIdentity paramUserIdentity, ZonedDateTime paramZonedDateTime, UUID paramUUID, EventInfo paramEventInfo);
  
  void updateActiveUserMetrics(UUID paramUUID1, UUID paramUUID2);
}
