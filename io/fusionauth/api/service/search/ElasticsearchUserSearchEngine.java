package io.fusionauth.api.service.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.inversoft.search.ElasticSearchClient;
import com.inversoft.search.SearchEngineRequestFailedException;
import com.inversoft.search.client.domain.BulkIndexItem;
import com.inversoft.search.client.domain.BulkItem;
import com.inversoft.search.client.domain.BulkRequest;
import com.inversoft.search.client.domain.Query;
import com.inversoft.search.client.domain.request.JSON;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.service.cache.SearchIndexNameCache;
import io.fusionauth.api.service.search.client.domain.documents.IndexUser;
import io.fusionauth.domain.User;
import io.fusionauth.domain.search.Sort;
import io.fusionauth.domain.search.SortField;
import io.fusionauth.domain.search.UserSearchCriteria;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ElasticsearchUserSearchEngine extends BaseElasticsearchSearchEngine implements UserSearchEngine {
  private static final Pattern digitElasticWildcardOnlyRegex = Pattern.compile("[^\\d*?]");
  
  private static final Logger logger = LoggerFactory.getLogger(ElasticsearchUserSearchEngine.class);
  
  private static final Pattern phoneNumberRegex = Pattern.compile("phoneNumber:(?<!\")(\\S+)");
  
  private static final Pattern phoneNumberRegexQuoted = Pattern.compile("phoneNumber:\"(.*?)\"");
  
  public static String BREACHED_REQUIRING_ACTION_QUERY_STRING = "_exists_:breachedPasswordStatus AND NOT (breachedPasswordStatus:None)";
  
  protected static List<SortField> DEFAULT_SORT_FIELDS = List.of(new SortField("_score", Sort.desc), new SortField("fullName", Sort.desc), new SortField("email", Sort.desc), new SortField("id", Sort.desc));
  
  @Inject
  public ElasticsearchUserSearchEngine(FusionAuthConfiguration paramFusionAuthConfiguration, SearchIndexNameCache paramSearchIndexNameCache) {
    super(paramFusionAuthConfiguration, paramFusionAuthConfiguration.userSearchIndexName(), "user", paramSearchIndexNameCache);
  }
  
  protected ElasticsearchUserSearchEngine(FusionAuthConfiguration paramFusionAuthConfiguration, String paramString, SearchIndexNameCache paramSearchIndexNameCache) {
    super(paramFusionAuthConfiguration, paramString, "user", paramSearchIndexNameCache);
  }
  
  public String adjustFieldSpecificQueryString(String paramString) {
    Pattern pattern = phoneNumberRegexQuoted;
    while (true) {
      Matcher matcher = pattern.matcher(paramString);
      while (matcher.find()) {
        String str1 = matcher.group(1);
        String str2 = digitElasticWildcardOnlyRegex.matcher(str1).replaceAll("");
        if (!str1.startsWith("*"))
          str2 = "*" + str2; 
        String str3 = "phoneNumber:%s".formatted(new Object[] { str2 });
        paramString = paramString.replace(matcher.group(0), str3);
      } 
      if (pattern == phoneNumberRegex)
        break; 
      pattern = phoneNumberRegex;
    } 
    return paramString;
  }
  
  public void index(Collection<User> paramCollection) {
    if (paramCollection.isEmpty())
      return; 
    for (Iterator<String> iterator = getConcreteIndexNamesForWriting().iterator(); iterator.hasNext(); ) {
      Response response;
      String str = iterator.next();
      if (paramCollection.size() == 1) {
        User user = paramCollection.iterator().next();
        response = (new ElasticSearchClient()).withRestClient(this.tuple.client).withIndexName(str).withMethod("PUT").withEndpoint("/%s/_doc/" + user.id.toString()).request(new IndexUser(user));
      } else {
        BulkRequest bulkRequest = new BulkRequest();
        paramCollection.forEach(paramUser -> paramBulkRequest.add(new BulkIndexItem(new BulkItem(paramUser.id, paramString))).add(new IndexUser(paramUser)));
        response = (new ElasticSearchClient()).withRestClient(this.tuple.client).withIndexName(str).withMethod("POST").withEndpoint("/_bulk").bulkRequest(bulkRequest);
      } 
      int i = response.getStatusLine().getStatusCode();
      if (i != 201 && i != 200) {
        logger.error("Failed to complete an index request. Status Code [{}]", Integer.valueOf(i));
        throw new SearchEngineRequestFailedException(response);
      } 
    } 
  }
  
  public SearchEngine.SearchEngineStatus status() {
    SearchEngine.SearchEngineStatus searchEngineStatus = new SearchEngine.SearchEngineStatus();
    Response response = (new ElasticSearchClient()).withRestClient(this.tuple.client).withIndexName(this.indexName).withMethod("GET").withEndpoint("/_cluster/health").request();
    searchEngineStatus.cluster = ElasticSearchClient.deserializeToJsonNode(response);
    return searchEngineStatus;
  }
  
  protected JsonNode adjustQueryDSLNode(JsonNode paramJsonNode) {
    List list1 = paramJsonNode.findParents("phoneNumber");
    list1.forEach(paramJsonNode -> {
          if (paramJsonNode instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode)paramJsonNode;
            JsonNode jsonNode = objectNode.get("phoneNumber");
            if (jsonNode instanceof ObjectNode) {
              ObjectNode objectNode1 = (ObjectNode)jsonNode;
              String str1 = "query";
              JsonNode jsonNode1 = objectNode1.get(str1);
              if (jsonNode1 == null) {
                str1 = "value";
                jsonNode1 = objectNode1.get(str1);
              } 
              if (jsonNode1 == null)
                return; 
              String str2 = jsonNode1.asText();
              String str3 = digitElasticWildcardOnlyRegex.matcher(str2).replaceAll("");
              objectNode1.put(str1, str3);
            } 
          } 
        });
    List list2 = paramJsonNode.findParents("query_string");
    list2.forEach(paramJsonNode -> {
          JsonNode jsonNode = paramJsonNode.get("query_string");
          Optional optional = Optional.<JsonNode>ofNullable(jsonNode).map(());
          optional.ifPresent(());
        });
    return paramJsonNode;
  }
  
  protected String adjustWhenGeneral(String paramString) {
    if (paramString.contains("@")) {
      paramString = "email:" + paramString + " OR " + paramString;
    } else if (!paramString.equals("*")) {
      paramString = "*" + paramString + "*";
    } 
    return paramString;
  }
  
  protected Set<String> allowedSortFields() {
    return UserSearchCriteria.SortableFields;
  }
  
  protected List<SortField> getDefaultSortFields() {
    return DEFAULT_SORT_FIELDS;
  }
  
  protected Query prepareQuery(Query paramQuery, List<SortField> paramList) {
    paramQuery.sort = new ArrayList(paramList.size());
    for (SortField sortField : paramList) {
      JSON jSON = new JSON();
      jSON.add("order", (sortField.order == Sort.asc) ? "asc" : "desc");
      if (!sortField.name.equals("_score"))
        jSON.add("unmapped_type", "keyword")
          .add("missing", sortField.missing); 
      if (sortField.name.contains("registrations"))
        jSON.add("nested", new JSON("path", "registrations")); 
      paramQuery.sort.add(new JSON(sortField.name, jSON));
    } 
    if (paramList.stream().noneMatch(paramSortField -> "id".equals(paramSortField.name)))
      paramQuery.sort.add((new JSON()).add("id", (new JSON()).add("order", "desc"))); 
    return paramQuery;
  }
}
