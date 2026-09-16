package io.fusionauth.api.service.system;

import io.fusionauth.app.domain.Acquisition;
import io.fusionauth.domain.User;
import java.util.UUID;

public interface InstanceService {
  boolean activate(User paramUser, boolean paramBoolean, Acquisition paramAcquisition);
  
  void firstTimeSetupComplete();
  
  void kickstartActivate();
  
  UUID setupComplete();
  
  void setupIncomplete();
}
