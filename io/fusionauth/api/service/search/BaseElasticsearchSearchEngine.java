package io.fusionauth.api.service.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.inversoft.error.Errors;
import com.inversoft.search.ElasticRestClientHelper;
import com.inversoft.search.ElasticSearchClient;
import com.inversoft.search.SearchEngineRequestFailedException;
import com.inversoft.search.SearchEngineVersion;
import com.inversoft.search.client.CloseableTuple;
import com.inversoft.search.client.domain.BoolBuilder;
import com.inversoft.search.client.domain.BulkDeleteItem;
import com.inversoft.search.client.domain.BulkItem;
import com.inversoft.search.client.domain.BulkRequest;
import com.inversoft.search.client.domain.ElasticRequest;
import com.inversoft.search.client.domain.ErrorResponse;
import com.inversoft.search.client.domain.Hits;
import com.inversoft.search.client.domain.IndexRequest;
import com.inversoft.search.client.domain.JSONQuery;
import com.inversoft.search.client.domain.PointInTime;
import com.inversoft.search.client.domain.PointInTimeResponse;
import com.inversoft.search.client.domain.Query;
import com.inversoft.search.client.domain.QueryResponse;
import com.inversoft.search.client.domain.QueryString;
import com.inversoft.validator.Validator;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.SearchContinuationToken;
import io.fusionauth.api.domain.SearchEngineResult;
import io.fusionauth.api.service.cache.SearchIndexNameCache;
import io.fusionauth.api.service.user.InvalidSearchJSONException;
import io.fusionauth.domain.search.SortField;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.savantbuild.domain.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseElasticsearchSearchEngine implements Closeable {
  protected static final HttpEntity MATCH_ALL_QUERY = (HttpEntity)new NStringEntity("{\"query\":{\"match_all\":{}}}", ContentType.APPLICATION_JSON);
  
  private static final Logger logger = LoggerFactory.getLogger(BaseElasticsearchSearchEngine.class);
  
  protected final String baseConfigFileName;
  
  protected final FusionAuthConfiguration configuration;
  
  protected final String indexName;
  
  protected final CloseableTuple tuple;
  
  private final SearchIndexNameCache searchIndexNameCache;
  
  protected BaseElasticsearchSearchEngine(FusionAuthConfiguration paramFusionAuthConfiguration, String paramString1, String paramString2, SearchIndexNameCache paramSearchIndexNameCache) {
    this.baseConfigFileName = paramString2;
    this.configuration = paramFusionAuthConfiguration;
    this.tuple = ElasticRestClientHelper.buildClient(List.of((Object[])paramFusionAuthConfiguration.searchServers()), true, paramFusionAuthConfiguration.searchSniffer());
    this.indexName = paramString1;
    this.searchIndexNameCache = paramSearchIndexNameCache;
  }
  
  public void _setMaxResultWindow(int paramInt) {
    (new ElasticSearchClient()).withRestClient(this.tuple.client)
      .withIndexName(this.indexName)
      .withMethod("PUT")
      .withEndpoint("/%s/_settings")
      .request((ElasticRequest)new IndexRequest("max_result_window", "" + paramInt));
  }
  
  public void close() {
    if (this.tuple != null)
      try {
        this.tuple.close();
      } catch (IOException iOException) {
        logger.debug("Failed to close ElasticSearch RESTClient.");
      }  
  }
  
  public String createPointInTime() {
    if (!canDoPit())
      return null; 
    Response response = (new ElasticSearchClient()).withRestClient(this.tuple.client).withIndexName(this.indexName).withMethod("POST").withEndpoint(isOpenSearch() ? "/%s/_search/point_in_time?keep_alive=1m" : "/%s/_pit?keep_alive=1m").requestIgnoreCodes(new int[] { 400 });
    if (response.getStatusLine().getStatusCode() != 200) {
      logger.error("Failed to set a Point in Time on index [{}].", this.indexName);
      throw new SearchEngineRequestFailedException(response);
    } 
    return isElasticsearch() ? 
      ((PointInTimeResponse)ElasticSearchClient.deserialize(response, PointInTimeResponse.class)).id : 
      ((QueryResponse)ElasticSearchClient.deserialize(response, QueryResponse.class)).pitId;
  }
  
  public void delete(UUID paramUUID) {
    (new ElasticSearchClient()).withRestClient(this.tuple.client)
      .withIndexName(this.indexName)
      .withMethod("DELETE")
      .withEndpoint("/%s/_doc/" + String.valueOf(paramUUID))
      .requestIgnoreCodes(new int[] { 404 });
  }
  
  public void deleteAll() {
    (new ElasticSearchClient()).withRestClient(this.tuple.client)
      .withIndexName(this.indexName)
      .withMethod("POST")
      .withEndpoint("/%s/_delete_by_query?conflicts=proceed")
      .request(MATCH_ALL_QUERY);
  }
  
  public void deleteByIds(List<UUID> paramList) {
    if (paramList.isEmpty())
      return; 
    (new ElasticSearchClient()).withRestClient(this.tuple.client)
      .withIndexName(this.indexName)
      .withMethod("POST")
      .withEndpoint("/_bulk")
      .bulkRequest(new BulkRequest((List)paramList.stream().map(paramUUID -> new BulkDeleteItem(new BulkItem(paramUUID, this.indexName))).collect(Collectors.toList())));
  }
  
  public void deleteIndexAndRecreate() {
    ElasticSearchClient.resetVersion();
    deleteIndex(this.indexName + "_a");
    deleteIndex(this.indexName + "_b");
    (new ElasticSearchClient()).withRestClient(this.tuple.client)
      .withIndexName(this.indexName)
      .withMethod("DELETE")
      .withEndpoint("/%s")
      .debug("Delete index requested for index [%s]")

      
      .requestIgnoreCodes(new int[] { 400, 404 });
    String str = this.indexName + "_a";
    (new ElasticSearchClient()).withRestClient(this.tuple.client)
      .withIndexName(str)
      .initializeIndex(this.baseConfigFileName);
    Response response = (new ElasticSearchClient()).withRestClient(this.tuple.client).withMethod("PUT").withEndpoint("/" + str + "/_alias/" + this.indexName).request();
    int i = response.getStatusLine().getStatusCode();
    if (i != 201 && i != 200) {
      logger.error("Failed to create an alias [{}] for index [{}]. Status Code [{}]", new Object[] { this.indexName, str, Integer.valueOf(i) });
      throw new SearchEngineRequestFailedException(response);
    } 
  }
  
  public void flush() {
    (new ElasticSearchClient()).withRestClient(this.tuple.client)
      .withIndexName(this.indexName)
      .withMethod("POST")
      .withEndpoint("/%s/_flush")
      .debug("Flush requested for index [%s]")
      .request();
  }
  
  public String getIndexRefreshInterval() {
    String str = getConcreteIndexNamesForWriting().get(0);
    JsonNode jsonNode = getSettings();
    return jsonNode.at("/" + str + "/settings/index/refresh_interval").asText("1s");
  }
  
  public void setIndexRefreshInterval(String paramString) {
    (new ElasticSearchClient()).withRestClient(this.tuple.client)
      .withIndexName(this.indexName)
      .withMethod("PUT")
      .withEndpoint("/%s/_settings")
      .request((ElasticRequest)new IndexRequest("refresh_interval", paramString));
  }
  
  public int getMaxResultWindow() {
    String str = getConcreteIndexNamesForWriting().get(0);
    JsonNode jsonNode = getSettings();
    return jsonNode.at("/" + str + "/settings/index/max_result_window").asInt(10000);
  }
  
  public void refresh() {
    (new ElasticSearchClient()).withRestClient(this.tuple.client)
      .withIndexName(this.indexName)
      .withMethod("POST")
      .withEndpoint("/%s/_refresh")
      .debug("Refresh requested for index [%s]")
      .request();
  }
  
  public WriteIndicies retrieveWriteIndicies() {
    WriteIndicies writeIndicies = new WriteIndicies();
    writeIndicies.alias = this.indexName;
    try {
      for (String str : List.of(this.indexName + "_a", this.indexName + "_b")) {
        Response response = (new ElasticSearchClient()).withRestClient(this.tuple.client).withMethod("GET").withEndpoint("/" + str).requestIgnoreCodes(new int[] { 404 });
        if (response.getStatusLine().getStatusCode() == 200)
          writeIndicies.indices.add(str); 
      } 
    } catch (Exception exception) {
      writeIndicies.indices.clear();
      writeIndicies.indices.add(this.indexName);
    } 
    return writeIndicies;
  }
  
  public SearchEngineResult searchByQuery(UUID paramUUID, String paramString1, int paramInt1, int paramInt2, List<SortField> paramList, boolean paramBoolean, List<String> paramList1, String paramString2) {
    BoolBuilder boolBuilder = new BoolBuilder();
    if (!paramString1.isEmpty())
      try {
        boolBuilder.must(ElasticSearchClient.objectMapper.readerFor(JsonNode.class).readTree(paramString1));
      } catch (IOException iOException) {
        throw new InvalidSearchJSONException(iOException);
      }  
    if (paramUUID != null)
      boolBuilder.mustQueryString("tenantId:" + String.valueOf(paramUUID)); 
    JsonNode jsonNode = (JsonNode)ElasticSearchClient.objectMapper.convertValue(boolBuilder, JsonNode.class);
    jsonNode = adjustQueryDSLNode(jsonNode);
    JSONQuery jSONQuery = new JSONQuery(jsonNode, Integer.valueOf(paramInt1), Integer.valueOf(paramInt2));
    jSONQuery.searchAfter = paramList1;
    if (ElasticSearchClient.MAJOR_VERSION != SearchEngineVersion.ELASTICSEARCH_6_X)
      jSONQuery.track_total_hits = paramBoolean ? Boolean.valueOf(true) : Integer.valueOf(this.configuration.searchEngineDefaultMaxHitCount()); 
    return executeSearch((Query)jSONQuery, paramList, paramString2, (new SearchContinuationToken()).with(paramSearchContinuationToken -> paramSearchContinuationToken.q = paramString));
  }
  
  public SearchEngineResult searchByQueryString(UUID paramUUID, String paramString1, int paramInt1, int paramInt2, List<SortField> paramList, boolean paramBoolean, List<String> paramList1, String paramString2) {
    String str = paramString1;
    paramString1 = paramString1.trim();
    if (!paramString1.contains(":") && !paramString1.contains(" ")) {
      paramString1 = adjustWhenGeneral(paramString1);
    } else if (paramString1.contains(":")) {
      paramString1 = adjustFieldSpecificQueryString(paramString1);
    } 
    if (paramUUID != null)
      if (paramString1.equals("")) {
        paramString1 = paramString1 + "tenantId:" + paramString1;
      } else {
        paramString1 = "(" + paramString1 + ") AND tenantId:" + String.valueOf(paramUUID);
      }  
    Query query = new Query(new QueryString(paramString1), Integer.valueOf(paramInt1), Integer.valueOf(paramInt2));
    query.searchAfter = paramList1;
    if (ElasticSearchClient.MAJOR_VERSION != SearchEngineVersion.ELASTICSEARCH_6_X)
      query.track_total_hits = paramBoolean ? Boolean.valueOf(true) : Integer.valueOf(this.configuration.searchEngineDefaultMaxHitCount()); 
    return executeSearch(query, paramList, paramString2, (new SearchContinuationToken()).with(paramSearchContinuationToken -> paramSearchContinuationToken.qs = paramString));
  }
  
  public Errors validate(UUID paramUUID, String paramString1, String paramString2, List<SortField> paramList, int paramInt1, int paramInt2) {
    Set<String> set = allowedSortFields();
    Validator validator = (new Validator()).ifTrue((paramUUID != null && paramString2 != null), paramValidator -> paramValidator.ensure(!paramString.contains("tenantId"), "queryString", "[invalidTenant]", new Object[0])).ifTrue((paramUUID != null && paramString1 != null), paramValidator -> paramValidator.ensure(!paramString.contains("tenantId"), "query", "[invalidTenant]", new Object[0])).ifFalse(getDefaultSortFields().equals(paramList), paramValidator -> paramValidator.forEach(paramList, ()));
    int i = paramInt1 + paramInt2;
    int j = getMaxResultWindow();
    Errors errors = validator.ensure((i <= j), "numberOfResults", "[invalid]", new Object[] { Integer.valueOf(j) }).done();
    if (errors.empty()) {
      if (paramString1 != null)
        errors.add(validateJSONQuery(paramString1)); 
      if (paramString2 != null)
        errors.add(validateQueryString(paramString2)); 
    } 
    return errors;
  }
  
  protected String createIndex() {
    String str = this.indexName + "_a";
    Response response = (new ElasticSearchClient()).withRestClient(this.tuple.client).withMethod("GET").withEndpoint("/_alias/" + this.indexName).requestIgnoreCodes(new int[] { 404 });
    if (response.getStatusLine().getStatusCode() == 200) {
      JsonNode jsonNode = ElasticSearchClient.deserializeToJsonNode(response);
      if (jsonNode.isObject()) {
        String str1 = jsonNode.fieldNames().next();
        if (str1.equals(str))
          str = this.indexName + "_b"; 
      } 
    } 
    (new ElasticSearchClient()).withRestClient(this.tuple.client)
      .withIndexName(str)
      .withMethod("DELETE")
      .withEndpoint("/%s")
      .debug("Delete index requested for index [%s]")
      .requestIgnoreCodes(new int[] { 404 });
    (new ElasticSearchClient()).withRestClient(this.tuple.client)
      .withIndexName(str)
      .debug("Create and initialize [%s]")
      .initializeIndex(this.baseConfigFileName);
    return str;
  }
  
  protected void deleteIndex(String paramString) {
    ElasticSearchClient elasticSearchClient = new ElasticSearchClient();
    try {
      Response response = elasticSearchClient.withRestClient(this.tuple.client).withIndexName(paramString).withMethod("DELETE").withEndpoint("/%s").debug("Delete index requested for index [%s]").request();
      if (response.getStatusLine().getStatusCode() == 404)
        elasticSearchClient.debug("  Index [%s] not found, ignoring delete index request."); 
    } catch (SearchEngineRequestFailedException searchEngineRequestFailedException) {
      if (searchEngineRequestFailedException.response.getStatusLine().getStatusCode() == 404) {
        elasticSearchClient.debug("  Index [%s] not found, ignoring delete index request.");
      } else {
        throw searchEngineRequestFailedException;
      } 
    } 
  }
  
  protected SearchEngineResult executeSearch(Query paramQuery, List<SortField> paramList, String paramString, SearchContinuationToken paramSearchContinuationToken) {
    List<SortField> list = (paramList == null || paramList.isEmpty()) ? getDefaultSortFields() : paramList;
    paramQuery = prepareQuery(paramQuery, list);
    ElasticSearchClient elasticSearchClient = (new ElasticSearchClient()).withRestClient(this.tuple.client).withIndexName(this.indexName).withMethod("GET");
    if (paramString == null) {
      elasticSearchClient.withEndpoint("/%s/_search");
    } else {
      elasticSearchClient.withEndpoint("/_search");
      paramQuery.pointInTime = new PointInTime(paramString, "1m");
    } 
    ElasticSearchClient.debugJSON(paramQuery);
    Response response = elasticSearchClient.request((ElasticRequest)paramQuery);
    if (response.getStatusLine().getStatusCode() != 200) {
      logger.error("Failed to query ElasticSearch");
      throw new SearchEngineRequestFailedException(response);
    } 
    QueryResponse queryResponse = (QueryResponse)ElasticSearchClient.deserialize(response, QueryResponse.class);
    List list1 = queryResponse.hits.hits;
    List<UUID> list2 = (List)list1.stream().map(paramHit -> UUID.fromString(paramHit._id)).collect(Collectors.toList());
    boolean bool = "eq".equals(queryResponse.hits.relation);
    paramSearchContinuationToken
      
      .ls = (list1.size() > 0) ? ((Hits.Hit)queryResponse.hits.hits.get(queryResponse.hits.hits.size() - 1)).sort : null;
    paramSearchContinuationToken.sf = list;
    paramSearchContinuationToken.pit = queryResponse.pitId;
    String str = null;
    if (queryResponse.hits.total.longValue() > 0L)
      try {
        byte[] arrayOfByte = ElasticSearchClient.objectMapper.writeValueAsBytes(paramSearchContinuationToken);
        str = Base64.getUrlEncoder().withoutPadding().encodeToString(arrayOfByte);
      } catch (JsonProcessingException jsonProcessingException) {
        logger.error("Failed to serializing the SearchContinuationToken. The search request will not fail. However, the API response will not contain the nextResults token.", (Throwable)jsonProcessingException);
      }  
    return new SearchEngineResult(list2, bool, queryResponse.hits.total.longValue(), str);
  }
  
  protected List<String> getConcreteIndexNamesForWriting() {
    List<String> list = (this.searchIndexNameCache != null) ? (List)this.searchIndexNameCache.get(this.indexName) : List.of(this.indexName);
    if (list.isEmpty())
      list = List.of(this.indexName); 
    return list;
  }
  
  protected boolean isElasticsearch() {
    return !isOpenSearch();
  }
  
  protected boolean isOpenSearch() {
    return "opensearch".equals(ElasticSearchClient.DISTRIBUTION);
  }
  
  protected void updateAliasDeleteOldIndex() {
    String str = (this instanceof ElasticsearchUserSearchEngine) ? this.configuration.userSearchIndexName() : this.configuration.entitySearchIndexName();
    Response response = (new ElasticSearchClient()).withRestClient(this.tuple.client).withMethod("HEAD").withEndpoint("/_alias/" + str).request();
    if (response.getStatusLine().getStatusCode() == 404)
      (new ElasticSearchClient()).withRestClient(this.tuple.client)
        .withIndexName(str)
        .withMethod("DELETE")
        .withEndpoint("/%s")
        .debug("Delete index requested for index [%s]")
        .requestIgnoreCodes(new int[] { 404 }); 
    response = (new ElasticSearchClient()).withRestClient(this.tuple.client).withMethod("PUT").withEndpoint("/" + this.indexName + "/_alias/" + str).debug("Add alias [" + str + "] to [" + this.indexName + "]").request();
    int i = response.getStatusLine().getStatusCode();
    if (i != 201 && i != 200) {
      logger.error("Failed to create an alias [{}] for index [{}]. Status Code [{}]", new Object[] { str, this.indexName, Integer.valueOf(i) });
      throw new SearchEngineRequestFailedException(response);
    } 
    deleteIndex(this.indexName.endsWith("_a") ? 
        this.indexName.replace("_a", "_b") : 
        this.indexName.replace("_b", "_a"));
  }
  
  protected Errors validateJSONQuery(String paramString) {
    Errors errors = new Errors();
    try {
      errors.add(validateQuery("query", (Query)new JSONQuery(ElasticSearchClient.objectMapper.readerFor(JsonNode.class).readTree(paramString))));
    } catch (IOException iOException) {
      throw new InvalidSearchJSONException(iOException);
    } 
    return errors;
  }
  
  protected Errors validateQuery(String paramString, Query paramQuery) {
    Errors errors = new Errors();
    Response response = (new ElasticSearchClient()).withRestClient(this.tuple.client).withIndexName(this.indexName).withMethod("GET").withEndpoint("/%s/_validate/query?explain=true").request((ElasticRequest)paramQuery);
    int i = response.getStatusLine().getStatusCode();
    if (i == 200) {
      JsonNode jsonNode = ElasticSearchClient.deserializeToJsonNode(response);
      if (!jsonNode.get("valid").asBoolean(false)) {
        String str = jsonNode.has("explanations") ? jsonNode.get("explanations").toString() : "No explanation available";
        errors.addFieldError(paramString, "[invalid]" + paramString, null, new Object[] { str });
      } 
      return errors;
    } 
    if (i == 400) {
      if (response.getEntity().getContentLength() > 0L) {
        ErrorResponse errorResponse = (ErrorResponse)ElasticSearchClient.deserialize(response, ErrorResponse.class);
        logger.error("Query Validation Failed.\n" + String.valueOf(errorResponse));
      } 
      throw new SearchEngineRequestFailedException(response);
    } 
    throw new SearchEngineRequestFailedException(response);
  }
  
  protected Errors validateQueryString(String paramString) {
    return validateQuery("queryString", new Query(new QueryString(paramString)));
  }
  
  private boolean canDoPit() {
    Version version = new Version(ElasticSearchClient.VERSION);
    return isElasticsearch() ? (
      (version.compareTo(new Version("7.10.0")) > 0)) : (
      (version.compareTo(new Version("2.4.0")) > 0));
  }
  
  private JsonNode getSettings() {
    Response response = (new ElasticSearchClient()).withRestClient(this.tuple.client).withIndexName(this.indexName).withMethod("GET").withEndpoint("/%s/_settings").request();
    return ElasticSearchClient.deserializeToJsonNode(response);
  }
  
  protected abstract String adjustFieldSpecificQueryString(String paramString);
  
  protected abstract JsonNode adjustQueryDSLNode(JsonNode paramJsonNode);
  
  protected abstract String adjustWhenGeneral(String paramString);
  
  protected abstract Set<String> allowedSortFields();
  
  protected abstract List<SortField> getDefaultSortFields();
  
  protected abstract Query prepareQuery(Query paramQuery, List<SortField> paramList);
  
  public static class WriteIndicies {
    public String alias;
    
    public List<String> indices = new ArrayList<>(2);
  }
}
