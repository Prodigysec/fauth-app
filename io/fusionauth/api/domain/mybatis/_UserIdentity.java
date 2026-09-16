package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.UserIdentity;
import java.util.UUID;

public class _UserIdentity extends UserIdentity {
  public UUID connectorId;
  
  public String email;
  
  public String uniqueUsername;
  
  public String username;
}
