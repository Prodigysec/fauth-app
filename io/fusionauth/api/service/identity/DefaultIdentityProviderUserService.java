package io.fusionauth.api.service.identity;

import com.google.inject.Inject;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.IdentityProviderLinkMapper;
import io.fusionauth.api.service.system.EventHelper;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.IdentityProviderLink;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Tenantable;
import io.fusionauth.domain.User;
import io.fusionauth.domain.event.UserIdentityProviderLinkEvent;
import io.fusionauth.domain.event.UserIdentityProviderUnlinkEvent;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderTenantConfiguration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.mybatis.guice.transactional.Transactional;

public class DefaultIdentityProviderUserService implements IdentityProviderUserService {
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private final ExternalIdentifierService externalIdentifierService;
  
  private final IdentityProviderLinkMapper identityProviderLinkMapper;
  
  private final IdentityProviderReaderService identityProviderReader;
  
  private final TenantReaderService tenantReader;
  
  private final UserReaderService userReaderService;
  
  @Inject
  public DefaultIdentityProviderUserService(ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, IdentityProviderReaderService paramIdentityProviderReaderService, IdentityProviderLinkMapper paramIdentityProviderLinkMapper, TenantReaderService paramTenantReaderService, UserReaderService paramUserReaderService) {
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    this.externalIdentifierService = paramExternalIdentifierService;
    this.identityProviderReader = paramIdentityProviderReaderService;
    this.identityProviderLinkMapper = paramIdentityProviderLinkMapper;
    this.tenantReader = paramTenantReaderService;
    this.userReaderService = paramUserReaderService;
  }
  
  @Transactional
  public void _link(Tenant paramTenant, User paramUser, BaseIdentityProvider<?> paramBaseIdentityProvider, String paramString1, String paramString2, String paramString3, String paramString4) {
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    this.identityProviderLinkMapper.upsertIdentityProviderLink((new IdentityProviderLink())
        .with(paramIdentityProviderLink -> paramIdentityProviderLink.displayName = paramString)
        .with(paramIdentityProviderLink -> paramIdentityProviderLink.identityProviderId = paramBaseIdentityProvider.id)
        .with(paramIdentityProviderLink -> paramIdentityProviderLink.identityProviderUserId = paramString)
        .with(paramIdentityProviderLink -> paramIdentityProviderLink.insertInstant = paramZonedDateTime)
        .with(paramIdentityProviderLink -> paramIdentityProviderLink.lastLoginInstant = paramZonedDateTime)
        .with(paramIdentityProviderLink -> paramIdentityProviderLink.tenantId = paramTenant.id)
        .with(paramIdentityProviderLink -> paramIdentityProviderLink.token = paramString)
        .with(paramIdentityProviderLink -> paramIdentityProviderLink.userId = paramUser.id));
    if (paramString4 != null)
      this.externalIdentifierService.deleteById(paramString4); 
  }
  
  public IdentityProviderLink link(EventInfo paramEventInfo, Tenant paramTenant, User paramUser, BaseIdentityProvider<?> paramBaseIdentityProvider, String paramString1, String paramString2, String paramString3, String paramString4) {
    _link(paramTenant, paramUser, paramBaseIdentityProvider, paramString1, paramString2, paramString3, paramString4);
    IdentityProviderLink identityProviderLink = retrieveIdentityProviderUser(paramTenant, paramBaseIdentityProvider, paramString1, paramUser);
    EventHelper.send(paramTenant, null, new UserIdentityProviderLinkEvent(paramEventInfo, identityProviderLink, paramUser));
    return identityProviderLink;
  }
  
  public IdentityProviderLink retrieveIdentityProviderUser(Tenant paramTenant, BaseIdentityProvider<?> paramBaseIdentityProvider, String paramString, User paramUser) {
    return this.identityProviderLinkMapper.retrieveIdentityProviderLink(paramTenant.id, paramBaseIdentityProvider.id, paramString, (paramUser != null) ? paramUser.id : null);
  }
  
  public List<IdentityProviderLink> retrieveIdentityProviderUsersByUser(Tenant paramTenant, BaseIdentityProvider<?> paramBaseIdentityProvider, UUID paramUUID) {
    return this.identityProviderLinkMapper.retrieveIdentityProviderLinksByUserId(paramTenant.id, (paramBaseIdentityProvider != null) ? paramBaseIdentityProvider.id : null, paramUUID);
  }
  
  public void unlink(EventInfo paramEventInfo, Tenant paramTenant, BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLink paramIdentityProviderLink, User paramUser) {
    this.identityProviderLinkMapper.deleteIdentityProviderLink(paramTenant.id, paramBaseIdentityProvider.id, paramIdentityProviderLink.identityProviderUserId);
    EventHelper.send(paramTenant, null, new UserIdentityProviderUnlinkEvent(paramEventInfo, paramIdentityProviderLink, paramUser));
  }
  
  public IdentityProviderUserService.ValidationResult validateLink(Tenant paramTenant, UUID paramUUID1, String paramString1, String paramString2, UUID paramUUID2) {
    IdentityProviderUserService.ValidationResult validationResult = new IdentityProviderUserService.ValidationResult();
    validationResult.identityProvider = (paramUUID1 != null) ? this.identityProviderReader.retrieveById(null, paramUUID1) : null;
    validationResult.identityProviderUserId = paramString1;
    validationResult.user = (paramUUID2 != null) ? this.userReaderService.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID2) : null;
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.user });
    validationResult.externalIdentifier = (paramString2 != null) ? this.externalIdentifierReader.retrieveByType((validationResult.tenant != null) ? validationResult.tenant : null, paramString2, ExternalIdentifier.ExternalIdType.PendingIdPLinkId) : null;
    validationResult.link = (validationResult.identityProviderUserId != null) ? this.identityProviderReader.retrieveIdProviderUser((validationResult.tenant != null) ? validationResult.tenant.id : null, paramUUID1, validationResult.identityProviderUserId) : null;
    if (paramUUID1 == null && validationResult.externalIdentifier != null) {
      UUID uUID = UUID.fromString(validationResult.externalIdentifier.getAttribute("identityProviderId"));
      validationResult.identityProviderUserId = validationResult.externalIdentifier.getAttribute("identityProviderUserId");
      validationResult.identityProvider = this.identityProviderReader.retrieveById(null, uUID);
    } 
    IdentityProviderTenantConfiguration identityProviderTenantConfiguration = (validationResult.tenant != null && validationResult.identityProvider != null) ? validationResult.identityProvider.tenantConfiguration.get(validationResult.tenant.id) : null;
    List<IdentityProviderLink> list = (validationResult.tenant != null && validationResult.identityProvider != null && validationResult.user != null && identityProviderTenantConfiguration != null && identityProviderTenantConfiguration.limitUserLinkCount.enabled) ? this.identityProviderLinkMapper.retrieveIdentityProviderLinksByUserId(validationResult.tenant.id, validationResult.identityProvider.id, validationResult.user.id) : null;
    validationResult







































      
      .errors = (new Validator()).ifTrue((paramString2 == null), paramValidator -> paramValidator.notMissing(paramUUID, "identityProviderId", new Object[0]).ifLastCheckHadNoError(()).notBlank(paramString, "identityProviderUserId", new Object[0]).ifLastCheckHadNoError(())).ifTrue((paramString2 != null), paramValidator -> paramValidator.validObject(paramValidationResult.externalIdentifier, "pendingIdPLinkId", new Object[0]).ifLastCheckHadNoError(())).notMissing(paramUUID2, "userId", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.validObject(paramValidationResult.user, "userId", new Object[] { paramUUID })).ifNoFieldErrors("identityProviderUserId", paramValidator -> paramValidator.ifTrue((paramList != null), ())).done();
    return validationResult;
  }
  
  public IdentityProviderUserService.ValidationResult validatePendingLinkRetrieve(Tenant paramTenant, String paramString, UUID paramUUID) {
    IdentityProviderUserService.ValidationResult validationResult = new IdentityProviderUserService.ValidationResult();
    validationResult.tenant = paramTenant;
    validationResult
      
      .errors = (new Validator()).notBlank(paramString, "pendingLinkId", new Object[0]).done();
    if (validationResult.errors.empty()) {
      ExternalIdentifierReaderService.ValidationResult validationResult1 = this.externalIdentifierReader.validate(validationResult.tenant, paramString, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.PendingIdPLinkId });
      validationResult.externalIdentifier = validationResult1.id;
      validationResult.tenant = this.tenantReader.resolve(validationResult.tenant, new Tenantable[] { validationResult1.id });
      if (validationResult.externalIdentifier == null)
        return validationResult; 
      validationResult.identityProvider = this.identityProviderReader.retrieveById(null, validationResult.externalIdentifier.getAttributeAsUUID("identityProviderId"));
      if (validationResult.identityProvider == null || (validationResult.identityProvider.tenantId != null && 
        !validationResult.identityProvider.tenantId.equals(validationResult.tenant.id))) {
        validationResult.externalIdentifier = null;
        return validationResult;
      } 
      validationResult.user = (paramUUID != null) ? this.userReaderService.retrieveById(validationResult.tenant.id, paramUUID) : null;
      validationResult
        
        .errors = (new Validator()).ifTrue((paramUUID != null), paramValidator -> paramValidator.notMissing(paramValidationResult.user, "userId", new Object[] { "[invalid]", paramUUID })).done();
    } 
    if (validationResult.errors.empty() && validationResult.user != null)
      validationResult.userLinks = this.identityProviderLinkMapper.retrieveIdentityProviderLinksByUserId(validationResult.tenant.id, validationResult.identityProvider.id, validationResult.user.id); 
    return validationResult;
  }
  
  public IdentityProviderUserService.ValidationResult validateRetrieve(Tenant paramTenant, UUID paramUUID1, String paramString, UUID paramUUID2) {
    IdentityProviderUserService.ValidationResult validationResult = new IdentityProviderUserService.ValidationResult();
    validationResult.identityProvider = (paramUUID1 != null) ? this.identityProviderReader.retrieveById(null, paramUUID1) : null;
    validationResult.user = (paramUUID2 != null) ? this.userReaderService.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID2) : null;
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.user });
    validationResult













      
      .errors = (new Validator()).ifTrue((paramString == null && paramUUID2 == null), paramValidator -> paramValidator.notMissing(paramUUID, "identityProviderId", new Object[0])).ifTrue((paramUUID1 != null), paramValidator -> paramValidator.validObject(paramValidationResult.identityProvider, "identityProviderId", new Object[] { paramUUID }).ifLastCheckHadNoError(())).ifTrue((paramUUID2 != null), paramValidator -> paramValidator.validObject(paramValidationResult.user, "userId", new Object[] { paramUUID })).done();
    return validationResult;
  }
  
  public IdentityProviderUserService.ValidationResult validateUnlink(Tenant paramTenant, UUID paramUUID1, String paramString, UUID paramUUID2) {
    IdentityProviderUserService.ValidationResult validationResult = new IdentityProviderUserService.ValidationResult();
    validationResult.identityProvider = (paramUUID1 != null) ? this.identityProviderReader.retrieveById(null, paramUUID1) : null;
    validationResult.user = (paramUUID2 != null) ? this.userReaderService.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID2) : null;
    validationResult.tenant = this.tenantReader.resolve(paramTenant, new Tenantable[] { validationResult.user });
    validationResult.link = (paramString != null) ? this.identityProviderReader.retrieveIdProviderUser((validationResult.tenant != null) ? validationResult.tenant.id : null, paramUUID1, paramString) : null;
    validationResult










      
      .errors = (new Validator()).notMissing(paramUUID1, "identityProviderId", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.validObject(paramValidationResult.identityProvider, "identityProviderId", new Object[] { paramUUID })).notBlank(paramString, "identityProviderUserId", new Object[0]).notMissing(paramUUID2, "userId", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.validObject(paramValidationResult.user, "userId", new Object[] { paramUUID })).done();
    return validationResult;
  }
}
