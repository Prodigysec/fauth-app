package io.fusionauth.app.action.admin.identityProvider;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.cors.CORSUtils;
import io.fusionauth.app.action.admin.BaseSearchAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.CORSConfiguration;
import io.fusionauth.domain.RequiresCORSConfiguration;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.IdentityProviderResponse;
import io.fusionauth.domain.api.IdentityProviderSearchRequest;
import io.fusionauth.domain.api.IdentityProviderSearchResponse;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.IdentityProviderSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.parameter.annotation.PreRenderMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "system_manager"})
public class IndexAction extends BaseSearchAction<BaseIdentityProvider<?>, IdentityProviderSearchCriteria> {
  @FTLVariable
  public List<Application> applications = new ArrayList<>();
  
  public Map<String, CORSConfiguration> requiredCORSConfiguration = new LinkedHashMap<>();
  
  @FTLVariable
  public List<IdentityProviderType> types = List.of((Object[])new IdentityProviderType[] { 
        IdentityProviderType.Apple, IdentityProviderType.EpicGames, IdentityProviderType.ExternalJWT, IdentityProviderType.Facebook, IdentityProviderType.Google, IdentityProviderType.HYPR, IdentityProviderType.LinkedIn, IdentityProviderType.Nintendo, IdentityProviderType.OpenIDConnect, IdentityProviderType.SAMLv2, 
        IdentityProviderType.SAMLv2IdPInitiated, IdentityProviderType.SonyPSN, IdentityProviderType.Steam, IdentityProviderType.Twitch, IdentityProviderType.Twitter, IdentityProviderType.Xbox });
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  @PreRenderMethod
  public void preRenderSetup() {
    if (this.results == null)
      this.results = Collections.emptyList(); 
    testCORSConfiguration();
    ApplicationResponse applicationResponse1 = this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.setTenantId(this.s.tenantId).retrieveApplications());
    if (applicationResponse1 != null && applicationResponse1.applications != null)
      this.applications.addAll(applicationResponse1.applications); 
    ApplicationResponse applicationResponse2 = this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.setTenantId(this.s.tenantId).retrieveInactiveApplications());
    if (applicationResponse2 != null && applicationResponse2.applications != null)
      this.applications.addAll(applicationResponse2.applications); 
    if (this.s.applicationId != null && this.applications.stream().noneMatch(paramApplication -> paramApplication.id.equals(this.s.applicationId)))
      this.s.applicationId = null; 
    this.applications.sort(Comparator.comparing(paramApplication -> paramApplication.name));
  }
  
  protected IdentityProviderSearchCriteria defaultSearchCriteria() {
    return new IdentityProviderSearchCriteria();
  }
  
  protected SearchResults<BaseIdentityProvider<?>> search() {
    UUID uUID = parseUUID(this.s.name);
    if (uUID != null) {
      ClientResponse<IdentityProviderResponse, Errors> clientResponse = this.client.retrieveIdentityProvider(uUID);
      if (clientResponse.wasSuccessful())
        return new SearchResults<>(List.of(((IdentityProviderResponse)clientResponse.getSuccessResponse()).identityProvider), 1L); 
      return null;
    } 
    IdentityProviderSearchResponse identityProviderSearchResponse = this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.searchIdentityProviders(new IdentityProviderSearchRequest(this.s)));
    return new SearchResults<>(identityProviderSearchResponse.identityProviders, identityProviderSearchResponse.total);
  }
  
  private void testCORSConfiguration() {
    this.results.stream()
      .filter(BaseIdentityProvider::inUse)
      .filter(paramBaseIdentityProvider -> paramBaseIdentityProvider instanceof RequiresCORSConfiguration)
      .forEach(paramBaseIdentityProvider -> this.requiredCORSConfiguration.put(paramBaseIdentityProvider.name, ((RequiresCORSConfiguration)paramBaseIdentityProvider).corsConfiguration()));
    CORSConfiguration cORSConfiguration = CORSUtils.mergeConfiguration(this.systemConfiguration.corsConfiguration, this.requiredCORSConfiguration.values());
    if (this.systemConfiguration.corsConfiguration.allowedOrigins.contains(URI.create("*"))) {
      cORSConfiguration.allowedOrigins.clear();
      cORSConfiguration.allowedOrigins.addAll(this.systemConfiguration.corsConfiguration.allowedOrigins);
    } 
    cORSConfiguration.debug = this.systemConfiguration.corsConfiguration.debug;
    if (cORSConfiguration.equals(this.systemConfiguration.corsConfiguration))
      this.requiredCORSConfiguration.clear(); 
  }
}
