package io.fusionauth.api.service.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.inversoft.search.ElasticSearchClient;
import com.inversoft.search.SearchEngineRequestFailedException;
import com.inversoft.search.client.domain.BulkIndexItem;
import com.inversoft.search.client.domain.BulkItem;
import com.inversoft.search.client.domain.BulkRequest;
import com.inversoft.search.client.domain.ElasticRequest;
import com.inversoft.search.client.domain.Query;
import com.inversoft.search.client.domain.request.JSON;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.service.cache.SearchIndexNameCache;
import io.fusionauth.api.service.search.client.domain.documents.IndexEntity;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.search.EntitySearchCriteria;
import io.fusionauth.domain.search.Sort;
import io.fusionauth.domain.search.SortField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ElasticsearchEntitySearchEngine extends BaseElasticsearchSearchEngine implements EntitySearchEngine {
  private static final Map<String, String> SORT_FIELD_MAPPINGS = Map.of("name", "name.raw");
  
  private static final Logger logger = LoggerFactory.getLogger(ElasticsearchEntitySearchEngine.class);
  
  protected static List<SortField> DEFAULT_SORT_FIELDS = List.of(new SortField("_score", Sort.desc), new SortField("name", Sort.asc), new SortField("id", Sort.asc));
  
  @Inject
  public ElasticsearchEntitySearchEngine(FusionAuthConfiguration paramFusionAuthConfiguration, SearchIndexNameCache paramSearchIndexNameCache) {
    super(paramFusionAuthConfiguration, paramFusionAuthConfiguration.entitySearchIndexName(), "entity", paramSearchIndexNameCache);
  }
  
  protected ElasticsearchEntitySearchEngine(FusionAuthConfiguration paramFusionAuthConfiguration, String paramString, SearchIndexNameCache paramSearchIndexNameCache) {
    super(paramFusionAuthConfiguration, paramString, "entity", paramSearchIndexNameCache);
  }
  
  public void deleteByType(UUID paramUUID) {
    Query query = new Query();
    query.query = Map.of("match", Map.of("typeId", paramUUID));
    (new ElasticSearchClient()).withRestClient(this.tuple.client)
      .withIndexName(this.indexName)
      .withMethod("POST")
      .withEndpoint("/%s/_delete_by_query")
      .request((ElasticRequest)query);
  }
  
  public void index(Collection<Entity> paramCollection) {
    if (paramCollection.isEmpty())
      return; 
    for (Iterator<String> iterator = getConcreteIndexNamesForWriting().iterator(); iterator.hasNext(); ) {
      Response response;
      String str = iterator.next();
      if (paramCollection.size() == 1) {
        Entity entity = paramCollection.iterator().next();
        response = (new ElasticSearchClient()).withRestClient(this.tuple.client).withIndexName(str).withMethod("PUT").withEndpoint("/%s/_doc/" + entity.id.toString()).request(new IndexEntity(entity));
      } else {
        BulkRequest bulkRequest = new BulkRequest();
        paramCollection.forEach(paramEntity -> paramBulkRequest.add(new BulkIndexItem(new BulkItem(paramEntity.id, paramString))).add(new IndexEntity(paramEntity)));
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
    return null;
  }
  
  protected String adjustFieldSpecificQueryString(String paramString) {
    return paramString;
  }
  
  protected JsonNode adjustQueryDSLNode(JsonNode paramJsonNode) {
    return paramJsonNode;
  }
  
  protected String adjustWhenGeneral(String paramString) {
    if (!paramString.equals("*"))
      paramString = "*" + paramString + "*"; 
    return paramString;
  }
  
  protected Set<String> allowedSortFields() {
    return EntitySearchCriteria.SortableFields;
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
      paramQuery.sort.add(new JSON(mapName(sortField.name), jSON));
    } 
    if (paramList.stream().noneMatch(paramSortField -> "id".equals(paramSortField.name)))
      paramQuery.sort.add((new JSON()).add("id", (new JSON()).add("order", "desc"))); 
    return paramQuery;
  }
  
  private String mapName(String paramString) {
    String str = SORT_FIELD_MAPPINGS.get(paramString);
    return (str != null) ? str : paramString;
  }
}
