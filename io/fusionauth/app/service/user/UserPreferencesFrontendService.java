package io.fusionauth.app.service.user;

import io.fusionauth.domain.User;

public interface UserPreferencesFrontendService {
  void dismissLicenseCTA(User paramUser);
  
  boolean showUserLicenseCTA(User paramUser);
}
