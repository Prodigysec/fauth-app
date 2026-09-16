package io.fusionauth.app.action.ajax.entity;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.StringTools;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.SearchEngineType;
import io.fusionauth.api.service.search.ElasticsearchEntitySearchEngine;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.domain.Pagination;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.search.SearchFrontendService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.EntityType;
import io.fusionauth.domain.api.EntitySearchRequest;
import io.fusionauth.domain.api.EntitySearchResponse;
import io.fusionauth.domain.api.EntityTypeResponse;
import io.fusionauth.domain.search.SortField;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.annotation.Extension;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "entity_manager"})
public class SearchAction extends BaseAJAXAction {
  private final FusionAuthConfiguration configuration;
  
  private final ElasticsearchEntitySearchEngine elasticsearchSearchEngine;
  
  private final String extension;
  
  private final SearchFrontendService searchFrontendService;
  
  public int currentPage;
  
  public List<EntityType> entityTypes;
  
  public long firstResult;
  
  public String fullQuery;
  
  public long lastResult;
  
  @FTLVariable
  public Integer maxWindow;
  
  @FTLVariable
  public String nextResults;
  
  public int numberOfPages;
  
  @JSONResponse
  public EntitySearchResponse response;
  
  public List<Entity> results;
  
  public SearchFrontendService.FrontendEntitySearchCriteria s = new SearchFrontendService.FrontendEntitySearchCriteria();
  
  @FTLVariable
  public boolean showFullQuery;
  
  public long total;
  
  public UUID typeId;
  
  @Inject
  public SearchAction(FusionAuthConfiguration paramFusionAuthConfiguration, ElasticsearchEntitySearchEngine paramElasticsearchEntitySearchEngine, FrontEndSupport paramFrontEndSupport, SearchFrontendService paramSearchFrontendService, @Extension String paramString) {
    super(paramFrontEndSupport);
    this.configuration = paramFusionAuthConfiguration;
    this.elasticsearchSearchEngine = paramElasticsearchEntitySearchEngine;
    this.searchFrontendService = paramSearchFrontendService;
    this.extension = paramString;
  }
  
  public String get() {
    if (this.s.queryString == null) {
      this.s.queryString = "";
    } else {
      this.s.queryString = this.s.queryString.trim();
    } 
    this.s.id = parseUUID(this.s.queryString);
    if (this.s.id != null) {
      EntitySearchResponse entitySearchResponse = this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.searchEntitiesByIds(Collections.singletonList(this.s.id)));
      this.results = entitySearchResponse.entities;
      this.total = entitySearchResponse.total;
    } else {
      String str = this.searchFrontendService.buildEntitySearchQuery(this.s);
      List<SortField> list = this.s.sortFields;
      if (StringTools.isNotBlank(this.s.nextResults)) {
        this.s.queryString = null;
        this.s.sortFields = null;
      } 
      this.s.accurateTotal = true;
      EntitySearchResponse entitySearchResponse = this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.searchEntities(new EntitySearchRequest(this.s)));
      this.nextResults = entitySearchResponse.nextResults;
      this.results = entitySearchResponse.entities;
      if (this.configuration.searchEngineType() == SearchEngineType.elasticsearch)
        this.maxWindow = Integer.valueOf(this.elasticsearchSearchEngine.getMaxResultWindow()); 
      this.total = entitySearchResponse.total;
      this.fullQuery = this.s.queryString;
      this.s.queryString = str;
      this.s.sortFields = list;
    } 
    calculatePagination(this.s.nextResults);
    if (this.extension.equals("json")) {
      this.response = new EntitySearchResponse(this.results, this.total);
      return "render-json";
    } 
    return "render";
  }
  
  @FormPrepareMethod
  public void prepareForm() {
    this.entityTypes = (List<EntityType>)Objects.requireNonNullElseGet(((EntityTypeResponse)superDelegate().execute(FusionAuthClient::retrieveEntityTypes)).entityTypes, Collections::emptyList);
    this.entityTypes.sort(Comparator.comparing(paramEntityType -> paramEntityType.name));
  }
  
  protected void calculatePagination(String paramString) {
    if (StringTools.isBlank(paramString)) {
    
    } else {
    
    } 
    Pagination pagination = new Pagination(this.currentPage * this.s.numberOfResults, this.s.numberOfResults, this.results.size(), this.total);
    this.currentPage = pagination.currentPage;
    this.numberOfPages = pagination.numberOfPages;
    this.firstResult = pagination.firstResult;
    this.lastResult = pagination.lastResult;
  }
}
