package io.fusionauth.domain.api.user;

import io.fusionauth.domain.DisplayableRawLogin;
import java.util.ArrayList;
import java.util.List;

public class RecentLoginResponse {
  public List<DisplayableRawLogin> logins = new ArrayList<>();
}
