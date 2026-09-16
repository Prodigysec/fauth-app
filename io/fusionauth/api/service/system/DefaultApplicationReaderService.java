package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.ApplicationMapper;
import io.fusionauth.api.domain.InstanceMapper;
import io.fusionauth.api.util.MapperTools;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationOAuthScope;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.search.ApplicationSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultApplicationReaderService implements ApplicationReaderService {
  private final ApplicationMapper applicationMapper;
  
  private final FusionAuthConfiguration configuration;
  
  private final InstanceMapper instanceMapper;
  
  @Inject
  public DefaultApplicationReaderService(ApplicationMapper paramApplicationMapper, FusionAuthConfiguration paramFusionAuthConfiguration, InstanceMapper paramInstanceMapper) {
    this.applicationMapper = paramApplicationMapper;
    this.configuration = paramFusionAuthConfiguration;
    this.instanceMapper = paramInstanceMapper;
  }
  
  public List<Application> retrieveAll(UUID paramUUID, Set<ApplicationReaderService.ApplicationExpansion> paramSet) {
    return process(this.applicationMapper.retrieveAll(paramUUID), paramSet);
  }
  
  public List<Application> retrieveAllIgnoreActive(UUID paramUUID, Set<ApplicationReaderService.ApplicationExpansion> paramSet) {
    return process(this.applicationMapper.retrieveAllIgnoreActive(paramUUID), paramSet);
  }
  
  public List<Application> retrieveAllInactive(UUID paramUUID, Set<ApplicationReaderService.ApplicationExpansion> paramSet) {
    return process(this.applicationMapper.retrieveAllInactive(paramUUID), paramSet);
  }
  
  public List<ApplicationMapper.ApplicationId> retrieveAllUsingIPAccessControlList(UUID paramUUID) {
    return this.applicationMapper.retrieveAllUsingIPAccessControlList(paramUUID);
  }
  
  public Application retrieveById(UUID paramUUID1, UUID paramUUID2) {
    return process(this.applicationMapper.retrieveById(paramUUID1, paramUUID2), ApplicationReaderService.ApplicationExpansion.all());
  }
  
  public Application retrieveByIdIgnoreActive(UUID paramUUID1, UUID paramUUID2) {
    return process(this.applicationMapper.retrieveByIdIgnoreActive(paramUUID1, paramUUID2), ApplicationReaderService.ApplicationExpansion.all());
  }
  
  public List<Application> retrieveByIds(UUID paramUUID, List<UUID> paramList, Set<ApplicationReaderService.ApplicationExpansion> paramSet) {
    if (paramList == null || paramList.isEmpty())
      return List.of(); 
    List<?> list = MapperTools.safeRetrieve(this.configuration.internalApplicationReaderBatchSize(), paramList, paramList -> this.applicationMapper.retrieveByIds(paramUUID, paramList));
    return process((List)list, paramSet);
  }
  
  public Application retrieveByName(UUID paramUUID, String paramString) {
    return process(this.applicationMapper.retrieveByName(paramUUID, paramString), ApplicationReaderService.ApplicationExpansion.all());
  }
  
  public Application retrieveBySAMLv2Issuer(UUID paramUUID, String paramString) {
    return process(this.applicationMapper.retrieveBySAMLv2Issuer(paramUUID, paramString), ApplicationReaderService.ApplicationExpansion.all());
  }
  
  public int retrieveCountByThemeId(UUID paramUUID) {
    return this.applicationMapper.retrieveCountByThemeId(paramUUID);
  }
  
  public Application retrieveDefaultApplication() {
    return process(this.applicationMapper.retrieveById(null, Application.FUSIONAUTH_APP_ID), ApplicationReaderService.ApplicationExpansion.all());
  }
  
  public ApplicationOAuthScope retrieveOAuthScopeById(UUID paramUUID1, UUID paramUUID2, UUID paramUUID3) {
    return this.applicationMapper.retrieveOAuthScopeById(paramUUID1, paramUUID2, paramUUID3);
  }
  
  public ApplicationOAuthScope retrieveOAuthScopeByName(UUID paramUUID1, UUID paramUUID2, String paramString) {
    return this.applicationMapper.retrieveOAuthScopeByName(paramUUID1, paramUUID2, paramString);
  }
  
  public ApplicationRole retrieveRoleById(UUID paramUUID1, UUID paramUUID2, UUID paramUUID3) {
    return this.applicationMapper.retrieveRoleById(paramUUID1, paramUUID2, paramUUID3);
  }
  
  public ApplicationRole retrieveRoleByName(UUID paramUUID1, UUID paramUUID2, String paramString) {
    return this.applicationMapper.retrieveRoleByName(paramUUID1, paramUUID2, paramString);
  }
  
  public List<ApplicationRole> retrieveRolesByIds(UUID paramUUID, Collection<UUID> paramCollection) {
    return MapperTools.safeRetrieve(32000, paramCollection, paramList -> this.applicationMapper.retrieveRolesByIds(paramUUID, paramList));
  }
  
  public List<ApplicationRole> retrieveRolesByNames(UUID paramUUID1, UUID paramUUID2, Collection<String> paramCollection) {
    return MapperTools.safeRetrieve(32000, paramCollection, paramList -> this.applicationMapper.retrieveRolesByNames(paramUUID1, paramUUID2, paramList));
  }
  
  public UUID retrieveTenantManagerApplicationId() {
    return this.instanceMapper.retrieveTenantManagerApplicationsId();
  }
  
  public SearchResults<Application> search(ApplicationSearchCriteria paramApplicationSearchCriteria, Set<ApplicationReaderService.ApplicationExpansion> paramSet) {
    int i = this.applicationMapper.retrieveCountByCriteria(paramApplicationSearchCriteria);
    if (i == 0)
      return new SearchResults<>(Collections.emptyList(), i); 
    List<Application> list = this.applicationMapper.retrieveByCriteria(paramApplicationSearchCriteria);
    return new SearchResults<>(process(list, paramSet), i);
  }
  
  public List<UUID> translateCleanSpeakApplicationIds(List<UUID> paramList) {
    List<UUID> list1 = this.applicationMapper.retrieveIdsByCleanSpeakIds(paramList);
    List<UUID> list2 = this.applicationMapper.retrieveValidIds(paramList);
    HashSet<UUID> hashSet = new HashSet<>(list1);
    hashSet.addAll(list2);
    return new ArrayList<>(hashSet);
  }
  
  private Application expand(Application paramApplication, Set<ApplicationReaderService.ApplicationExpansion> paramSet) {
    if (paramApplication == null)
      return null; 
    return (Application)expand(List.of(paramApplication), paramSet).getFirst();
  }
  
  private List<Application> expand(List<Application> paramList, Set<ApplicationReaderService.ApplicationExpansion> paramSet) {
    if (paramSet == null || paramSet.isEmpty())
      return paramList; 
    List<Application> list = paramList.stream().map(Application::new).toList();
    List<?> list1 = paramList.stream().map(paramApplication -> paramApplication.id).toList();
    if (paramSet.contains(ApplicationReaderService.ApplicationExpansion.roles)) {
      Objects.requireNonNull(this.applicationMapper);
      List<?> list2 = MapperTools.safeRetrieve(this.configuration.internalApplicationReaderBatchSize(), list1, this.applicationMapper::retrieveRolesByApplicationIds);
      Map map = (Map)list2.stream().collect(Collectors.groupingBy(paramApplicationRole -> paramApplicationRole.applicationId, Collectors.toList()));
      list.forEach(paramApplication -> paramApplication.roles.addAll((Collection<? extends ApplicationRole>)paramMap.getOrDefault(paramApplication.id, List.of())));
    } 
    if (paramSet.contains(ApplicationReaderService.ApplicationExpansion.scopes)) {
      List<?> list2 = MapperTools.safeRetrieve(this.configuration.internalApplicationReaderBatchSize(), list1, paramList -> this.applicationMapper.retrieveOAuthScopesByApplicationIds(null, paramList));
      Map map = (Map)list2.stream().collect(Collectors.groupingBy(paramApplicationOAuthScope -> paramApplicationOAuthScope.applicationId, Collectors.toList()));
      list.forEach(paramApplication -> paramApplication.scopes.addAll((Collection<? extends ApplicationOAuthScope>)paramMap.getOrDefault(paramApplication.id, List.of())));
    } 
    return list;
  }
  
  private Application process(Application paramApplication, Set<ApplicationReaderService.ApplicationExpansion> paramSet) {
    return setKeyDefaults(expand(paramApplication, paramSet));
  }
  
  private List<Application> process(List<Application> paramList, Set<ApplicationReaderService.ApplicationExpansion> paramSet) {
    return setKeyDefaults(expand(paramList, paramSet));
  }
  
  private List<Application> setKeyDefaults(List<Application> paramList) {
    return paramList.stream()
      .map(this::setKeyDefaults)
      .toList();
  }
  
  private Application setKeyDefaults(Application paramApplication) {
    if (paramApplication == null)
      return null; 
    Application.SAMLv2Configuration sAMLv2Configuration = paramApplication.samlv2Configuration;
    if (!sAMLv2Configuration.verificationKeyIds.isEmpty())
      sAMLv2Configuration.defaultVerificationKeyId = (UUID)sAMLv2Configuration.verificationKeyIds.getFirst(); 
    if (!sAMLv2Configuration.logout.verificationKeyIds.isEmpty())
      sAMLv2Configuration.logout.defaultVerificationKeyId = (UUID)sAMLv2Configuration.logout.verificationKeyIds.getFirst(); 
    return paramApplication;
  }
}
