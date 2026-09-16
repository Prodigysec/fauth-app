package io.fusionauth.api.service.jwt;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.error.Errors;
import com.inversoft.util.SecurityTools;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.RefreshTokenMapper;
import io.fusionauth.api.domain.mybatis._RefreshToken;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.oauth2.DPoPService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.EventHelper;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.util.ClaimTools;
import io.fusionauth.api.util.HashTools;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.JWTConfiguration;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Tenantable;
import io.fusionauth.domain.User;
import io.fusionauth.domain.event.JWTRefreshTokenRevokeEvent;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.jwt.domain.JWT;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.mybatis.guice.transactional.Transactional;

public class DefaultRefreshTokenService implements RefreshTokenService {
  private static final long CUTOFF_BUFFER_MILLIS = 300000L;
  
  private final ApplicationCache applicationCache;
  
  private final ApplicationReaderService applicationReader;
  
  private final RefreshTokenMapper backgroundRefreshTokenMapper;
  
  private final DPoPService dpopService;
  
  private final RefreshTokenMapper refreshTokenMapper;
  
  private final TenantCache tenantCache;
  
  private final UserReaderService userReader;
  
  @Inject
  public DefaultRefreshTokenService(ApplicationCache paramApplicationCache, ApplicationReaderService paramApplicationReaderService, @Named("background") RefreshTokenMapper paramRefreshTokenMapper1, DPoPService paramDPoPService, RefreshTokenMapper paramRefreshTokenMapper2, TenantCache paramTenantCache, UserReaderService paramUserReaderService) {
    this.applicationCache = paramApplicationCache;
    this.applicationReader = paramApplicationReaderService;
    this.backgroundRefreshTokenMapper = paramRefreshTokenMapper1;
    this.dpopService = paramDPoPService;
    this.refreshTokenMapper = paramRefreshTokenMapper2;
    this.tenantCache = paramTenantCache;
    this.userReader = paramUserReaderService;
  }
  
  @Transactional
  public void createBulk(Tenant paramTenant, List<RefreshToken> paramList) {
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    for (byte b = 0; b < paramList.size(); b += 1000) {
      int i = Math.min(b + 1000, paramList.size());
      List<RefreshTokenService.HashedRefreshToken> list = (List)paramList.subList(b, i).stream().map(paramRefreshToken -> {
            paramRefreshToken.id = UUID.randomUUID();
            paramRefreshToken.tenantId = null;
            if (paramRefreshToken.insertInstant == null)
              paramRefreshToken.insertInstant = paramZonedDateTime; 
            return new RefreshTokenService.HashedRefreshToken(paramRefreshToken, HashTools.sha256(paramRefreshToken.token));
          }).collect(Collectors.toList());
      this.refreshTokenMapper.createBulk(list);
    } 
  }
  
  public RefreshToken createRefreshToken(UUID paramUUID, Tenant paramTenant, User paramUser, Application paramApplication, Map<String, Object> paramMap, RefreshToken.MetaData paramMetaData) {
    return generateRefreshToken(paramUUID, paramUser, paramApplication, paramMap, paramMetaData, null);
  }
  
  public String createRefreshTokenFromAnotherRefreshToken(UUID paramUUID, Tenant paramTenant, User paramUser, Application paramApplication, RefreshToken paramRefreshToken) {
    if (paramRefreshToken.isExpired(paramTenant, paramApplication)) {
      String str = HashTools.sha256(paramRefreshToken.token);
      this.refreshTokenMapper.delete(str, paramRefreshToken.token);
      return null;
    } 
    JWTConfiguration jWTConfiguration1 = paramTenant.lookupJWTConfiguration(paramApplication);
    JWTConfiguration jWTConfiguration2 = paramTenant.lookupJWTConfiguration((Application)this.applicationCache.get(paramRefreshToken.applicationId));
    if (jWTConfiguration2.refreshTokenExpirationPolicy != jWTConfiguration1.refreshTokenExpirationPolicy)
      return null; 
    return (generateRefreshToken(paramUUID, paramUser, paramApplication, paramRefreshToken.data, paramRefreshToken.metaData, paramRefreshToken.startInstant)).token;
  }
  
  public RefreshToken createRefreshTokenWithStartInstant(UUID paramUUID, Tenant paramTenant, User paramUser, Application paramApplication, Map<String, Object> paramMap, RefreshToken.MetaData paramMetaData, ZonedDateTime paramZonedDateTime) {
    return generateRefreshToken(paramUUID, paramUser, paramApplication, paramMap, paramMetaData, paramZonedDateTime);
  }
  
  public RefreshToken createSSOSessionToken(User paramUser, RefreshToken.MetaData paramMetaData, String paramString) {
    if (paramMetaData == null)
      paramMetaData = new RefreshToken.MetaData(); 
    paramMetaData.device.lastAccessedAddress = paramString;
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.id = UUID.randomUUID();
    refreshToken.token = SecurityTools.secureRandom(40);
    refreshToken.userId = paramUser.id;
    refreshToken.insertInstant = zonedDateTime;
    refreshToken.metaData = paramMetaData;
    refreshToken.metaData.device.lastAccessedInstant = zonedDateTime;
    refreshToken.startInstant = zonedDateTime;
    refreshToken.tenantId = paramUser.tenantId;
    this.refreshTokenMapper.create(new RefreshTokenService.HashedRefreshToken(refreshToken, HashTools.sha256(refreshToken.token)));
    return refreshToken;
  }
  
  public void refreshSSOSessionToken(RefreshToken paramRefreshToken, String paramString) {
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    paramRefreshToken.metaData.device.lastAccessedInstant = zonedDateTime;
    paramRefreshToken.metaData.device.lastAccessedAddress = paramString;
    paramRefreshToken.token = SecurityTools.secureRandom(40);
    paramRefreshToken.startInstant = zonedDateTime;
    this.refreshTokenMapper.update(new RefreshTokenService.HashedRefreshToken(paramRefreshToken, HashTools.sha256(paramRefreshToken.token)), false);
  }
  
  public RefreshToken retrieveForUpdateUsingSeed(String paramString) {
    String str = HashTools.sha256(paramString);
    _RefreshToken _RefreshToken = this.refreshTokenMapper.retrieveForUpdateUsingSeed(str, paramString);
    if (_RefreshToken != null)
      _RefreshToken.generateSyntheticFields(str, paramString); 
    return _RefreshToken;
  }
  
  public RefreshToken retrieveRefreshToken(String paramString) {
    String str = HashTools.sha256(paramString);
    return this.refreshTokenMapper.retrieve(str, paramString);
  }
  
  public RefreshToken retrieveRefreshTokenById(UUID paramUUID) {
    return this.refreshTokenMapper.retrieveById(paramUUID);
  }
  
  public List<RefreshToken> retrieveRefreshTokenByUserIdAndApplicationId(UUID paramUUID1, UUID paramUUID2) {
    return this.refreshTokenMapper.retrieveByUserIdAndApplicationId(paramUUID1, paramUUID2);
  }
  
  public RefreshTokenService.RefreshTokenResult retrieveRefreshTokenForUser(Tenant paramTenant, User paramUser, String paramString) {
    RefreshTokenService.RefreshTokenResult refreshTokenResult = new RefreshTokenService.RefreshTokenResult();
    if (paramTenant == null || paramUser == null || paramString == null)
      return refreshTokenResult; 
    String str = HashTools.sha256(paramString);
    refreshTokenResult.refreshToken = this.refreshTokenMapper.retrieve(str, paramString);
    if (refreshTokenResult.refreshToken == null || !refreshTokenResult.refreshToken.userId.equals(paramUser.id))
      return refreshTokenResult; 
    refreshTokenResult.application = this.applicationCache.get(paramTenant.id, refreshTokenResult.refreshToken.applicationId);
    if (refreshTokenResult.refreshToken.isExpired(paramTenant, refreshTokenResult.application))
      refreshTokenResult.refreshToken = null; 
    return refreshTokenResult;
  }
  
  public RefreshToken retrieveRefreshTokenUsingSeed(String paramString) {
    String str = HashTools.sha256(paramString);
    _RefreshToken _RefreshToken = this.refreshTokenMapper.retrieveUsingSeed(str, paramString);
    if (_RefreshToken != null)
      _RefreshToken.generateSyntheticFields(str, paramString); 
    return _RefreshToken;
  }
  
  public List<RefreshToken> retrieveRefreshTokensByApplicationId(UUID paramUUID) {
    return this.refreshTokenMapper.retrieveByApplicationId(paramUUID);
  }
  
  public List<RefreshToken> retrieveRefreshTokensByUserId(UUID paramUUID) {
    return this.refreshTokenMapper.retrieveByUserId(paramUUID, 1000);
  }
  
  public int revokeExpiredRefreshTokens() {
    int i = 0;
    long l1 = System.currentTimeMillis();
    Map map = (Map)this.tenantCache.getAll().stream().collect(Collectors.toMap(paramTenant -> paramTenant.id, paramTenant -> paramTenant));
    for (Application application : this.applicationCache.getAll()) {
      if (application.jwtConfiguration == null || !application.jwtConfiguration.enabled) {
        JWTConfiguration jWTConfiguration = (application.tenantId != null) ? ((Tenant)map.get(application.tenantId)).jwtConfiguration : application.jwtConfiguration;
        long l3 = bufferCutoffMillis(l1, jWTConfiguration.refreshTokenTimeToLiveInMinutes, TimeUnit.MINUTES);
        i += this.backgroundRefreshTokenMapper.deleteOlderThanForApplicationId(application.id, l3);
        continue;
      } 
      long l = bufferCutoffMillis(l1, application.jwtConfiguration.refreshTokenTimeToLiveInMinutes, TimeUnit.MINUTES);
      i += this.backgroundRefreshTokenMapper.deleteOlderThanForApplicationId(application.id, l);
    } 
    long l2 = 0L;
    for (Tenant tenant : map.values()) {
      long l = bufferCutoffMillis(l1, tenant.httpSessionMaxInactiveInterval, TimeUnit.SECONDS);
      i += this.backgroundRefreshTokenMapper.deleteSingleSignOnTokensOlderThan(tenant.id, l);
      l2 = Math.max(l2, l);
    } 
    i += this.backgroundRefreshTokenMapper.legacyDeleteSingleSignOnTokensOlderThan(l2);
    return i;
  }
  
  public void revokeFusionAuthSessionToken(UUID paramUUID, EventInfo paramEventInfo) {
    RefreshToken refreshToken = retrieveRefreshTokenById(paramUUID);
    if (refreshToken != null) {
      Application application = (Application)this.applicationCache.get(refreshToken.applicationId);
      Tenant tenant = (Tenant)this.tenantCache.get(application.tenantId);
      User user = this.userReader.retrieveById(tenant.id, refreshToken.userId);
      revokeRefreshToken(tenant, application, refreshToken, user, paramEventInfo);
    } 
  }
  
  @Transactional
  public boolean revokeRefreshToken(Tenant paramTenant, Application paramApplication, RefreshToken paramRefreshToken, User paramUser, EventInfo paramEventInfo) {
    String str = HashTools.sha256(paramRefreshToken.token);
    int i = this.refreshTokenMapper.delete(str, paramRefreshToken.token);
    if (paramApplication != null) {
      int j = (paramApplication != null && paramApplication.jwtConfiguration != null && paramApplication.jwtConfiguration.enabled) ? paramApplication.jwtConfiguration.timeToLiveInSeconds : paramTenant.jwtConfiguration.timeToLiveInSeconds;
      JWTRefreshTokenRevokeEvent jWTRefreshTokenRevokeEvent = new JWTRefreshTokenRevokeEvent(paramEventInfo, paramUser, paramRefreshToken.applicationId, j);
      jWTRefreshTokenRevokeEvent.refreshToken = (new RefreshToken(paramRefreshToken)).secure();
      EventHelper.send(paramTenant, paramApplication, jWTRefreshTokenRevokeEvent);
    } 
    return (i > 0);
  }
  
  @Transactional
  public int revokeRefreshTokensByApplicationId(Tenant paramTenant, Application paramApplication, EventInfo paramEventInfo) {
    Objects.requireNonNull(paramTenant);
    int i = this.refreshTokenMapper.deleteByApplicationId(paramApplication.id);
    int j = (paramTenant.lookupJWTConfiguration(paramApplication)).timeToLiveInSeconds;
    JWTRefreshTokenRevokeEvent jWTRefreshTokenRevokeEvent = new JWTRefreshTokenRevokeEvent(paramEventInfo, null, paramApplication.id, j);
    EventHelper.send(paramTenant, paramApplication, jWTRefreshTokenRevokeEvent);
    return i;
  }
  
  @Transactional
  public void revokeRefreshTokensByUser(Tenant paramTenant, User paramUser, EventInfo paramEventInfo) {
    Objects.requireNonNull(paramTenant);
    Set<UUID> set = this.refreshTokenMapper.retrieveApplicationIdsByUserId(paramUser.id);
    int i = this.refreshTokenMapper.deleteByUserId(paramUser.id);
    if (i == 0)
      return; 
    Map map = (Map)this.applicationCache.getAllByTenantId(paramTenant.id).stream().collect(Collectors.toMap(paramApplication -> paramApplication.id, paramApplication -> paramApplication));
    Map<UUID, Integer> map1 = (Map)set.stream().collect(Collectors.toMap(paramUUID -> paramUUID, paramUUID -> Integer.valueOf((paramTenant.lookupJWTConfiguration((Application)paramMap.get(paramUUID))).timeToLiveInSeconds)));
    JWTRefreshTokenRevokeEvent jWTRefreshTokenRevokeEvent = new JWTRefreshTokenRevokeEvent(paramEventInfo, paramUser, map1);
    EventHelper.send(paramTenant, null, jWTRefreshTokenRevokeEvent);
  }
  
  @Transactional
  public int revokeRefreshTokensByUserAndApplicationId(Tenant paramTenant, User paramUser, Application paramApplication, EventInfo paramEventInfo) {
    int i = this.refreshTokenMapper.deleteByUserAndApplicationId(paramUser.id, paramApplication.id);
    int j = (paramTenant.lookupJWTConfiguration(paramApplication)).timeToLiveInSeconds;
    JWTRefreshTokenRevokeEvent jWTRefreshTokenRevokeEvent = new JWTRefreshTokenRevokeEvent(paramEventInfo, paramUser, paramApplication.id, j);
    EventHelper.send(paramTenant, paramApplication, jWTRefreshTokenRevokeEvent);
    return i;
  }
  
  public void revokeSSOSessionToken(String paramString, EventInfo paramEventInfo) {
    String str = HashTools.sha256(paramString);
    RefreshToken refreshToken = this.refreshTokenMapper.retrieve(str, paramString);
    if (refreshToken != null) {
      User user = this.userReader.retrieveById(null, refreshToken.userId);
      Tenant tenant = (Tenant)this.tenantCache.get(user.tenantId);
      revokeRefreshToken(tenant, null, refreshToken, user, paramEventInfo);
    } 
  }
  
  public Errors validateBulkCreate(Tenant paramTenant, List<RefreshToken> paramList, boolean paramBoolean) {
    HashMap<Object, Object> hashMap1 = new HashMap<>();
    HashMap<Object, Object> hashMap2 = new HashMap<>();
    Function function1 = paramUUID -> (Application)paramMap.computeIfAbsent(paramUUID, ());
    Function function2 = paramUUID -> (User)paramMap.computeIfAbsent(paramUUID, ());
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    Validator validator = new Validator();
    for (byte b = 0; b < paramList.size(); b++) {
      byte b1 = b;
      RefreshToken refreshToken = paramList.get(b);
      if (paramBoolean)
        validator.notMissingWithCode(refreshToken.token, "refreshTokens[" + b1 + "].token", "[missing]refreshTokens.token", new Object[0])
          .ifLastCheckHadNoError(() -> paramValidator.ifLastCheckHadNoError(()))



          
          .notMissingWithCode(refreshToken.applicationId, "refreshTokens[" + b1 + "].applicationId", "[missing]refreshTokens.applicationId", new Object[0])
          .ifLastCheckHadNoError(() -> paramValidator.validObjectWithCode(paramFunction.apply(paramRefreshToken.applicationId), "refreshTokens[" + paramInt + "].applicationId", "[invalid]refreshTokens.applicationId", new Object[] { paramRefreshToken.applicationId })).notMissingWithCode(refreshToken.userId, "refreshTokens[" + b1 + "].userId", "[missing]refreshTokens.userId", new Object[0])
          .ifLastCheckHadNoError(() -> paramValidator.validObjectWithCode(paramFunction.apply(paramRefreshToken.userId), "refreshTokens[" + paramInt + "].userId", "[invalid]refreshTokens.userId", new Object[] { paramRefreshToken.userId })).ifNoErrors(() -> paramValidator.ifLastCheckHadNoError(())); 
      validator.notMissingWithCode(refreshToken.startInstant, "refreshTokens[" + b1 + "].startInstant", "[missing]refreshTokens.startInstant", new Object[0])
        .ifLastCheckHadNoError(() -> paramValidator.validWithCode(paramRefreshToken.startInstant.isBefore(paramZonedDateTime), "refreshTokens[" + paramInt + "].startInstant", "[invalid]refreshTokens.startInstant", new Object[] { paramRefreshToken.startInstant }));
    } 
    return validator.done();
  }
  
  public JWTService.ValidationResult validateDeleteRefreshToken(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString, UUID paramUUID3, JWT paramJWT, HTTPRequest paramHTTPRequest) {
    JWTService.ValidationResult validationResult = validateRefreshToken(paramTenant, paramUUID1, paramUUID2, paramString, paramUUID3, paramHTTPRequest);
    validationResult
      
      .errors = (new Validator()).ifTrue((paramUUID2 != null), paramValidator -> paramValidator.validObject(paramValidationResult.application, "applicationId", new Object[] { paramUUID })).ifTrue((paramUUID1 != null), paramValidator -> paramValidator.validObject(paramValidationResult.user, "userId", new Object[] { paramUUID })).done();
    if (paramJWT != null) {
      validationResult.jwtApplicationId = ClaimTools.resolveApplicationId(paramJWT);
      if (validationResult.jwtApplicationId != null) {
        Objects.requireNonNull(this.applicationReader);
        validationResult.jwtApplication = this.applicationCache.get((paramTenant == null) ? null : paramTenant.id, validationResult.jwtApplicationId, this.applicationReader::retrieveById);
      } 
    } 
    return validationResult;
  }
  
  public JWTService.ValidationResult validateLogout(Tenant paramTenant, String paramString) {
    JWTService.ValidationResult validationResult = new JWTService.ValidationResult();
    UUID uUID = (paramTenant != null) ? paramTenant.id : null;
    if (paramString != null) {
      validationResult.refreshToken = retrieveRefreshToken(paramString);
      if (validationResult.refreshToken != null) {
        validationResult.application = this.applicationCache.get(uUID, validationResult.refreshToken.applicationId);
        validationResult.user = this.userReader.retrieveById(uUID, validationResult.refreshToken.userId);
        validationResult.tenant = this.tenantCache.resolve(paramTenant, new Tenantable[] { validationResult.application, validationResult.user });
      } 
    } 
    return validationResult;
  }
  
  public JWTService.ValidationResult validateRefreshToken(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString, UUID paramUUID3, HTTPRequest paramHTTPRequest) {
    JWTService.ValidationResult validationResult = new JWTService.ValidationResult();
    if (paramUUID2 != null)
      validationResult.application = this.applicationCache.get((paramTenant == null) ? null : paramTenant.id, paramUUID2); 
    validationResult.tenant = this.tenantCache.resolve(paramTenant, new Tenantable[] { validationResult.application });
    if (paramUUID1 != null)
      validationResult.user = this.userReader.retrieveById((validationResult.tenant == null) ? null : validationResult.tenant.id, paramUUID1); 
    if (paramUUID3 != null) {
      validationResult.refreshToken = this.refreshTokenMapper.retrieveById(paramUUID3);
    } else if (paramString != null) {
      validationResult.refreshToken = retrieveRefreshToken(paramString);
    } 
    if (paramUUID1 != null && validationResult.refreshToken != null && 
      !validationResult.refreshToken.userId.equals(paramUUID1))
      validationResult.refreshToken = null; 
    if (validationResult.refreshToken != null) {
      DPoPService.DPoPResult dPoPResult = this.dpopService.parseDPoPRefreshToken(paramHTTPRequest, validationResult.refreshToken);
      if (dPoPResult.error() != null) {
        validationResult.errors = new Errors();
        validationResult.errors.addGeneralError("[" + (dPoPResult.error()).error.toString() + "]", (dPoPResult.error()).description, new Object[0]);
        return validationResult;
      } 
      validationResult.dPoPThumbprint = dPoPResult.dPoPThumbprint();
      if (paramUUID1 == null)
        validationResult.user = this.userReader.retrieveById((validationResult.tenant == null) ? null : validationResult.tenant.id, validationResult.refreshToken.userId); 
      validationResult.refreshTokenApplication = this.applicationCache.get((validationResult.tenant == null) ? null : validationResult.tenant.id, validationResult.refreshToken.applicationId);
      if (validationResult.tenant == null)
        validationResult.tenant = this.tenantCache.resolve((Tenant)null, new Tenantable[] { validationResult.refreshTokenApplication, validationResult.user }); 
      if (validationResult.refreshToken.isExpired(validationResult.tenant, validationResult.refreshTokenApplication))
        validationResult.refreshToken = null; 
    } 
    return validationResult;
  }
  
  private long bufferCutoffMillis(long paramLong1, long paramLong2, TimeUnit paramTimeUnit) {
    return paramLong1 - 300000L - paramTimeUnit.toMillis(paramLong2);
  }
  
  private RefreshToken generateRefreshToken(UUID paramUUID, User paramUser, Application paramApplication, Map<String, Object> paramMap, RefreshToken.MetaData paramMetaData, ZonedDateTime paramZonedDateTime) {
    if (paramMetaData == null)
      paramMetaData = new RefreshToken.MetaData(); 
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    if (paramZonedDateTime == null)
      paramZonedDateTime = zonedDateTime; 
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.id = (paramUUID != null) ? paramUUID : UUID.randomUUID();
    if (paramMap != null)
      refreshToken.data.putAll(paramMap); 
    refreshToken.data.put("v", Integer.valueOf(2));
    refreshToken.token = SecurityTools.secureRandom(48);
    refreshToken.userId = paramUser.id;
    refreshToken.insertInstant = zonedDateTime;
    refreshToken.applicationId = paramApplication.id;
    refreshToken.metaData = paramMetaData;
    refreshToken.metaData.device.lastAccessedInstant = zonedDateTime;
    refreshToken.startInstant = paramZonedDateTime;
    refreshToken.tenantId = null;
    this.refreshTokenMapper.create(new RefreshTokenService.HashedRefreshToken(refreshToken, HashTools.sha256(refreshToken.token)));
    return refreshToken;
  }
}
