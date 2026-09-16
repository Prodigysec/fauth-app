package io.fusionauth.app.action.ajax.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.inversoft.json.ToString;
import com.inversoft.rest.ClientResponse;
import com.inversoft.search.client.domain.BoolBuilder;
import com.inversoft.search.client.domain.request.JSON;
import com.inversoft.util.StringTools;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.SearchEngineType;
import io.fusionauth.api.service.search.ElasticsearchUserSearchEngine;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.domain.Pagination;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Group;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.GroupResponse;
import io.fusionauth.domain.api.user.SearchRequest;
import io.fusionauth.domain.api.user.SearchResponse;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.SortField;
import io.fusionauth.domain.search.UserSearchCriteria;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.annotation.Extension;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager", "user_support_viewer"})
public class SearchAction extends BaseAJAXAction {
  private final FusionAuthConfiguration configuration;
  
  private final ElasticsearchUserSearchEngine elasticsearchSearchEngine;
  
  private final String extension;
  
  private final ObjectMapper objectMapper;
  
  public UUID applicationId;
  
  public List<Application> applications;
  
  public int currentPage;
  
  public long firstResult;
  
  public String fullQuery;
  
  public UUID groupId;
  
  public List<Group> groups;
  
  public long lastResult;
  
  @FTLVariable
  public Integer maxWindow;
  
  @FTLVariable
  public String nextResults;
  
  public int numberOfPages;
  
  @JSONResponse
  public SearchResponse response;
  
  @FTLVariable
  public List<User> results;
  
  public String role;
  
  public List<String> roles;
  
  public AJAXUserSearchCriteria s = new AJAXUserSearchCriteria();
  
  @FTLVariable
  public boolean showFullQuery;
  
  public long total;
  
  public List<UUID> userId;
  
  @Inject
  public SearchAction(FusionAuthConfiguration paramFusionAuthConfiguration, ElasticsearchUserSearchEngine paramElasticsearchUserSearchEngine, FrontEndSupport paramFrontEndSupport, ObjectMapper paramObjectMapper, @Extension String paramString) {
    super(paramFrontEndSupport);
    this.configuration = paramFusionAuthConfiguration;
    this.elasticsearchSearchEngine = paramElasticsearchUserSearchEngine;
    this.objectMapper = paramObjectMapper;
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
      SearchResponse searchResponse = this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.searchUsersByIds(Collections.singletonList(this.s.id)));
      this.results = searchResponse.users;
      this.total = searchResponse.total;
    } else {
      String str = this.s.queryString;
      List<SortField> list = this.s.sortFields;
      JsonNode jsonNode = null;
      try {
        jsonNode = this.objectMapper.readTree(this.s.queryString);
      } catch (JsonProcessingException jsonProcessingException) {}
      BoolBuilder boolBuilder = new BoolBuilder();
      if (jsonNode != null && !jsonNode.isNull() && !jsonNode.isEmpty()) {
        boolBuilder.must(jsonNode);
        this.s.queryString = "";
      } 
      if (this.groupId != null)
        if (this.s.queryString.equals("")) {
          this.s.queryString = "memberships.groupId:" + String.valueOf(this.groupId);
        } else {
          this.s.queryString = this.s.queryString + " AND memberships.groupId:" + this.s.queryString;
        }  
      if (this.applicationId != null) {
        BoolBuilder boolBuilder1 = (new BoolBuilder()).mustMatch("registrations.applicationId", this.applicationId);
        if (this.role != null && !this.role.equals(""))
          boolBuilder1.mustMatch("registrations.roles", this.role.trim()); 
        boolBuilder.must(this.objectMapper.valueToTree(new JSON("nested", (new JSON())
                .add("path", "registrations")
                .add("query", boolBuilder1))));
      } 
      if (StringTools.isNotBlank(this.s.nextResults)) {
        this.s.queryString = null;
        this.s.sortFields = null;
        this.s.query = null;
      } else if (!boolBuilder.bool.isEmpty()) {
        if (this.s.queryString != null && !this.s.queryString.equals(""))
          boolBuilder.mustQueryString(this.s.queryString); 
        this.s.query = ToString.toJSONString(boolBuilder);
        if (this.tenantId != null)
          boolBuilder.mustQueryString("tenantId:" + String.valueOf(this.tenantId)); 
        this.fullQuery = ToString.toString(boolBuilder);
      } else {
        if (this.s.queryString.equals(""))
          this.s.queryString = "*"; 
        this.fullQuery = this.s.queryString;
        if (this.tenantId != null)
          if (this.fullQuery.equals("*")) {
            this.fullQuery = "tenantId:" + String.valueOf(this.tenantId);
          } else {
            this.fullQuery = "(" + this.fullQuery + ") AND tenantId:" + String.valueOf(this.tenantId);
          }  
      } 
      this.s.accurateTotal = true;
      SearchResponse searchResponse = this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.searchUsersByQuery((new SearchRequest(this.s)).with(())));
      this.nextResults = searchResponse.nextResults;
      this.results = searchResponse.users;
      if (this.configuration.searchEngineType() == SearchEngineType.elasticsearch)
        this.maxWindow = Integer.valueOf(this.elasticsearchSearchEngine.getMaxResultWindow()); 
      this.total = searchResponse.total;
      this.s.queryString = str;
      this.s.sortFields = list;
    } 
    calculatePagination(this.s.nextResults);
    if (this.extension.equals("json")) {
      this.response = new SearchResponse(new SearchResults<>(this.results, this.total));
      return "render-json";
    } 
    return "render";
  }
  
  @FormPrepareMethod
  public void prepareForm() {
    this.applications = (List<Application>)Objects.requireNonNullElseGet(((ApplicationResponse)this.delegate.execute(FusionAuthClient::retrieveApplications)).applications, Collections::emptyList);
    this.applications.sort(Comparator.comparing(paramApplication -> paramApplication.name));
    this.groups = ((GroupResponse)this.delegate.execute(FusionAuthClient::retrieveGroups)).groups;
    if (this.groups != null)
      this.groups.sort(Comparator.comparing(paramGroup -> paramGroup.name)); 
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
  
  public static class AJAXUserSearchCriteria extends UserSearchCriteria {
    public UUID id;
  }
}
