package io.fusionauth.api.service.entity;

import com.google.inject.Inject;
import com.inversoft.cache.CacheNotifier;
import com.inversoft.error.Errors;
import com.inversoft.mybatis.MyBatisTools;
import com.inversoft.util.SecurityTools;
import com.inversoft.util.StringTools;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.EntityMapper;
import io.fusionauth.api.domain.EntityTypeKeyType;
import io.fusionauth.api.domain.SCIMMapper;
import io.fusionauth.api.domain.SearchEngineResult;
import io.fusionauth.api.domain.UserMapper;
import io.fusionauth.api.service.search.EntitySearchEngine;
import io.fusionauth.api.service.system.EventHelper;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.api.util.InUseValidator;
import io.fusionauth.api.util.KeyValidator;
import io.fusionauth.api.util.MapperTools;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.EntityGrant;
import io.fusionauth.domain.EntityType;
import io.fusionauth.domain.EntityTypePermission;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.EntityGrantSearchRequest;
import io.fusionauth.domain.event.EntityCreateCompleteEvent;
import io.fusionauth.domain.event.EntityCreateEvent;
import io.fusionauth.domain.event.EntityDeleteCompleteEvent;
import io.fusionauth.domain.event.EntityDeleteEvent;
import io.fusionauth.domain.event.EntityUpdateCompleteEvent;
import io.fusionauth.domain.event.EntityUpdateEvent;
import io.fusionauth.domain.search.EntityGrantSearchCriteria;
import io.fusionauth.domain.search.EntityTypeSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.SortField;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.UUID;
import javax.annotation.Nullable;
import org.mybatis.guice.transactional.Transactional;

public class DefaultEntityService implements EntityService {
  private final CacheNotifier cacheNotifier;
  
  private final EntityMapper entityMapper;
  
  private final EntitySearchEngine entitySearchEngine;
  
  private final InUseValidator inUseValidator;
  
  private final KeyValidator keyValidator;
  
  private final SCIMMapper scimMapper;
  
  private final TenantReaderService tenantReader;
  
  private final UserMapper userMapper;
  
  @Inject
  public DefaultEntityService(CacheNotifier paramCacheNotifier, EntityMapper paramEntityMapper, EntitySearchEngine paramEntitySearchEngine, InUseValidator paramInUseValidator, KeyValidator paramKeyValidator, SCIMMapper paramSCIMMapper, TenantReaderService paramTenantReaderService, UserMapper paramUserMapper) {
    this.cacheNotifier = paramCacheNotifier;
    this.entityMapper = paramEntityMapper;
    this.entitySearchEngine = paramEntitySearchEngine;
    this.inUseValidator = paramInUseValidator;
    this.keyValidator = paramKeyValidator;
    this.scimMapper = paramSCIMMapper;
    this.tenantReader = paramTenantReaderService;
    this.userMapper = paramUserMapper;
  }
  
  @Transactional
  public Entity _createEntity(Tenant paramTenant, Entity paramEntity, @Nullable EventInfo paramEventInfo) {
    if (paramEntity.id == null)
      paramEntity.id = UUID.randomUUID(); 
    if (StringTools.isTrimmedEmpty(paramEntity.clientId))
      paramEntity.clientId = paramEntity.id.toString(); 
    if (StringTools.isTrimmedEmpty(paramEntity.clientSecret))
      paramEntity.clientSecret = SecurityTools.secureRandom(); 
    paramEntity.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramEntity.lastUpdateInstant = paramEntity.insertInstant;
    paramEntity.tenantId = paramTenant.id;
    this.entityMapper.createEntity(paramEntity);
    paramEntity = this.entityMapper.retrieveEntityById(paramTenant.id, paramEntity.id);
    EventHelper.send(paramTenant, null, new EntityCreateEvent(paramEventInfo, paramEntity));
    this.entitySearchEngine.index(List.of(paramEntity));
    return paramEntity;
  }
  
  @Transactional
  public void _createType(EntityType paramEntityType) {
    if (paramEntityType.id == null)
      paramEntityType.id = UUID.randomUUID(); 
    paramEntityType.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramEntityType.lastUpdateInstant = paramEntityType.insertInstant;
    this.entityMapper.createType(paramEntityType);
    writeVerificationKeys(paramEntityType);
    createPermissions(paramEntityType);
  }
  
  @Transactional
  public void _deleteEntity(Tenant paramTenant, Entity paramEntity, @Nullable EventInfo paramEventInfo) {
    this.entityMapper.deleteEntity(paramEntity.id);
    EventHelper.send(paramTenant, null, new EntityDeleteEvent(paramEventInfo, paramEntity));
    this.entitySearchEngine.delete(paramEntity.id);
  }
  
  @Transactional
  public void _deleteType(EntityType paramEntityType) {
    this.entityMapper.deleteTypeVerificationKeys(paramEntityType.id);
    this.entityMapper.deleteType(paramEntityType.id);
    this.entitySearchEngine.deleteByType(paramEntityType.id);
  }
  
  @Transactional
  public void _updateEntity(Tenant paramTenant, Entity paramEntity1, Entity paramEntity2, @Nullable EventInfo paramEventInfo) {
    paramEntity2.clientId = paramEntity1.clientId;
    paramEntity2.id = paramEntity1.id;
    paramEntity2.insertInstant = paramEntity1.insertInstant;
    paramEntity2.tenantId = paramEntity1.tenantId;
    paramEntity2.type = paramEntity1.type;
    paramEntity2.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    if (StringTools.isTrimmedEmpty(paramEntity2.clientSecret))
      paramEntity2.clientSecret = paramEntity1.clientSecret; 
    this.entityMapper.updateEntity(paramEntity2);
    EventHelper.send(paramTenant, null, new EntityUpdateEvent(paramEventInfo, paramEntity1, paramEntity2));
    this.entitySearchEngine.index(List.of(paramEntity2));
  }
  
  @Transactional
  public void _updateType(EntityType paramEntityType1, EntityType paramEntityType2) {
    paramEntityType2.insertInstant = paramEntityType1.insertInstant;
    paramEntityType2.permissions.clear();
    paramEntityType2.permissions.addAll(paramEntityType1.permissions);
    paramEntityType2.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    this.entityMapper.updateType(paramEntityType2);
    writeVerificationKeys(paramEntityType2);
  }
  
  public Entity createEntity(Tenant paramTenant, Entity paramEntity, @Nullable EventInfo paramEventInfo) {
    paramEntity = _createEntity(paramTenant, paramEntity, paramEventInfo);
    EventHelper.send(paramTenant, null, new EntityCreateCompleteEvent(paramEventInfo, paramEntity));
    return paramEntity;
  }
  
  public void createPermission(EntityTypePermission paramEntityTypePermission) {
    if (paramEntityTypePermission.id == null)
      paramEntityTypePermission.id = UUID.randomUUID(); 
    paramEntityTypePermission.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramEntityTypePermission.lastUpdateInstant = paramEntityTypePermission.insertInstant;
    this.entityMapper.createPermission(paramEntityTypePermission);
  }
  
  public void createType(EntityType paramEntityType) {
    _createType(paramEntityType);
    this.cacheNotifier.reload("Keys");
  }
  
  public void deleteEntity(Tenant paramTenant, Entity paramEntity, @Nullable EventInfo paramEventInfo) {
    _deleteEntity(paramTenant, paramEntity, paramEventInfo);
    EventHelper.send(paramTenant, null, new EntityDeleteCompleteEvent(paramEventInfo, paramEntity));
  }
  
  public void deleteGrant(EntityGrant paramEntityGrant) {
    if (paramEntityGrant.userId != null) {
      this.entityMapper.deleteUserGrant(paramEntityGrant.id);
    } else {
      this.entityMapper.deleteEntityGrant(paramEntityGrant.id);
    } 
  }
  
  public void deletePermission(EntityTypePermission paramEntityTypePermission) {
    this.entityMapper.deletePermission(paramEntityTypePermission.id);
  }
  
  public void deleteType(EntityType paramEntityType) {
    _deleteType(paramEntityType);
    this.cacheNotifier.reload("Keys");
  }
  
  public void refreshSearchIndex() {
    this.entitySearchEngine.refresh();
  }
  
  public List<EntityType> retrieveAllTypes() {
    return this.entityMapper.retrieveAllTypes();
  }
  
  public Entity retrieveByClientId(UUID paramUUID, String paramString) {
    return this.entityMapper.retrieveEntityByClientId(paramUUID, paramString);
  }
  
  public List<Entity> retrieveByIds(@Nullable UUID paramUUID, List<UUID> paramList) {
    if (paramList == null || paramList.isEmpty())
      return Collections.emptyList(); 
    List<?> list = MapperTools.safeRetrieve(10000, paramList, paramList -> this.entityMapper.retrieveEntitiesByIds(paramUUID, paramList));
    list = MyBatisTools.order(list, paramList, paramEntity -> paramEntity.id);
    list.forEach(paramEntity -> paramEntity.type.permissions.sort(Comparator.comparing(())));
    return (List)list;
  }
  
  public Entity retrieveEntityById(UUID paramUUID1, UUID paramUUID2) {
    return this.entityMapper.retrieveEntityById(paramUUID1, paramUUID2);
  }
  
  public EntityGrant retrieveEntityGrantForEntity(Entity paramEntity1, Entity paramEntity2) {
    return this.entityMapper.retrieveEntityGrantForEntity(paramEntity1.id, paramEntity2.id);
  }
  
  public List<EntityGrant> retrieveGrantsForEntity(UUID paramUUID) {
    ArrayList<EntityGrant> arrayList = new ArrayList();
    arrayList.addAll(this.entityMapper.retrieveEntityGrantsForEntity(paramUUID));
    arrayList.addAll(this.entityMapper.retrieveUserGrantsForEntity(paramUUID));
    arrayList.sort((paramEntityGrant1, paramEntityGrant2) -> 
        (paramEntityGrant1.recipientEntityId != null && paramEntityGrant2.recipientEntityId != null) ? paramEntityGrant1.recipientEntityId.compareTo(paramEntityGrant2.recipientEntityId) : (

        
        (paramEntityGrant1.userId != null && paramEntityGrant2.userId != null) ? paramEntityGrant1.userId.compareTo(paramEntityGrant2.userId) : ((paramEntityGrant1.userId != null) ? -1 : 1)));
    return arrayList;
  }
  
  public EntityTypePermission retrievePermissionById(UUID paramUUID1, UUID paramUUID2) {
    return this.entityMapper.retrievePermissionById(paramUUID1, paramUUID2);
  }
  
  public EntityType retrieveTypeById(UUID paramUUID) {
    return this.entityMapper.retrieveTypeById(paramUUID);
  }
  
  public SearchResults<Entity> searchByQuery(@Nullable UUID paramUUID, @Nullable String paramString1, int paramInt1, @Nullable List<SortField> paramList, int paramInt2, boolean paramBoolean, @Nullable List<String> paramList1, @Nullable String paramString2) {
    SearchEngineResult searchEngineResult = this.entitySearchEngine.searchByQuery(paramUUID, paramString1, paramInt1, paramInt2, paramList, paramBoolean, paramList1, paramString2);
    return fetchAndSort(paramUUID, searchEngineResult);
  }
  
  public SearchResults<Entity> searchByQueryString(@Nullable UUID paramUUID, @Nullable String paramString1, int paramInt1, int paramInt2, @Nullable List<SortField> paramList, boolean paramBoolean, @Nullable List<String> paramList1, @Nullable String paramString2) {
    SearchEngineResult searchEngineResult = this.entitySearchEngine.searchByQueryString(paramUUID, paramString1, paramInt2, paramInt1, paramList, paramBoolean, paramList1, paramString2);
    return fetchAndSort(paramUUID, searchEngineResult);
  }
  
  public SearchResults<EntityGrant> searchGrants(EntityGrantSearchCriteria paramEntityGrantSearchCriteria) {
    if (paramEntityGrantSearchCriteria.entityId == null && paramEntityGrantSearchCriteria.userId == null)
      return new SearchResults<>(List.of(), 0L); 
    int i = this.entityMapper.countGrants(paramEntityGrantSearchCriteria);
    List<UUID> list = this.entityMapper.retrieveGrantIdsByCriteria(paramEntityGrantSearchCriteria);
    List<?> list1 = List.of();
    if (list.size() > 0) {
      if (paramEntityGrantSearchCriteria.userId != null) {
        Objects.requireNonNull(this.entityMapper);
        list1 = MapperTools.safeRetrieve(32000, list, this.entityMapper::retrieveUserGrantsByIds);
      } else if (paramEntityGrantSearchCriteria.entityId != null) {
        Objects.requireNonNull(this.entityMapper);
        list1 = MapperTools.safeRetrieve(32000, list, this.entityMapper::retrieveEntityGrantsByIds);
      } 
      list1 = MyBatisTools.order(list1, list, paramEntityGrant -> paramEntityGrant.id);
    } 
    return new SearchResults(list1, i);
  }
  
  public SearchResults<EntityType> searchTypes(EntityTypeSearchCriteria paramEntityTypeSearchCriteria) {
    int i = this.entityMapper.countTypes(paramEntityTypeSearchCriteria);
    List<UUID> list = this.entityMapper.retrieveTypeIdsByCriteria(paramEntityTypeSearchCriteria);
    Objects.requireNonNull(this.entityMapper);
    List<?> list1 = MapperTools.safeRetrieve(32000, list, this.entityMapper::retrieveTypesByIds);
    list1 = MyBatisTools.order(list1, list, paramEntityType -> paramEntityType.id);
    return new SearchResults(list1, i);
  }
  
  public void updateEntity(Tenant paramTenant, Entity paramEntity1, Entity paramEntity2, @Nullable EventInfo paramEventInfo) {
    _updateEntity(paramTenant, paramEntity1, paramEntity2, paramEventInfo);
    EventHelper.send(paramTenant, null, new EntityUpdateCompleteEvent(paramEventInfo, paramEntity1, paramEntity2));
  }
  
  public EntityTypePermission updatePermission(EntityTypePermission paramEntityTypePermission) {
    paramEntityTypePermission.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    this.entityMapper.updatePermission(paramEntityTypePermission);
    return this.entityMapper.retrievePermissionById(paramEntityTypePermission.entityTypeId, paramEntityTypePermission.id);
  }
  
  public void updateType(EntityType paramEntityType1, EntityType paramEntityType2) {
    _updateType(paramEntityType1, paramEntityType2);
    this.cacheNotifier.reload("Keys");
  }
  
  @Transactional
  public void upsertGrant(Entity paramEntity, EntityGrant paramEntityGrant1, EntityGrant paramEntityGrant2, List<EntityTypePermission> paramList) {
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    paramEntityGrant2.lastUpdateInstant = zonedDateTime;
    if (paramEntityGrant1 != null) {
      if (paramEntityGrant1.userId != null) {
        this.entityMapper.deleteUserGrantPermissions(paramEntityGrant1.id);
      } else {
        this.entityMapper.deleteEntityGrantPermissions(paramEntityGrant1.id);
      } 
      paramEntityGrant2.id = paramEntityGrant1.id;
      paramEntityGrant2.insertInstant = paramEntityGrant1.insertInstant;
    } else {
      paramEntityGrant2.id = UUID.randomUUID();
      paramEntityGrant2.insertInstant = zonedDateTime;
    } 
    if (paramEntityGrant2.userId != null) {
      this.entityMapper.upsertUserGrant(paramEntity, paramEntityGrant2);
      MapperTools.safeCreateUpdate(5000, paramList, paramList -> this.entityMapper.createUserGrantPermissions(paramEntityGrant, paramList));
    } else {
      this.entityMapper.upsertEntityGrant(paramEntity, paramEntityGrant2);
      MapperTools.safeCreateUpdate(5000, paramList, paramList -> this.entityMapper.createEntityGrantPermissions(paramEntityGrant, paramList));
    } 
  }
  
  public EntityService.ValidationResult validateEntityCreate(Entity paramEntity) {
    EntityService.ValidationResult validationResult = new EntityService.ValidationResult();
    validationResult






      
      .errors = (new Validator()).ifTrue((paramEntity.id != null), paramValidator -> paramValidator.notDuplicate(this.entityMapper.retrieveEntityById(null, paramEntity.id), "entityId", new Object[] { paramEntity.id })).ifFalse(StringTools.isTrimmedEmpty(paramEntity.clientId), paramValidator -> paramValidator.notDuplicate(this.entityMapper.retrieveEntityByClientId(null, paramEntity.clientId), "entity.clientId", new Object[] { paramEntity.clientId })).notBlank(paramEntity.name, "entity.name", new Object[0]).maxLength(paramEntity.name, 255, "entity.name", new Object[0]).notMissing(paramEntity.type, "entity.type", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.notMissing(paramEntity.type.id, "entity.type.id", new Object[0])).ifLastCheckHadNoError(paramValidator -> paramValidator.validObject(this.entityMapper.retrieveTypeById(paramEntity.type.id), "entity.type.id", new Object[] { paramEntity.type.id })).done();
    return validationResult;
  }
  
  public EntityService.ValidationResult validateEntityDelete(Tenant paramTenant, UUID paramUUID) {
    EntityService.ValidationResult validationResult = new EntityService.ValidationResult();
    validationResult.existing = this.entityMapper.retrieveEntityById(paramTenant.id, paramUUID);
    if (validationResult.existing == null)
      return validationResult; 
    validationResult

      
      .errors = (new Validator()).notInUse(!this.scimMapper.isEntityIdInUseByGroups(paramUUID), "entityId", new Object[] { paramUUID, "SCIM Client mapping SCIM Group external Ids" }).notInUse(!this.scimMapper.isEntityIdInUseByUsers(paramUUID), "entityId", new Object[] { paramUUID, "SCIM Client mapping SCIM User external Ids" }).done();
    return validationResult;
  }
  
  public EntityService.ValidationResult validateEntityUpdate(Tenant paramTenant, Entity paramEntity) {
    EntityService.ValidationResult validationResult = new EntityService.ValidationResult();
    validationResult.existing = (paramEntity.id != null) ? this.entityMapper.retrieveEntityById(paramTenant.id, paramEntity.id) : null;
    validationResult

      
      .errors = (new Validator()).notMissing(paramEntity.id, "entityId", new Object[0]).notBlank(paramEntity.name, "entity.name", new Object[0]).maxLength(paramEntity.name, 255, "entity.name", new Object[0]).done();
    return validationResult;
  }
  
  public EntityService.GrantValidationResult validateGrantDelete(Entity paramEntity, @Nullable UUID paramUUID1, @Nullable UUID paramUUID2) {
    EntityService.GrantValidationResult grantValidationResult = new EntityService.GrantValidationResult();
    grantValidationResult.errors = new Errors();
    if (paramUUID1 != null) {
      grantValidationResult.existing = this.entityMapper.retrieveGrantForUser(paramEntity.id, paramUUID1);
    } else if (paramUUID2 != null) {
      grantValidationResult.existing = this.entityMapper.retrieveEntityGrantForEntity(paramUUID2, paramEntity.id);
    } else {
      grantValidationResult.errors.addFieldError("grant", "[invalid]grant", null, new Object[0]);
    } 
    return grantValidationResult;
  }
  
  public EntityService.GrantValidationResult validateGrantRetrieve(Entity paramEntity, @Nullable UUID paramUUID1, @Nullable UUID paramUUID2) {
    EntityService.GrantValidationResult grantValidationResult = new EntityService.GrantValidationResult();
    if (paramUUID1 == null && paramUUID2 == null)
      return grantValidationResult; 
    if (paramUUID1 != null) {
      grantValidationResult.existing = this.entityMapper.retrieveGrantForUser(paramEntity.id, paramUUID1);
    } else {
      grantValidationResult.existing = this.entityMapper.retrieveEntityGrantForEntity(paramUUID2, paramEntity.id);
    } 
    return grantValidationResult;
  }
  
  public EntityService.GrantValidationResult validateGrantUpsert(Entity paramEntity, EntityGrant paramEntityGrant) {
    EntityService.GrantValidationResult grantValidationResult = new EntityService.GrantValidationResult();
    grantValidationResult.errors = new Errors();
    if (paramEntityGrant == null) {
      grantValidationResult.errors.addFieldError("grant", "[missing]grant", null, new Object[0]);
      return grantValidationResult;
    } 
    if (paramEntityGrant.userId != null) {
      grantValidationResult.existing = this.entityMapper.retrieveGrantForUser(paramEntity.id, paramEntityGrant.userId);
      User user = this.userMapper.retrieveById(paramEntity.tenantId, paramEntityGrant.userId);
      if (user == null)
        grantValidationResult.errors.addFieldError("grant.userId", "[invalid]grant.userId", null, new Object[] { paramEntityGrant.userId }); 
    } else if (paramEntityGrant.recipientEntityId != null) {
      grantValidationResult.existing = this.entityMapper.retrieveEntityGrantForEntity(paramEntityGrant.recipientEntityId, paramEntity.id);
      Entity entity = retrieveEntityById(paramEntity.tenantId, paramEntityGrant.recipientEntityId);
      if (entity == null)
        grantValidationResult.errors.addFieldError("grant.recipientEntityId", "[invalid]grant.recipientEntityId", null, new Object[0]); 
    } else {
      grantValidationResult.errors.addFieldError("grant", "[invalid]grant", null, new Object[0]);
    } 
    if (paramEntityGrant.permissions.isEmpty()) {
      paramEntity.type.permissions.stream()
        .filter(paramEntityTypePermission -> paramEntityTypePermission.isDefault)
        .forEach(paramEntityTypePermission -> paramGrantValidationResult.permissions.add(paramEntityTypePermission));
    } else {
      TreeSet<String> treeSet = new TreeSet();
      for (String str : paramEntityGrant.permissions) {
        EntityTypePermission entityTypePermission = paramEntity.type.permissions.stream().filter(paramEntityTypePermission -> paramEntityTypePermission.name.equals(paramString)).findFirst().orElse(null);
        if (entityTypePermission == null) {
          treeSet.add(str);
          continue;
        } 
        grantValidationResult.permissions.add(entityTypePermission);
      } 
      if (treeSet.size() > 0)
        grantValidationResult.errors.addFieldError("grant.permissions", "[invalid]grant.permissions", null, new Object[] { String.join(", ", (Iterable)treeSet) }); 
    } 
    return grantValidationResult;
  }
  
  public EntityService.PermissionValidationResult validatePermissionCreate(UUID paramUUID, EntityTypePermission paramEntityTypePermission) {
    EntityService.PermissionValidationResult permissionValidationResult = new EntityService.PermissionValidationResult();
    permissionValidationResult.entityType = (paramUUID != null) ? retrieveTypeById(paramUUID) : null;
    permissionValidationResult



      
      .errors = (new Validator()).notBlank(paramEntityTypePermission.name, "permission.name", new Object[0]).maxLength(paramEntityTypePermission.description, 255, "permission.description", new Object[0]).ifNoErrors(paramValidator -> paramValidator.notDuplicate(this.entityMapper.retrievePermissionByName(paramUUID, paramEntityTypePermission.name), "permission.name", new Object[] { paramEntityTypePermission.name })).ifTrue((paramEntityTypePermission.id != null), paramValidator -> paramValidator.notDuplicate(this.entityMapper.retrievePermissionById(paramUUID, paramEntityTypePermission.id), "permissionId", new Object[] { paramEntityTypePermission.id })).validObject(permissionValidationResult.entityType, "entityTypeId", new Object[] { paramUUID }).done();
    return permissionValidationResult;
  }
  
  public EntityService.PermissionValidationResult validatePermissionDelete(UUID paramUUID1, UUID paramUUID2, String paramString) {
    EntityService.PermissionValidationResult permissionValidationResult = new EntityService.PermissionValidationResult();
    if (paramUUID2 != null) {
      permissionValidationResult.permission = this.entityMapper.retrievePermissionById(paramUUID1, paramUUID2);
    } else if (paramString != null) {
      permissionValidationResult.permission = this.entityMapper.retrievePermissionByName(paramUUID1, paramString);
    } 
    permissionValidationResult
      .errors = (new Validator()).notMissing(paramUUID1, "entityTypeId", new Object[0]).done();
    return permissionValidationResult;
  }
  
  public EntityService.PermissionValidationResult validatePermissionUpdate(UUID paramUUID, EntityTypePermission paramEntityTypePermission) {
    EntityService.PermissionValidationResult permissionValidationResult = new EntityService.PermissionValidationResult();
    permissionValidationResult.permission = (paramUUID != null && paramEntityTypePermission.id != null) ? this.entityMapper.retrievePermissionById(paramUUID, paramEntityTypePermission.id) : null;
    permissionValidationResult.entityType = this.entityMapper.retrieveTypeById(paramUUID);
    permissionValidationResult

      
      .errors = (new Validator()).notMissing(paramEntityTypePermission.id, "permissionId", new Object[0]).maxLength(paramEntityTypePermission.description, 255, "permission.description", new Object[0]).validObject(permissionValidationResult.entityType, "entityTypeId", new Object[] { paramUUID }).done();
    return permissionValidationResult;
  }
  
  public Errors validateSearchQuery(@Nullable UUID paramUUID, @Nullable String paramString1, @Nullable String paramString2, @Nullable List<SortField> paramList, int paramInt1, int paramInt2) {
    return this.entitySearchEngine.validate(paramUUID, paramString1, paramString2, paramList, paramInt1, paramInt2);
  }
  
  public EntityService.ValidationResult validateSearchRequest(EntityGrantSearchRequest paramEntityGrantSearchRequest, String paramString) {
    EntityService.ValidationResult validationResult = new EntityService.ValidationResult();
    validationResult
      .errors = (new Validator()).ensure((paramEntityGrantSearchRequest.search.entityId == null || paramEntityGrantSearchRequest.search.userId == null), paramString + "userId", "[notMissing]", new Object[0]).done();
    return validationResult;
  }
  
  public EntityService.TypeValidationResult validateTypeCreate(EntityType paramEntityType) {
    EntityService.TypeValidationResult typeValidationResult = new EntityService.TypeValidationResult();
    typeValidationResult






















      
      .errors = (new Validator()).ifTrue((paramEntityType.id != null), paramValidator -> paramValidator.notDuplicate(this.entityMapper.retrieveTypeById(paramEntityType.id), "entityTypeId", new Object[0])).ifTrue(paramEntityType.jwtConfiguration.enabled, paramValidator -> paramValidator.valid((paramEntityType.jwtConfiguration.timeToLiveInSeconds > 0), "entityType.jwtConfiguration.timeToLiveInSeconds", new Object[0])).ifTrue((paramEntityType.jwtConfiguration.accessTokenKeyId != null), paramValidator -> this.keyValidator.validateAccessTokenSigningKey(paramValidator, paramEntityType.jwtConfiguration.accessTokenKeyId, "entityType.jwtConfiguration.accessTokenKeyId")).forEach(paramEntityType.jwtConfiguration.accessTokenVerificationKeyIds, (paramValidator, paramUUID, paramInteger) -> this.keyValidator.validateAccessTokenVerificationKey(paramValidator, paramUUID, "entityType.jwtConfiguration.accessTokenVerificationKeyIds[" + paramInteger + "]", "[cannotVerify]entityType.jwtConfiguration.accessTokenVerificationKeyIds")).notBlank(paramEntityType.name, "entityType.name", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.notDuplicate(this.entityMapper.retrieveTypeDuplicate(null, paramEntityType.name), "entityType.name", new Object[] { paramEntityType.name })).ifTrue((paramEntityType.permissions != null && !paramEntityType.permissions.isEmpty()), paramValidator -> paramValidator.ensure(paramEntityType.permissions.stream().map(()).allMatch(new HashSet()::add), "entityType.permissions", "[duplicate]", new Object[0])).forEach(paramEntityType.permissions, (paramValidator, paramEntityTypePermission, paramInteger) -> paramValidator.notBlankWithCode(paramEntityTypePermission.name, "entityType.permissions[" + paramInteger + "].name", "[blank]entityType.permissions.name", new Object[0]).maxLengthWithCode(paramEntityTypePermission.description, 255, "entityType.permissions[" + paramInteger + "].description", "[tooLong]entityType.permissions.description", new Object[0])).done();
    return typeValidationResult;
  }
  
  public EntityService.TypeValidationResult validateTypeDelete(UUID paramUUID) {
    EntityService.TypeValidationResult typeValidationResult = new EntityService.TypeValidationResult();
    typeValidationResult.existing = retrieveTypeById(paramUUID);
    typeValidationResult





      
      .errors = (new Validator()).forEach(this.tenantReader.retrieveAllUsingEntityTypes(), (paramValidator, paramTenant, paramInteger) -> paramValidator.validate(()).validate(())).done();
    return typeValidationResult;
  }
  
  public EntityService.TypeValidationResult validateTypeUpdate(EntityType paramEntityType) {
    EntityService.TypeValidationResult typeValidationResult = new EntityService.TypeValidationResult();
    typeValidationResult.existing = (paramEntityType.id != null) ? retrieveTypeById(paramEntityType.id) : null;
    typeValidationResult





















      
      .errors = (new Validator()).notMissing(paramEntityType.id, "entityTypeId", new Object[0]).ifTrue(paramEntityType.jwtConfiguration.enabled, paramValidator -> paramValidator.valid((paramEntityType.jwtConfiguration.timeToLiveInSeconds > 0), "entityType.jwtConfiguration.timeToLiveInSeconds", new Object[0])).ifTrue((paramEntityType.jwtConfiguration.accessTokenKeyId != null), paramValidator -> {
          boolean bool = (paramTypeValidationResult.existing == null || !Objects.equals(paramEntityType.jwtConfiguration.accessTokenKeyId, paramTypeValidationResult.existing.jwtConfiguration.accessTokenKeyId)) ? true : false;
          if (bool)
            this.keyValidator.validateAccessTokenSigningKey(paramValidator, paramEntityType.jwtConfiguration.accessTokenKeyId, "entityType.jwtConfiguration.accessTokenKeyId"); 
        }).forEach(paramEntityType.jwtConfiguration.accessTokenVerificationKeyIds, (paramValidator, paramUUID, paramInteger) -> this.keyValidator.validateAccessTokenVerificationKey(paramValidator, paramUUID, "entityType.jwtConfiguration.accessTokenVerificationKeyIds[" + paramInteger + "]", "[cannotVerify]entityType.jwtConfiguration.accessTokenVerificationKeyIds")).notBlank(paramEntityType.name, "entityType.name", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.notDuplicate(this.entityMapper.retrieveTypeDuplicate(paramEntityType.id, paramEntityType.name), "entityType.name", new Object[] { paramEntityType.name })).done();
    return typeValidationResult;
  }
  
  private void createPermissions(EntityType paramEntityType) {
    for (EntityTypePermission entityTypePermission : paramEntityType.permissions) {
      if (entityTypePermission.id == null)
        entityTypePermission.id = UUID.randomUUID(); 
      entityTypePermission.entityTypeId = paramEntityType.id;
      entityTypePermission.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
      entityTypePermission.lastUpdateInstant = entityTypePermission.insertInstant;
      this.entityMapper.createPermission(entityTypePermission);
    } 
  }
  
  private SearchResults<Entity> fetchAndSort(UUID paramUUID, SearchEngineResult paramSearchEngineResult) {
    List<Entity> list = retrieveByIds(paramUUID, paramSearchEngineResult.ids);
    SearchResults<Entity> searchResults = new SearchResults<>(list, paramSearchEngineResult.totalNumberOfResults, paramSearchEngineResult.nextSearchToken);
    searchResults.totalEqualToActual = paramSearchEngineResult.totalEqualToActual;
    return searchResults;
  }
  
  private void writeVerificationKeys(EntityType paramEntityType) {
    this.entityMapper.deleteTypeVerificationKeys(paramEntityType.id);
    if (!paramEntityType.jwtConfiguration.accessTokenVerificationKeyIds.isEmpty())
      MapperTools.safeCreateUpdate(5000, paramEntityType.jwtConfiguration.accessTokenVerificationKeyIds, paramList -> this.entityMapper.createTypeVerificationKeys(paramEntityType.id, EntityTypeKeyType.AccessTokenVerification.name(), paramList, false)); 
  }
}
