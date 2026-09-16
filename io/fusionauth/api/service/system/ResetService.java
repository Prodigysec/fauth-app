package io.fusionauth.api.service.system;

import io.fusionauth.api.domain.KickstartFile;
import io.fusionauth.api.service.lock.ResetDistributedLock;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public interface ResetService {
  void createKickstart(String paramString, Path paramPath);
  
  void reset(String paramString, int paramInt, boolean paramBoolean1, ResetDistributedLock paramResetDistributedLock, boolean paramBoolean2);
  
  void resetAfterTest();
  
  Thread resetToKickstart(UUID paramUUID);
  
  List<KickstartFile> retrieveAllKickstartFiles();
}
