package io.fusionauth.app.domain;

import io.fusionauth.domain.EntityTypePermission;
import java.util.List;

public final class AIAgentPermissions {
  public static final String AIAgentCall = "call";
  
  public static List<EntityTypePermission> AIAgentEntityPermissions;
  
  static {
    AIAgentEntityPermissions = List.of((new EntityTypePermission())
        .with(paramEntityTypePermission -> paramEntityTypePermission.name = "call")
        .with(paramEntityTypePermission -> paramEntityTypePermission.description = "Call this agent"));
  }
  
  public static List<String> All = List.of("call");
}
