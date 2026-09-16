package io.fusionauth.domain.api;

import java.util.List;
import java.util.UUID;

public class UserDeleteResponse {
  public boolean dryRun;
  
  public boolean hardDelete;
  
  public int total;
  
  public List<UUID> userIds;
}
