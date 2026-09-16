package io.fusionauth.app.action.admin.theme;

import com.google.inject.Inject;
import com.inversoft.cache.Cache;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.CachedTheme;
import io.fusionauth.app.action.admin.BaseSearchAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Theme;
import io.fusionauth.domain.api.ThemeResponse;
import io.fusionauth.domain.api.ThemeSearchRequest;
import io.fusionauth.domain.api.ThemeSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.ThemeSearchCriteria;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "theme_manager"})
public class IndexAction extends BaseSearchAction<Theme, ThemeSearchCriteria> {
  public Set<String> defaultMessageKeys;
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport, Cache<UUID, CachedTheme> paramCache) {
    super(paramFrontEndSupport);
    CachedTheme cachedTheme = (CachedTheme)paramCache.get(Theme.FUSIONAUTH_THEME_ID);
    this.defaultMessageKeys = cachedTheme.defaultProperties.stringPropertyNames();
  }
  
  protected ThemeSearchCriteria defaultSearchCriteria() {
    return new ThemeSearchCriteria();
  }
  
  protected SearchResults<Theme> search() {
    UUID uUID = parseUUID(this.s.name);
    if (uUID != null) {
      ClientResponse<ThemeResponse, Errors> clientResponse = this.superClient.retrieveTheme(uUID);
      if (clientResponse.wasSuccessful())
        return new SearchResults<>(List.of(((ThemeResponse)clientResponse.getSuccessResponse()).theme), 1L); 
      return null;
    } 
    ThemeSearchResponse themeSearchResponse = superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.searchThemes(new ThemeSearchRequest(this.s)));
    return new SearchResults<>(themeSearchResponse.themes, themeSearchResponse.total);
  }
}
