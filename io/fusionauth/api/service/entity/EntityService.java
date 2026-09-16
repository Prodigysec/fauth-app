package io.fusionauth.api.service.entity;

import com.inversoft.error.Errors;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.EntityGrant;
import io.fusionauth.domain.EntityType;
import io.fusionauth.domain.EntityTypePermission;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.api.EntityGrantSearchRequest;
import io.fusionauth.domain.search.EntityGrantSearchCriteria;
import io.fusionauth.domain.search.EntityTypeSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.SortField;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

public interface EntityService {
  Entity createEntity(Tenant paramTenant, Entity paramEntity, @Nullable EventInfo paramEventInfo);
  
  void createPermission(EntityTypePermission paramEntityTypePermission);
  
  void createType(EntityType paramEntityType);
  
  void deleteEntity(Tenant paramTenant, Entity paramEntity, @Nullable EventInfo paramEventInfo);
  
  void deleteGrant(EntityGrant paramEntityGrant);
  
  void deletePermission(EntityTypePermission paramEntityTypePermission);
  
  void deleteType(EntityType paramEntityType);
  
  void refreshSearchIndex();
  
  List<EntityType> retrieveAllTypes();
  
  Entity retrieveByClientId(UUID paramUUID, String paramString);
  
  List<Entity> retrieveByIds(@Nullable UUID paramUUID, List<UUID> paramList);
  
  Entity retrieveEntityById(UUID paramUUID1, UUID paramUUID2);
  
  EntityGrant retrieveEntityGrantForEntity(Entity paramEntity1, Entity paramEntity2);
  
  List<EntityGrant> retrieveGrantsForEntity(UUID paramUUID);
  
  EntityTypePermission retrievePermissionById(UUID paramUUID1, UUID paramUUID2);
  
  EntityType retrieveTypeById(UUID paramUUID);
  
  SearchResults<Entity> searchByQuery(@Nullable UUID paramUUID, @Nullable String paramString1, int paramInt1, @Nullable List<SortField> paramList, int paramInt2, boolean paramBoolean, @Nullable List<String> paramList1, @Nullable String paramString2);
  
  SearchResults<Entity> searchByQueryString(@Nullable UUID paramUUID, @Nullable String paramString1, int paramInt1, int paramInt2, @Nullable List<SortField> paramList, boolean paramBoolean, @Nullable List<String> paramList1, @Nullable String paramString2);
  
  SearchResults<EntityGrant> searchGrants(EntityGrantSearchCriteria paramEntityGrantSearchCriteria);
  
  SearchResults<EntityType> searchTypes(EntityTypeSearchCriteria paramEntityTypeSearchCriteria);
  
  void updateEntity(Tenant paramTenant, Entity paramEntity1, Entity paramEntity2, @Nullable EventInfo paramEventInfo);
  
  EntityTypePermission updatePermission(EntityTypePermission paramEntityTypePermission);
  
  void updateType(EntityType paramEntityType1, EntityType paramEntityType2);
  
  void upsertGrant(Entity paramEntity, EntityGrant paramEntityGrant1, EntityGrant paramEntityGrant2, List<EntityTypePermission> paramList);
  
  ValidationResult validateEntityCreate(Entity paramEntity);
  
  ValidationResult validateEntityDelete(Tenant paramTenant, UUID paramUUID);
  
  ValidationResult validateEntityUpdate(Tenant paramTenant, Entity paramEntity);
  
  GrantValidationResult validateGrantDelete(Entity paramEntity, @Nullable UUID paramUUID1, @Nullable UUID paramUUID2);
  
  GrantValidationResult validateGrantRetrieve(Entity paramEntity, @Nullable UUID paramUUID1, @Nullable UUID paramUUID2);
  
  GrantValidationResult validateGrantUpsert(Entity paramEntity, EntityGrant paramEntityGrant);
  
  PermissionValidationResult validatePermissionCreate(UUID paramUUID, EntityTypePermission paramEntityTypePermission);
  
  PermissionValidationResult validatePermissionDelete(UUID paramUUID1, UUID paramUUID2, String paramString);
  
  PermissionValidationResult validatePermissionUpdate(UUID paramUUID, EntityTypePermission paramEntityTypePermission);
  
  Errors validateSearchQuery(@Nullable UUID paramUUID, @Nullable String paramString1, @Nullable String paramString2, @Nullable List<SortField> paramList, int paramInt1, int paramInt2);
  
  ValidationResult validateSearchRequest(EntityGrantSearchRequest paramEntityGrantSearchRequest, String paramString);
  
  TypeValidationResult validateTypeCreate(EntityType paramEntityType);
  
  TypeValidationResult validateTypeDelete(UUID paramUUID);
  
  TypeValidationResult validateTypeUpdate(EntityType paramEntityType);
  
  public static class GrantValidationResult {
    public Errors errors;
    
    public EntityGrant existing;
    
    public List<EntityTypePermission> permissions = new ArrayList<>();
  }
  
  public static class PermissionValidationResult {
    public EntityType entityType;
    
    public Errors errors;
    
    public EntityType existing;
    
    public EntityTypePermission permission;
  }
  
  public static class TypeValidationResult {
    public Errors errors;
    
    public EntityType existing;
  }
  
  public static class ValidationResult {
    public Errors errors;
    
    public Entity existing;
  }
}
