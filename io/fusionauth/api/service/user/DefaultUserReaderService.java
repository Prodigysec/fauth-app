package io.fusionauth.api.service.user;

import com.google.inject.Inject;
import com.inversoft.mybatis.MyBatisTools;
import com.inversoft.search.SearchEngineUnavailableException;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.GroupMapper;
import io.fusionauth.api.domain.SearchEngineResult;
import io.fusionauth.api.domain.SearchEngineType;
import io.fusionauth.api.domain.UserIdentityStatus;
import io.fusionauth.api.domain.UserMapper;
import io.fusionauth.api.domain.mybatis._UserIdentity;
import io.fusionauth.api.domain.mybatis._UserRegistration;
import io.fusionauth.api.service.search.ElasticsearchUserSearchEngine;
import io.fusionauth.api.service.search.UserSearchEngine;
import io.fusionauth.api.util.MapperTools;
import io.fusionauth.domain.GroupMember;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.IdentityVerifiedReason;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.SortField;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.mybatis.guice.transactional.Transactional;

public class DefaultUserReaderService implements UserReaderService {
  public static final List<IdentityType> DefaultIdentityTypes = List.of(IdentityType.email, IdentityType.username);
  
  private final FusionAuthConfiguration configuration;
  
  private final GroupMapper groupMapper;
  
  private final UserSearchEngine searchEngine;
  
  private final UserMapper userMapper;
  
  @Inject
  public DefaultUserReaderService(FusionAuthConfiguration paramFusionAuthConfiguration, GroupMapper paramGroupMapper, UserSearchEngine paramUserSearchEngine, UserMapper paramUserMapper) {
    this.configuration = paramFusionAuthConfiguration;
    this.groupMapper = paramGroupMapper;
    this.searchEngine = paramUserSearchEngine;
    this.userMapper = paramUserMapper;
  }
  
  public static List<User> expand(List<User> paramList, Set<UserReaderService.UserExpansion> paramSet, FusionAuthConfiguration paramFusionAuthConfiguration, UserMapper paramUserMapper, GroupMapper paramGroupMapper) {
    if (paramList == null || paramList.isEmpty())
      return paramList; 
    List<?> list = paramList.stream().map(paramUser -> paramUser.id).toList();
    List<User> list1 = (List)paramList.stream().map(User::new).collect(Collectors.toList());
    list1.forEach(DefaultUserReaderService::normalizeFromIdentities);
    if (paramSet == null || paramSet.isEmpty())
      return list1; 
    if (paramSet.contains(UserReaderService.UserExpansion.memberships)) {
      Objects.requireNonNull(paramGroupMapper);
      List<?> list2 = MapperTools.safeRetrieve(paramFusionAuthConfiguration.internalUserReaderExpansionBatchSize(), list, paramGroupMapper::retrieveMembersByUserIds);
      Map map = (Map)list2.stream().collect(Collectors.groupingBy(paramGroupMember -> paramGroupMember.userId));
      list1.forEach(paramUser -> paramUser.getMemberships().addAll(((List)paramMap.getOrDefault(paramUser.id, List.of())).stream().peek(()).toList()));
    } 
    if (paramSet.contains(UserReaderService.UserExpansion.registrations)) {
      Objects.requireNonNull(paramUserMapper);
      List<?> list2 = MapperTools.safeRetrieve(paramFusionAuthConfiguration.internalUserReaderExpansionBatchSize(), list, paramUserMapper::retrieveRegistrationsByUserIds);
      Map map = (Map)list2.stream().collect(Collectors.groupingBy(param_UserRegistration -> param_UserRegistration.userId));
      list1.forEach(paramUser -> paramUser.getRegistrations().addAll((Collection<? extends UserRegistration>)paramMap.getOrDefault(paramUser.id, List.of())));
    } 
    return list1;
  }
  
  public static User expand(User paramUser, Set<UserReaderService.UserExpansion> paramSet, FusionAuthConfiguration paramFusionAuthConfiguration, UserMapper paramUserMapper, GroupMapper paramGroupMapper) {
    if (paramUser == null)
      return null; 
    return (User)expand(List.of(paramUser), paramSet, paramFusionAuthConfiguration, paramUserMapper, paramGroupMapper).getFirst();
  }
  
  public static void fixLegacyIdentity(User paramUser) {
    if (paramUser == null || userIsMigrated(paramUser))
      return; 
    _UserIdentity _UserIdentity = (_UserIdentity)paramUser.identities.getFirst();
    paramUser.identities.clear();
    paramUser.verifiedInstant = _UserIdentity.verifiedInstant;
    if (_UserIdentity.email != null)
      paramUser.identities.add((new UserIdentity()).with(paramUserIdentity -> paramUserIdentity.insertInstant = paramUser.insertInstant)
          .with(paramUserIdentity -> paramUserIdentity.lastUpdateInstant = paramUser.lastUpdateInstant)
          .with(paramUserIdentity -> paramUserIdentity.type = IdentityType.email)
          .with(paramUserIdentity -> paramUserIdentity.value = param_UserIdentity.email)
          .with(paramUserIdentity -> paramUserIdentity.primary = true)
          .with(paramUserIdentity -> paramUserIdentity.verified = param_UserIdentity.verified)
          .with(paramUserIdentity -> paramUserIdentity.verifiedInstant = param_UserIdentity.verifiedInstant)); 
    if (_UserIdentity.username != null)
      paramUser.identities.add(buildModernUsernameIdentity(paramUser, _UserIdentity)); 
  }
  
  public static void normalizeFromIdentities(User paramUser) {
    paramUser.sort();
    if (paramUser.identities.isEmpty()) {
      paramUser.verified = true;
      paramUser.verifiedInstant = paramUser.insertInstant;
      paramUser.email = null;
      paramUser.username = null;
      paramUser.uniqueUsername = null;
    } else {
      UserIdentity userIdentity1 = paramUser.resolvePrimaryIdentity(IdentityType.email);
      UserIdentity userIdentity2 = paramUser.resolvePrimaryIdentity(IdentityType.phoneNumber);
      UserIdentity userIdentity3 = paramUser.resolvePrimaryIdentity(IdentityType.username);
      paramUser.email = (userIdentity1 != null) ? userIdentity1.value : null;
      paramUser.phoneNumber = (userIdentity2 != null) ? userIdentity2.value : null;
      if (userIdentity3 != null) {
        paramUser.username = userIdentity3.displayValue;
        paramUser.uniqueUsername = userIdentity3.value;
        paramUser.usernameStatus = userIdentity3.moderationStatus;
      } else {
        paramUser.username = null;
        paramUser.uniqueUsername = null;
      } 
      Optional optional = Stream.<UserIdentity>of(new UserIdentity[] { userIdentity1, userIdentity3 }).filter(Objects::nonNull).findFirst();
      paramUser.verified = ((Boolean)optional.map(paramUserIdentity -> Boolean.valueOf(!paramUserIdentity.verificationRequired())).orElse(Boolean.valueOf(false))).booleanValue();
    } 
  }
  
  private static UserIdentity buildModernUsernameIdentity(User paramUser, _UserIdentity param_UserIdentity) {
    return (new UserIdentity()).with(paramUserIdentity -> paramUserIdentity.insertInstant = paramUser.insertInstant)
      .with(paramUserIdentity -> paramUserIdentity.lastUpdateInstant = paramUser.lastUpdateInstant)
      .with(paramUserIdentity -> paramUserIdentity.type = IdentityType.username)
      .with(paramUserIdentity -> paramUserIdentity.displayValue = param_UserIdentity.username)
      .with(paramUserIdentity -> paramUserIdentity.moderationStatus = param_UserIdentity.moderationStatus)
      .with(paramUserIdentity -> paramUserIdentity.primary = true)
      .with(paramUserIdentity -> paramUserIdentity.value = param_UserIdentity.uniqueUsername)
      .with(paramUserIdentity -> paramUserIdentity.verified = false)
      .with(paramUserIdentity -> paramUserIdentity.verifiedReason = IdentityVerifiedReason.Unverifiable);
  }
  
  private static boolean userIsMigrated(User paramUser) {
    boolean bool = paramUser.identities.isEmpty();
    boolean bool1 = (paramUser.identities.size() != 1 || ((UserIdentity)paramUser.identities.getFirst()).type != null) ? true : false;
    return (bool || bool1);
  }
  
  public User _checkMigration(Supplier<User> paramSupplier) {
    User user = paramSupplier.get();
    if (user == null || userIsMigrated(user))
      return user; 
    return _migrateUser(user.id, paramSupplier);
  }
  
  @Transactional
  public User _migrateUser(UUID paramUUID, Supplier<User> paramSupplier) {
    this.userMapper.lockUserForMigration(paramUUID);
    User user = paramSupplier.get();
    if (user == null || userIsMigrated(user))
      return user; 
    _UserIdentity _UserIdentity = (_UserIdentity)user.identities.getFirst();
    this.userMapper.migrateUpdateUserFields(user, _UserIdentity.verifiedInstant);
    boolean bool = false;
    if (_UserIdentity.email != null) {
      this.userMapper.migrateIdentityToEmail(user, _UserIdentity.verified, _UserIdentity.verifiedInstant);
      bool = true;
    } 
    if (_UserIdentity.username != null)
      if (bool) {
        UserIdentity userIdentity = buildModernUsernameIdentity(user, _UserIdentity).with(paramUserIdentity -> paramUserIdentity.tenantId = paramUser.tenantId).with(paramUserIdentity -> paramUserIdentity.userId = paramUser.id);
        this.userMapper.createIdentityBulk(List.of(userIdentity), UserIdentityStatus.Active);
      } else {
        this.userMapper.migrateIdentityToUsername(user);
      }  
    return paramSupplier.get();
  }
  
  public List<User> retrieveAllBySearchQueryString(UUID paramUUID, String paramString, List<SortField> paramList, int paramInt, Set<UserReaderService.UserExpansion> paramSet) {
    ArrayList<User> arrayList = new ArrayList();
    int i = 0;
    int j = Math.min(paramInt, 500);
    while (arrayList.size() < paramInt) {
      int k = Math.min(j, paramInt - arrayList.size());
      SearchResults<User> searchResults = searchByQueryString(paramUUID, paramString, k, i, paramList, false, paramSet, null, null);
      if (searchResults.results.isEmpty())
        break; 
      arrayList.addAll(searchResults.results);
      i += searchResults.results.size();
    } 
    return arrayList;
  }
  
  public long retrieveBreachedUsersRequiringActionCountByTenantId(UUID paramUUID) {
    if (this.configuration.searchEngineType() == SearchEngineType.elasticsearch)
      return (this.searchEngine.searchByQueryString(paramUUID, ElasticsearchUserSearchEngine.BREACHED_REQUIRING_ACTION_QUERY_STRING, 0, 1, null, true, null, null)).totalNumberOfResults; 
    return this.userMapper.retrieveBreachedUsersPendingActionCount(paramUUID);
  }
  
  public User retrieveById(UUID paramUUID1, UUID paramUUID2) {
    if (paramUUID2 == null)
      return null; 
    User user = _checkMigration(() -> this.userMapper.retrieveById(paramUUID1, paramUUID2));
    return expand(user, UserReaderService.UserExpansion.all(), this.configuration, this.userMapper, this.groupMapper);
  }
  
  public List<User> retrieveByIds(UUID paramUUID, List<UUID> paramList, Set<UserReaderService.UserExpansion> paramSet) {
    if (paramList == null || paramList.isEmpty())
      return Collections.emptyList(); 
    List<?> list = MapperTools.safeRetrieve(10000, paramList, paramList -> this.userMapper.retrieveByIds(paramUUID, paramList));
    list.forEach(DefaultUserReaderService::fixLegacyIdentity);
    list = MyBatisTools.order(list, paramList, paramUser -> paramUser.id);
    return expand((List)list, paramSet);
  }
  
  public User retrieveByLoginId(UUID paramUUID, String paramString, List<IdentityType> paramList, Set<UserReaderService.UserExpansion> paramSet) {
    if (paramList == null || paramList.isEmpty())
      paramList = DefaultIdentityTypes; 
    LinkedHashMap<Object, Object> linkedHashMap = new LinkedHashMap<>();
    for (IdentityType identityType : paramList) {
      String str = IdentityHelper.canonicalizeValue(paramString, identityType);
      if (str != null)
        linkedHashMap.put(identityType, str); 
    } 
    if (linkedHashMap.isEmpty())
      return null; 
    User user = _checkMigration(() -> this.userMapper.retrieveUserByIdentityType(paramUUID, paramSequencedMap));
    return expand(user, paramSet, this.configuration, this.userMapper, this.groupMapper);
  }
  
  public User retrieveByLoginId(UUID paramUUID, String paramString, List<IdentityType> paramList) {
    return retrieveByLoginId(paramUUID, paramString, paramList, UserReaderService.UserExpansion.all());
  }
  
  public List<User> retrieveByParentEmail(UUID paramUUID, String paramString) {
    List<User> list = this.userMapper.retrieveByParentEmail(paramUUID, paramString);
    list.forEach(DefaultUserReaderService::fixLegacyIdentity);
    return expand(list, UserReaderService.UserExpansion.all());
  }
  
  public User retrieveExisting(Tenant paramTenant, UUID paramUUID, String paramString, IdentityType paramIdentityType) {
    User user = _checkMigration(() -> this.userMapper.retrieveExisting(paramTenant.id, paramUUID, paramString, paramIdentityType));
    return expand(user, UserReaderService.UserExpansion.all(), this.configuration, this.userMapper, this.groupMapper);
  }
  
  public User retrieveIdentityLessUserById(UUID paramUUID1, UUID paramUUID2) {
    if (paramUUID2 == null)
      return null; 
    User user = _checkMigration(() -> this.userMapper.retrieveUserByLinkUserId(paramUUID1, paramUUID2));
    return expand(user, UserReaderService.UserExpansion.all(), this.configuration, this.userMapper, this.groupMapper);
  }
  
  public UserRegistration retrieveRegistration(UUID paramUUID1, UUID paramUUID2, UUID paramUUID3) {
    return this.userMapper.retrieveRegistration(paramUUID1, paramUUID2, paramUUID3);
  }
  
  public UserRegistration retrieveRegistrationById(UUID paramUUID1, UUID paramUUID2) {
    return this.userMapper.retrieveRegistrationById(paramUUID1, paramUUID2);
  }
  
  public List<User> retrieveUnverifiedChildrenForReaping(UUID paramUUID, ZonedDateTime paramZonedDateTime) {
    List<User> list = this.userMapper.retrieveUnverifiedChildrenForReaping(paramUUID, paramZonedDateTime);
    list.forEach(DefaultUserReaderService::fixLegacyIdentity);
    return expand(list, null);
  }
  
  public List<User> retrieveUnverifiedUsersForReaping(UUID paramUUID, ZonedDateTime paramZonedDateTime1, ZonedDateTime paramZonedDateTime2) {
    List<User> list = this.userMapper.retrieveUnverifiedUsersForReaping(paramUUID, paramZonedDateTime1, paramZonedDateTime2);
    list.forEach(DefaultUserReaderService::fixLegacyIdentity);
    return expand(list, null);
  }
  
  public SearchResults<User> searchByQuery(UUID paramUUID, String paramString1, int paramInt1, List<SortField> paramList, int paramInt2, boolean paramBoolean, Set<UserReaderService.UserExpansion> paramSet, List<String> paramList1, String paramString2) throws SearchEngineUnavailableException {
    SearchEngineResult searchEngineResult = this.searchEngine.searchByQuery(paramUUID, paramString1, paramInt1, paramInt2, paramList, paramBoolean, paramList1, paramString2);
    return fetchAndSortUsers(null, searchEngineResult, paramSet);
  }
  
  public SearchResults<User> searchByQueryString(UUID paramUUID, String paramString1, int paramInt1, int paramInt2, List<SortField> paramList, boolean paramBoolean, Set<UserReaderService.UserExpansion> paramSet, List<String> paramList1, String paramString2) throws SearchEngineUnavailableException {
    if (paramString1.equals(ElasticsearchUserSearchEngine.BREACHED_REQUIRING_ACTION_QUERY_STRING) && this.configuration.searchEngineType() == SearchEngineType.database) {
      List<User> list = (paramInt1 > 0) ? this.userMapper.retrieveBreachedUsersPendingAction(paramUUID, paramInt1, paramInt2) : List.of();
      if (!list.isEmpty()) {
        list.forEach(DefaultUserReaderService::fixLegacyIdentity);
        list = expand(list, paramSet);
      } 
      return new SearchResults<>(list, this.userMapper.retrieveBreachedUsersPendingActionCount(paramUUID));
    } 
    SearchEngineResult searchEngineResult = this.searchEngine.searchByQueryString(paramUUID, paramString1, paramInt2, paramInt1, paramList, paramBoolean, paramList1, paramString2);
    return fetchAndSortUsers(paramUUID, searchEngineResult, paramSet);
  }
  
  private List<User> expand(List<User> paramList, Set<UserReaderService.UserExpansion> paramSet) {
    return expand(paramList, paramSet, this.configuration, this.userMapper, this.groupMapper);
  }
  
  private SearchResults<User> fetchAndSortUsers(UUID paramUUID, SearchEngineResult paramSearchEngineResult, Set<UserReaderService.UserExpansion> paramSet) {
    List<User> list = retrieveByIds(paramUUID, paramSearchEngineResult.ids, paramSet);
    SearchResults<User> searchResults = new SearchResults<>(list, paramSearchEngineResult.totalNumberOfResults, paramSearchEngineResult.nextSearchToken);
    searchResults.totalEqualToActual = paramSearchEngineResult.totalEqualToActual;
    return searchResults;
  }
}
