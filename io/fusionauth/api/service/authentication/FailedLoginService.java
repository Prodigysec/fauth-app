package io.fusionauth.api.service.authentication;

import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserActionLog;

public interface FailedLoginService {
  UserActionLog handleFailedLoginCountExceeded(Tenant paramTenant, User paramUser, EventInfo paramEventInfo);
}
