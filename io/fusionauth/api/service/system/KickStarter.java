package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import io.fusionauth.api.service.system.kickstart.KickstartService;

public class KickStarter {
  @Inject
  public KickStarter(KickstartService paramKickstartService) {
    paramKickstartService.kickstart();
  }
}
