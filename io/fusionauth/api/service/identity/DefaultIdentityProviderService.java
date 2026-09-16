package io.fusionauth.api.service.identity;

import com.google.inject.Inject;
import com.inversoft.cache.CacheNotifier;
import com.inversoft.error.Errors;
import com.inversoft.util.StringTools;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ApplicationMapper;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.IdentityProviderKeyType;
import io.fusionauth.api.domain.IdentityProviderLinkMapper;
import io.fusionauth.api.domain.IdentityProviderMapper;
import io.fusionauth.api.domain.LambdaMapper;
import io.fusionauth.api.domain.TenantMapper;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.system.KeyHelper;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierService;
import io.fusionauth.api.util.MapperTools;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Lambda;
import io.fusionauth.domain.LambdaType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.api.identityProvider.IdentityProviderConnectionTestRequest;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.BaseIdentityProviderApplicationConfiguration;
import io.fusionauth.domain.provider.BaseSAMLv2IdentityProvider;
import io.fusionauth.domain.provider.DomainBasedIdentityProvider;
import io.fusionauth.domain.provider.ExternalJWTIdentityProvider;
import io.fusionauth.domain.provider.FacebookIdentityProvider;
import io.fusionauth.domain.provider.GoogleApplicationConfiguration;
import io.fusionauth.domain.provider.GoogleIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderConnectionTestResult;
import io.fusionauth.domain.provider.IdentityProviderLinkingStrategy;
import io.fusionauth.domain.provider.IdentityProviderLoginMethod;
import io.fusionauth.domain.provider.IdentityProviderTenantConfiguration;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.provider.OpenIdConnectIdentityProvider;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.mybatis.guice.transactional.Transactional;
import org.primeframework.mvc.ErrorException;

public class DefaultIdentityProviderService implements IdentityProviderService {
  public static final Map<IdentityProviderType, LambdaType> SupportedLambdaTypes = new HashMap<>();
  
  private static final String UX_MODE_POPUP = "ux_mode=popup";
  
  private static final String UX_MODE_REDIRECT = "ux_mode=redirect";
  
  protected final ApplicationCache applicationCache;
  
  private final ApplicationMapper applicationMapper;
  
  private final CacheNotifier cacheNotifier;
  
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private final ExternalIdentifierService externalIdentifierService;
  
  private final IdentityProviderLinkMapper identityProviderLinkMapper;
  
  private final IdentityProviderMapper identityProviderMapper;
  
  private final IdentityProviderReaderService identityProviderReader;
  
  private final LambdaMapper lambdaMapper;
  
  private final TenantCache tenantCache;
  
  private final TenantMapper tenantMapper;
  
  private final Map<IdentityProviderType, IdentityProviderValidator> validators;
  
  @Inject
  public DefaultIdentityProviderService(ApplicationCache paramApplicationCache, ApplicationMapper paramApplicationMapper, CacheNotifier paramCacheNotifier, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, IdentityProviderLinkMapper paramIdentityProviderLinkMapper, IdentityProviderMapper paramIdentityProviderMapper, IdentityProviderReaderService paramIdentityProviderReaderService, Map<IdentityProviderType, IdentityProviderValidator> paramMap, LambdaMapper paramLambdaMapper, TenantCache paramTenantCache, TenantMapper paramTenantMapper) {
    this.applicationCache = paramApplicationCache;
    this.applicationMapper = paramApplicationMapper;
    this.cacheNotifier = paramCacheNotifier;
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    this.externalIdentifierService = paramExternalIdentifierService;
    this.identityProviderLinkMapper = paramIdentityProviderLinkMapper;
    this.identityProviderMapper = paramIdentityProviderMapper;
    this.identityProviderReader = paramIdentityProviderReaderService;
    this.lambdaMapper = paramLambdaMapper;
    this.tenantCache = paramTenantCache;
    this.tenantMapper = paramTenantMapper;
    this.validators = paramMap;
  }
  
  @Transactional
  public void _create(BaseIdentityProvider<?> paramBaseIdentityProvider) {
    paramBaseIdentityProvider.id = (UUID)Objects.requireNonNullElse(paramBaseIdentityProvider.id, UUID.randomUUID());
    if (paramBaseIdentityProvider.getType() == IdentityProviderType.HYPR) {
      paramBaseIdentityProvider.linkingStrategy = IdentityProviderLinkingStrategy.Unsupported;
      paramBaseIdentityProvider.tenantConfiguration.clear();
    } else if (paramBaseIdentityProvider.getType() == IdentityProviderType.OpenIDConnect) {
      OpenIdConnectIdentityProvider openIdConnectIdentityProvider = (OpenIdConnectIdentityProvider)paramBaseIdentityProvider;
      if (openIdConnectIdentityProvider.oauth2.issuer != null) {
        openIdConnectIdentityProvider.oauth2.authorization_endpoint = null;
        openIdConnectIdentityProvider.oauth2.token_endpoint = null;
        openIdConnectIdentityProvider.oauth2.userinfo_endpoint = null;
      } 
    } else if (paramBaseIdentityProvider.getType() == IdentityProviderType.Facebook) {
      FacebookIdentityProvider facebookIdentityProvider = (FacebookIdentityProvider)paramBaseIdentityProvider;
      facebookIdentityProvider.permissions = (facebookIdentityProvider.permissions == null) ? "email" : facebookIdentityProvider.permissions;
      facebookIdentityProvider.fields = (facebookIdentityProvider.fields == null) ? "email" : facebookIdentityProvider.fields;
      facebookIdentityProvider.loginMethod = (IdentityProviderLoginMethod)Objects.requireNonNullElse(facebookIdentityProvider.loginMethod, IdentityProviderLoginMethod.UseRedirect);
    } else if (paramBaseIdentityProvider.getType() == IdentityProviderType.Google) {
      GoogleIdentityProvider googleIdentityProvider = (GoogleIdentityProvider)paramBaseIdentityProvider;
      googleIdentityProvider.loginMethod = (IdentityProviderLoginMethod)Objects.requireNonNullElse(googleIdentityProvider.loginMethod, IdentityProviderLoginMethod.UseRedirect);
      handleGoogleIdLoginMethod(googleIdentityProvider);
    } 
    paramBaseIdentityProvider.insertInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramBaseIdentityProvider.lastUpdateInstant = paramBaseIdentityProvider.insertInstant;
    normalizeVerificationKeyIds(paramBaseIdentityProvider, null);
    this.identityProviderMapper.create(paramBaseIdentityProvider);
    createRelatedEntries(paramBaseIdentityProvider);
  }
  
  @Transactional
  public boolean _delete(BaseIdentityProvider<?> paramBaseIdentityProvider) {
    this.identityProviderMapper.deleteApplicationConfigurationsByIdentityProviderId(paramBaseIdentityProvider.id);
    this.identityProviderMapper.deleteTenantConfigurationsByIdentityProviderId(paramBaseIdentityProvider.id);
    this.identityProviderMapper.deleteVerificationKeys(paramBaseIdentityProvider.id);
    this.identityProviderMapper.deleteFederatedDomains(paramBaseIdentityProvider.id);
    this.identityProviderLinkMapper.deleteIdentityProviderLinksByIdentityProviderId(paramBaseIdentityProvider.id);
    return (this.identityProviderMapper.delete(paramBaseIdentityProvider.id) == 1);
  }
  
  @Transactional
  public int _update(BaseIdentityProvider<?> paramBaseIdentityProvider1, BaseIdentityProvider<?> paramBaseIdentityProvider2, @Nullable ZonedDateTime paramZonedDateTime) {
    BaseIdentityProvider<?> baseIdentityProvider = this.identityProviderMapper.retrieveByIdForUpdate(paramBaseIdentityProvider2.tenantId, paramBaseIdentityProvider2.id, paramZonedDateTime);
    if (baseIdentityProvider == null)
      return 0; 
    if (paramBaseIdentityProvider2.getType() == IdentityProviderType.HYPR) {
      paramBaseIdentityProvider2.linkingStrategy = IdentityProviderLinkingStrategy.Unsupported;
      paramBaseIdentityProvider2.tenantConfiguration.clear();
    } else if (paramBaseIdentityProvider2.getType() == IdentityProviderType.Google) {
      handleGoogleIdLoginMethod((GoogleIdentityProvider)paramBaseIdentityProvider2);
    } 
    paramBaseIdentityProvider2.insertInstant = paramBaseIdentityProvider1.insertInstant;
    paramBaseIdentityProvider2.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    paramBaseIdentityProvider2.source = paramBaseIdentityProvider1.source;
    paramBaseIdentityProvider2.tenantId = paramBaseIdentityProvider1.tenantId;
    normalizeVerificationKeyIds(paramBaseIdentityProvider2, paramBaseIdentityProvider1);
    createRelatedEntries(paramBaseIdentityProvider2);
    return this.identityProviderMapper.update(paramBaseIdentityProvider2);
  }
  
  public void create(BaseIdentityProvider<?> paramBaseIdentityProvider) {
    _create(paramBaseIdentityProvider);
    this.cacheNotifier.reload(new String[] { "CORSConfiguration", "IdentityProvider", "JSONWebKeys" });
  }
  
  public boolean delete(BaseIdentityProvider<?> paramBaseIdentityProvider) {
    boolean bool = _delete(paramBaseIdentityProvider);
    if (bool)
      this.cacheNotifier.reload(new String[] { "CORSConfiguration", "IdentityProvider", "JSONWebKeys" }); 
    return bool;
  }
  
  public void deleteAllByTenantId(UUID paramUUID) {
    if (paramUUID == null)
      return; 
    this.identityProviderMapper.retrieveAll(paramUUID).forEach(this::delete);
  }
  
  public IdentityProviderConnectionTestResult retrieveConnectionTestResult(ExternalIdentifier paramExternalIdentifier) {
    IdentityProviderConnectionTestResult identityProviderConnectionTestResult = new IdentityProviderConnectionTestResult();
    identityProviderConnectionTestResult.startInstant = paramExternalIdentifier.insertInstant;
    identityProviderConnectionTestResult.identityProviderId = paramExternalIdentifier.getAttributeAsUUID("identityProviderId");
    identityProviderConnectionTestResult
      .success = (paramExternalIdentifier.data.trace != null && !paramExternalIdentifier.data.trace.isEmpty() && paramExternalIdentifier.data.trace.stream().allMatch(ExternalIdentifier.TraceStep::success));
    identityProviderConnectionTestResult.email = paramExternalIdentifier.getAttribute("email");
    identityProviderConnectionTestResult.identityProviderUserId = paramExternalIdentifier.getAttribute("identityProviderUserId");
    identityProviderConnectionTestResult.username = paramExternalIdentifier.getAttribute("username");
    if (paramExternalIdentifier.data.trace != null)
      identityProviderConnectionTestResult.steps.addAll(paramExternalIdentifier.data.trace
          
          .stream()
          .map(paramTraceStep -> new IdentityProviderConnectionTestResult.IdentityProviderLoginStep(paramTraceStep.title(), paramTraceStep.success(), paramTraceStep.detail())).toList()); 
    return identityProviderConnectionTestResult;
  }
  
  public String startConnectionTest(Tenant paramTenant, IdentityProviderConnectionTestRequest paramIdentityProviderConnectionTestRequest) {
    ExternalIdentifier.ExternalIdData externalIdData = new ExternalIdentifier.ExternalIdData();
    externalIdData.setAttribute("identityProviderId", paramIdentityProviderConnectionTestRequest.identityProviderId.toString());
    return this.externalIdentifierService.createIdentityProviderConnectionTest(paramTenant, externalIdData);
  }
  
  public int update(BaseIdentityProvider<?> paramBaseIdentityProvider1, BaseIdentityProvider<?> paramBaseIdentityProvider2, @Nullable ZonedDateTime paramZonedDateTime) {
    int i = _update(paramBaseIdentityProvider1, paramBaseIdentityProvider2, paramZonedDateTime);
    if (i > 0)
      this.cacheNotifier.reload(new String[] { "CORSConfiguration", "IdentityProvider", "JSONWebKeys" }); 
    return i;
  }
  
  public IdentityProviderService.ValidationResult validate(UUID paramUUID, BaseIdentityProvider<?> paramBaseIdentityProvider, boolean paramBoolean) {
    IdentityProviderService.ValidationResult validationResult = handleRestrictedIdentityProviders(paramBaseIdentityProvider, paramBoolean);
    boolean bool = validationResult.errors.empty();
    if (!paramBoolean) {
      validationResult.existing = this.identityProviderReader.retrieveById(paramUUID, paramBaseIdentityProvider.id);
      if (validationResult.existing != null) {
        paramBaseIdentityProvider.tenantId = validationResult.existing.tenantId;
        validationResult.errors.add((new Validator())

            
            .ifTrue((validationResult.existing.enabled && paramBaseIdentityProvider.enabled && paramBaseIdentityProvider.getType() != IdentityProviderType.HYPR), paramValidator -> paramValidator.ensure((paramBaseIdentityProvider.linkingStrategy == paramValidationResult.existing.linkingStrategy), "identityProvider.linkingStrategy", "[restricted]", new Object[] { paramValidationResult.existing.linkingStrategy })).done());
      } else {
        throw new ErrorException("missing");
      } 
    } 
    validationResult.errors.add((new Validator())
        
        .ifTrue(bool, paramValidator -> paramValidator.notBlank(paramBaseIdentityProvider.name, "identityProvider.name", new Object[0]).ifLastCheckHadNoError(()).ensure(!((Set)RestrictedIdentityProviderTypes.stream().filter(()).map(()).collect(Collectors.toSet())).contains(paramBaseIdentityProvider.id), "identityProviderId", "[invalid]", new Object[] { paramBaseIdentityProvider.id }).ifLastCheckHadNoError(())).notBlank(paramBaseIdentityProvider.source, "identityProvider.source", new Object[0])
        .ifLastCheckHadNoError(paramValidator -> paramValidator.maxLength(paramBaseIdentityProvider.source, MapperTools.MaximumIndexedColumnLength, "identityProvider.source", new Object[] { Integer.valueOf(MapperTools.MaximumIndexedColumnLength) })).ifTrue((paramBaseIdentityProvider.applicationConfiguration != null), paramValidator -> paramValidator.forEach(paramBaseIdentityProvider.applicationConfiguration.keySet(), ()))








        
        .ifTrue((paramBaseIdentityProvider.lambdaConfiguration.reconcileId != null), paramValidator -> paramValidator.ensure(
            (paramBaseIdentityProvider.attributeMappings == null || paramBaseIdentityProvider.attributeMappings.isEmpty()), "identityProvider.lambdaConfiguration.reconcileId", "[unsupported]", new Object[0]).ifLastCheckHadNoError(()))












        
        .ifTrue((paramBaseIdentityProvider.linkingStrategy != null && paramBaseIdentityProvider.getType() != IdentityProviderType.HYPR), paramValidator -> paramValidator.ensure((paramBaseIdentityProvider.linkingStrategy != IdentityProviderLinkingStrategy.Unsupported), "identityProvider.linkingStrategy", Arrays.<IdentityProviderLinkingStrategy>stream(IdentityProviderLinkingStrategy.values()).filter(()).map(Enum::name).collect(Collectors.joining(", ")), new Object[0]))





        
        .ifTrue((paramBaseIdentityProvider.tenantId != null), paramValidator -> paramValidator.validObjectWithCode(this.tenantMapper.retrieveById(paramBaseIdentityProvider.tenantId), "identityProvider.tenantId", "[invalid]identityProvider.tenantId", new Object[] { paramBaseIdentityProvider.tenantId })).forEach(paramBaseIdentityProvider.tenantConfiguration.keySet(), (paramValidator, paramUUID, paramInteger) -> paramValidator.ifTrue((paramBaseIdentityProvider.tenantId != null), ()).ifNoFieldErrors("identityProvider.tenantConfiguration[" + String.valueOf(paramUUID) + "]", ()).ifLastCheckHadNoError(()))























        
        .done());
    if (bool) {
      IdentityProviderValidator identityProviderValidator = this.validators.get(paramBaseIdentityProvider.getType());
      if (identityProviderValidator != null)
        validationResult.errors.add(identityProviderValidator.validate(paramBaseIdentityProvider, validationResult.existing, paramBoolean)); 
    } 
    return validationResult;
  }
  
  public Errors validateClaim(UUID paramUUID, String paramString1, String paramString2) {
    return (new Validator())
      .notBlank(paramString1, "incomingClaim", new Object[0])
      .notBlank(paramString2, "fusionAuthClaim", new Object[0])
      .done();
  }
  
  public IdentityProviderService.ValidationResult validateDelete(UUID paramUUID1, UUID paramUUID2) {
    IdentityProviderService.ValidationResult validationResult = new IdentityProviderService.ValidationResult();
    validationResult.errors.add((new Validator())
        .notMissing(paramUUID2, "identityProviderId", new Object[0])
        .done());
    if (validationResult.errors.empty())
      validationResult.existing = this.identityProviderMapper.retrieveById(paramUUID1, paramUUID2); 
    return validationResult;
  }
  
  public IdentityProviderService.ValidationResult validateRetrieveConnectionTestResult(Tenant paramTenant, String paramString) {
    ExternalIdentifierReaderService.ValidationResult validationResult = this.externalIdentifierReader.validate(paramTenant, paramString, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.IdentityProviderConnectionTest });
    IdentityProviderService.ValidationResult validationResult1 = new IdentityProviderService.ValidationResult();
    validationResult1.connectionTestId = validationResult.id;
    validationResult1.tenant = validationResult.tenant;
    return validationResult1;
  }
  
  public IdentityProviderService.ValidationResult validateStartConnectionTest(Tenant paramTenant, IdentityProviderConnectionTestRequest paramIdentityProviderConnectionTestRequest) {
    IdentityProviderService.ValidationResult validationResult = new IdentityProviderService.ValidationResult();
    validationResult.tenant = (paramTenant != null) ? paramTenant : this.tenantMapper.retrieveById(paramIdentityProviderConnectionTestRequest.tenantId);
    validationResult.existing = this.identityProviderMapper.retrieveById(null, paramIdentityProviderConnectionTestRequest.identityProviderId);
    validationResult.errors.add((new Validator())

        
        .notMissing(paramIdentityProviderConnectionTestRequest.tenantId, "tenantId", new Object[0])
        .ifLastCheckHadNoError(paramValidator -> paramValidator.validObject(paramValidationResult.tenant, "tenantId", new Object[] { paramIdentityProviderConnectionTestRequest.tenantId })).notMissing(paramIdentityProviderConnectionTestRequest.identityProviderId, "identityProviderId", new Object[0])
        .ifLastCheckHadNoError(paramValidator -> paramValidator.ensureWithCode((paramValidationResult.existing != null && (paramValidationResult.existing.tenantId == null || paramValidationResult.existing.tenantId.equals(paramIdentityProviderConnectionTestRequest.tenantId))), "identityProviderId", "[invalid]identityProviderId", new Object[] { paramIdentityProviderConnectionTestRequest.identityProviderId })).done());
    return validationResult;
  }
  
  private Application cacheOrDbLookupIgnoreActive(UUID paramUUID1, UUID paramUUID2) {
    Application application = this.applicationCache.get(paramUUID1, paramUUID2);
    if (application == null)
      application = this.applicationMapper.retrieveByIdIgnoreActive(paramUUID1, paramUUID2); 
    return application;
  }
  
  private void createRelatedEntries(BaseIdentityProvider<?> paramBaseIdentityProvider) {
    if (paramBaseIdentityProvider instanceof DomainBasedIdentityProvider) {
      DomainBasedIdentityProvider domainBasedIdentityProvider = (DomainBasedIdentityProvider)paramBaseIdentityProvider;
      this.identityProviderMapper.deleteFederatedDomains(paramBaseIdentityProvider.id);
      MapperTools.safeCreateUpdate(5000, domainBasedIdentityProvider.getDomains(), paramList -> this.identityProviderMapper.createFederatedDomains(paramBaseIdentityProvider.id, paramList, paramBaseIdentityProvider.tenantId));
    } 
    this.identityProviderMapper.deleteApplicationConfigurationsByIdentityProviderId(paramBaseIdentityProvider.id);
    MapperTools.safeCreateUpdate(5000, paramBaseIdentityProvider.applicationConfiguration, paramMap -> this.identityProviderMapper.createApplicationConfiguration(paramBaseIdentityProvider.id, paramMap, paramBaseIdentityProvider.getType()));
    this.identityProviderMapper.deleteTenantConfigurationsByIdentityProviderId(paramBaseIdentityProvider.id);
    MapperTools.safeCreateUpdate(5000, paramBaseIdentityProvider.tenantConfiguration, paramMap -> this.identityProviderMapper.createTenantConfiguration(paramBaseIdentityProvider.id, paramMap));
    writeVerificationKeys(paramBaseIdentityProvider);
  }
  
  private String getAlreadyCreatedErrorFromType(IdentityProviderType paramIdentityProviderType) {
    switch (paramIdentityProviderType) {
      case Apple:
      
      case EpicGames:
      
      case Facebook:
      
      case Google:
      
      case HYPR:
      
      case LinkedIn:
      
      case Nintendo:
      
      case SonyPSN:
      
      case Steam:
      
      case Twitch:
      
      case Twitter:
      
      case Xbox:
      
    } 
    throw new IllegalArgumentException("Unrestricted IdP type: " + String.valueOf(paramIdentityProviderType));
  }
  
  private void handleGoogleIdLoginMethod(GoogleIdentityProvider paramGoogleIdentityProvider) {
    if (paramGoogleIdentityProvider.loginMethod == IdentityProviderLoginMethod.UsePopup && 
      paramGoogleIdentityProvider.properties.api.contains("ux_mode=redirect"))
      paramGoogleIdentityProvider.properties.api = paramGoogleIdentityProvider.properties.api.replace("ux_mode=redirect", "ux_mode=popup"); 
    boolean bool = paramGoogleIdentityProvider.properties.api.contains("ux_mode=redirect");
    paramGoogleIdentityProvider.applicationConfiguration.forEach((paramUUID, paramGoogleApplicationConfiguration) -> {
          if (paramGoogleIdentityProvider.lookupLoginMethod(paramUUID) == IdentityProviderLoginMethod.UsePopup)
            if (StringTools.isTrimmedEmpty(paramGoogleApplicationConfiguration.properties.api)) {
              if (paramBoolean)
                paramGoogleApplicationConfiguration.properties.api = "ux_mode=popup"; 
            } else if (paramGoogleApplicationConfiguration.properties.api.contains("ux_mode=redirect")) {
              paramGoogleApplicationConfiguration.properties.api = paramGoogleApplicationConfiguration.properties.api.replace("ux_mode=redirect", "ux_mode=popup");
            } else if (paramBoolean) {
              paramGoogleApplicationConfiguration.properties.api += "\nux_mode=popup";
            }  
        });
  }
  
  private IdentityProviderService.ValidationResult handleRestrictedIdentityProviders(BaseIdentityProvider<?> paramBaseIdentityProvider, boolean paramBoolean) {
    IdentityProviderService.ValidationResult validationResult = new IdentityProviderService.ValidationResult(paramBaseIdentityProvider);
    IdentityProviderType identityProviderType = paramBaseIdentityProvider.getType();
    if (RestrictedIdentityProviderTypes.contains(identityProviderType)) {
      if (paramBoolean && paramBaseIdentityProvider.id == null && paramBaseIdentityProvider.name == null && paramBaseIdentityProvider.tenantId == null) {
        if (this.identityProviderMapper
          .retrieveById(null, identityProviderType.id) != null || this.identityProviderMapper
          .retrieveByName(null, identityProviderType.displayName()) != null) {
          validationResult.errors.addGeneralError(getAlreadyCreatedErrorFromType(identityProviderType), null, new Object[0]);
          return validationResult;
        } 
        validationResult.identityProvider.id = identityProviderType.id;
        validationResult.identityProvider.name = identityProviderType.displayName();
      } 
      if (!paramBoolean) {
        if (paramBaseIdentityProvider.id == null)
          paramBaseIdentityProvider.id = identityProviderType.id; 
        if (paramBaseIdentityProvider.name == null && paramBaseIdentityProvider.id.equals(identityProviderType.id))
          paramBaseIdentityProvider.name = identityProviderType.displayName(); 
      } 
    } 
    return validationResult;
  }
  
  private void normalizeVerificationKeyIds(BaseIdentityProvider<?> paramBaseIdentityProvider1, BaseIdentityProvider<?> paramBaseIdentityProvider2) {
    if (paramBaseIdentityProvider1 instanceof ExternalJWTIdentityProvider) {
      ExternalJWTIdentityProvider externalJWTIdentityProvider1 = (ExternalJWTIdentityProvider)paramBaseIdentityProvider1;
      ExternalJWTIdentityProvider externalJWTIdentityProvider3 = (ExternalJWTIdentityProvider)paramBaseIdentityProvider2, externalJWTIdentityProvider2 = (paramBaseIdentityProvider2 instanceof ExternalJWTIdentityProvider) ? externalJWTIdentityProvider3 : null;
      externalJWTIdentityProvider1
        
        .verificationKeyIds = (externalJWTIdentityProvider2 == null) ? KeyHelper.normalizeVerificationKeyIds(externalJWTIdentityProvider1.defaultKeyId, externalJWTIdentityProvider1.verificationKeyIds) : KeyHelper.normalizeVerificationKeyIds(externalJWTIdentityProvider1.defaultKeyId, externalJWTIdentityProvider1.verificationKeyIds, externalJWTIdentityProvider2.defaultKeyId, externalJWTIdentityProvider2.verificationKeyIds);
      externalJWTIdentityProvider1.defaultKeyId = externalJWTIdentityProvider1.verificationKeyIds.isEmpty() ? null : (UUID)externalJWTIdentityProvider1.verificationKeyIds.getFirst();
    } else if (paramBaseIdentityProvider1 instanceof BaseSAMLv2IdentityProvider) {
      BaseSAMLv2IdentityProvider baseSAMLv2IdentityProvider1 = (BaseSAMLv2IdentityProvider)paramBaseIdentityProvider1;
      BaseSAMLv2IdentityProvider baseSAMLv2IdentityProvider3 = (BaseSAMLv2IdentityProvider)paramBaseIdentityProvider2, baseSAMLv2IdentityProvider2 = (paramBaseIdentityProvider2 instanceof BaseSAMLv2IdentityProvider) ? baseSAMLv2IdentityProvider3 : null;
      baseSAMLv2IdentityProvider1
        
        .verificationKeyIds = (baseSAMLv2IdentityProvider2 == null) ? KeyHelper.normalizeVerificationKeyIds(baseSAMLv2IdentityProvider1.keyId, baseSAMLv2IdentityProvider1.verificationKeyIds) : KeyHelper.normalizeVerificationKeyIds(baseSAMLv2IdentityProvider1.keyId, baseSAMLv2IdentityProvider1.verificationKeyIds, baseSAMLv2IdentityProvider2.keyId, baseSAMLv2IdentityProvider2.verificationKeyIds);
      baseSAMLv2IdentityProvider1.keyId = baseSAMLv2IdentityProvider1.verificationKeyIds.isEmpty() ? null : (UUID)baseSAMLv2IdentityProvider1.verificationKeyIds.getFirst();
    } 
  }
  
  private void validLambda(Validator paramValidator, IdentityProviderType paramIdentityProviderType, UUID paramUUID) {
    paramValidator.holdMyBeer(this.lambdaMapper.retrieveById(paramUUID))
      .validObject(paramValidator.barkeep(), "identityProvider.lambdaConfiguration.reconcileId", new Object[] { paramUUID }).ifLastCheckHadNoError(() -> paramValidator.ensure((((Lambda)paramValidator.barkeep(Lambda.class)).type == SupportedLambdaTypes.get(paramIdentityProviderType)), "identityProvider.lambdaConfiguration.reconcileId", "[type]", new Object[] { paramUUID, SupportedLambdaTypes.get(paramIdentityProviderType) }));
  }
  
  private void writeVerificationKeys(BaseIdentityProvider<?> paramBaseIdentityProvider) {
    this.identityProviderMapper.deleteVerificationKeys(paramBaseIdentityProvider.id);
    if (paramBaseIdentityProvider instanceof ExternalJWTIdentityProvider) {
      ExternalJWTIdentityProvider externalJWTIdentityProvider = (ExternalJWTIdentityProvider)paramBaseIdentityProvider;
      if (!externalJWTIdentityProvider.verificationKeyIds.isEmpty())
        this.identityProviderMapper.createVerificationKeys(paramBaseIdentityProvider.id, IdentityProviderKeyType.ExternalJwtVerification.name(), externalJWTIdentityProvider.verificationKeyIds); 
    } else if (paramBaseIdentityProvider instanceof BaseSAMLv2IdentityProvider) {
      BaseSAMLv2IdentityProvider baseSAMLv2IdentityProvider = (BaseSAMLv2IdentityProvider)paramBaseIdentityProvider;
      if (!baseSAMLv2IdentityProvider.verificationKeyIds.isEmpty())
        this.identityProviderMapper.createVerificationKeys(paramBaseIdentityProvider.id, IdentityProviderKeyType.Samlv2ResponseVerification.name(), baseSAMLv2IdentityProvider.verificationKeyIds); 
    } 
  }
  
  static {
    SupportedLambdaTypes.put(IdentityProviderType.Apple, LambdaType.AppleReconcile);
    SupportedLambdaTypes.put(IdentityProviderType.EpicGames, LambdaType.EpicGamesReconcile);
    SupportedLambdaTypes.put(IdentityProviderType.ExternalJWT, LambdaType.ExternalJWTReconcile);
    SupportedLambdaTypes.put(IdentityProviderType.Facebook, LambdaType.FacebookReconcile);
    SupportedLambdaTypes.put(IdentityProviderType.Google, LambdaType.GoogleReconcile);
    SupportedLambdaTypes.put(IdentityProviderType.HYPR, LambdaType.HYPRReconcile);
    SupportedLambdaTypes.put(IdentityProviderType.LinkedIn, LambdaType.LinkedInReconcile);
    SupportedLambdaTypes.put(IdentityProviderType.Nintendo, LambdaType.NintendoReconcile);
    SupportedLambdaTypes.put(IdentityProviderType.OpenIDConnect, LambdaType.OpenIDReconcile);
    SupportedLambdaTypes.put(IdentityProviderType.SAMLv2IdPInitiated, LambdaType.SAMLv2Reconcile);
    SupportedLambdaTypes.put(IdentityProviderType.SAMLv2, LambdaType.SAMLv2Reconcile);
    SupportedLambdaTypes.put(IdentityProviderType.SonyPSN, LambdaType.SonyPSNReconcile);
    SupportedLambdaTypes.put(IdentityProviderType.Steam, LambdaType.SteamReconcile);
    SupportedLambdaTypes.put(IdentityProviderType.Twitch, LambdaType.TwitchReconcile);
    SupportedLambdaTypes.put(IdentityProviderType.Twitter, LambdaType.TwitterReconcile);
    SupportedLambdaTypes.put(IdentityProviderType.Xbox, LambdaType.XboxReconcile);
  }
}
