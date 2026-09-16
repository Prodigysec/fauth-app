package io.fusionauth.api.domain;

import io.fusionauth.domain.EntityTypePermission;
import java.util.List;

public final class SCIMServerPermissions {
  public static final String SCIMEnterpriseUserCreate = "scim:enterprise:user:create";
  
  public static final String SCIMEnterpriseUserDelete = "scim:enterprise:user:delete";
  
  public static final String SCIMEnterpriseUserRead = "scim:enterprise:user:read";
  
  public static final String SCIMEnterpriseUserUpdate = "scim:enterprise:user:update";
  
  public static final String SCIMGroupCreate = "scim:group:create";
  
  public static final String SCIMGroupDelete = "scim:group:delete";
  
  public static final String SCIMGroupRead = "scim:group:read";
  
  public static final String SCIMGroupUpdate = "scim:group:update";
  
  public static final String SCIMResourceTypesRead = "scim:resource-types:read";
  
  public static final String SCIMSchemasRead = "scim:schemas:read";
  
  public static final String SCIMServiceProviderConfigRead = "scim:service-provider-config:read";
  
  public static final String SCIMUserCreate = "scim:user:create";
  
  public static final String SCIMUserDelete = "scim:user:delete";
  
  public static final String SCIMUserRead = "scim:user:read";
  
  public static final String SCIMUserUpdate = "scim:user:update";
  
  public static List<String> All = List.of((Object[])new String[] { 
        "scim:user:create", "scim:user:read", "scim:user:update", "scim:user:delete", "scim:enterprise:user:create", "scim:enterprise:user:read", "scim:enterprise:user:update", "scim:enterprise:user:delete", "scim:group:create", "scim:group:read", 
        "scim:group:update", "scim:group:delete", "scim:resource-types:read", "scim:schemas:read", "scim:service-provider-config:read" });
  
  public static List<EntityTypePermission> SCIMServerEntityPermissions;
  
  static {
    SCIMServerEntityPermissions = List.of((Object[])new EntityTypePermission[] { 
          (new EntityTypePermission()).with(paramEntityTypePermission -> paramEntityTypePermission.name = "scim:user:create")
          .with(paramEntityTypePermission -> paramEntityTypePermission.description = "Create SCIM User"), (new EntityTypePermission())
          .with(paramEntityTypePermission -> paramEntityTypePermission.name = "scim:user:read")
          .with(paramEntityTypePermission -> paramEntityTypePermission.description = "Read SCIM User"), (new EntityTypePermission())
          .with(paramEntityTypePermission -> paramEntityTypePermission.name = "scim:user:update")
          .with(paramEntityTypePermission -> paramEntityTypePermission.description = "Update SCIM User"), (new EntityTypePermission())
          .with(paramEntityTypePermission -> paramEntityTypePermission.name = "scim:user:delete")
          .with(paramEntityTypePermission -> paramEntityTypePermission.description = "Delete SCIM User"), (new EntityTypePermission())
          .with(paramEntityTypePermission -> paramEntityTypePermission.name = "scim:enterprise:user:create")
          .with(paramEntityTypePermission -> paramEntityTypePermission.description = "Create SCIM Enterprise User"), (new EntityTypePermission())
          .with(paramEntityTypePermission -> paramEntityTypePermission.name = "scim:enterprise:user:read")
          .with(paramEntityTypePermission -> paramEntityTypePermission.description = "Read SCIM Enterprise User"), (new EntityTypePermission())
          .with(paramEntityTypePermission -> paramEntityTypePermission.name = "scim:enterprise:user:update")
          .with(paramEntityTypePermission -> paramEntityTypePermission.description = "Update SCIM Enterprise User"), (new EntityTypePermission())
          .with(paramEntityTypePermission -> paramEntityTypePermission.name = "scim:enterprise:user:delete")
          .with(paramEntityTypePermission -> paramEntityTypePermission.description = "Delete SCIM Enterprise User"), (new EntityTypePermission())
          .with(paramEntityTypePermission -> paramEntityTypePermission.name = "scim:group:create")
          .with(paramEntityTypePermission -> paramEntityTypePermission.description = "Create SCIM Group"), (new EntityTypePermission())
          .with(paramEntityTypePermission -> paramEntityTypePermission.name = "scim:group:read")
          .with(paramEntityTypePermission -> paramEntityTypePermission.description = "Read SCIM Group"), 
          (new EntityTypePermission())
          .with(paramEntityTypePermission -> paramEntityTypePermission.name = "scim:group:update")
          .with(paramEntityTypePermission -> paramEntityTypePermission.description = "Update SCIM Group"), (new EntityTypePermission())
          .with(paramEntityTypePermission -> paramEntityTypePermission.name = "scim:group:delete")
          .with(paramEntityTypePermission -> paramEntityTypePermission.description = "Delete SCIM Group"), (new EntityTypePermission())
          .with(paramEntityTypePermission -> paramEntityTypePermission.name = "scim:resource-types:read")
          .with(paramEntityTypePermission -> paramEntityTypePermission.description = "Read SCIM Resource Types"), (new EntityTypePermission())
          .with(paramEntityTypePermission -> paramEntityTypePermission.name = "scim:schemas:read")
          .with(paramEntityTypePermission -> paramEntityTypePermission.description = "Read SCIM Schemas"), (new EntityTypePermission())
          .with(paramEntityTypePermission -> paramEntityTypePermission.name = "scim:service-provider-config:read")
          .with(paramEntityTypePermission -> paramEntityTypePermission.description = "Read SCIM Service Provider Configuration") });
  }
}
