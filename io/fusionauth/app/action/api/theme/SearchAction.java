package io.fusionauth.app.action.api.theme;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.system.ThemeService;
import io.fusionauth.app.action.api.BaseSearchAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.ThemeSearchRequest;
import io.fusionauth.domain.api.ThemeSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.ThemeSearchCriteria;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class SearchAction extends BaseSearchAPIAction<ThemeSearchCriteria> {
  @JSONRequest
  public final ThemeSearchRequest request = new ThemeSearchRequest();
  
  private final ThemeService themeService;
  
  @JSONResponse
  public ThemeSearchResponse response;
  
  @Inject
  public SearchAction(AuthenticationKeyCache paramAuthenticationKeyCache, FrontEndSupport paramFrontEndSupport, ThemeService paramThemeService) {
    super(paramAuthenticationKeyCache, paramFrontEndSupport);
    this.themeService = paramThemeService;
  }
  
  public String name() {
    return (criteria()).name;
  }
  
  public void setName(String paramString) {
    (criteria()).name = paramString;
  }
  
  protected ThemeSearchCriteria criteria() {
    return this.request.search;
  }
  
  protected String search() {
    this.response = new ThemeSearchResponse(this.themeService.search(this.request.search));
    return "render";
  }
}
