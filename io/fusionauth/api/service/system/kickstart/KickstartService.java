package io.fusionauth.api.service.system.kickstart;

import java.io.File;

public interface KickstartService {
  Thread kickstart();
  
  Thread kickstart(File paramFile);
}
