package io.fusionauth.api.migration.guice;

import com.fasterxml.jackson.databind.JsonNode;
import com.inversoft.migration.Migration;
import com.inversoft.search.ElasticRestClientHelper;
import com.inversoft.search.ElasticSearchClient;
import com.inversoft.search.SearchEngineRequestFailedException;
import com.inversoft.search.client.CloseableTuple;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.SearchEngineType;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseElasticMigration implements Migration {
  private static final Logger logger = LoggerFactory.getLogger(BaseElasticMigration.class);
  
  private final FusionAuthConfiguration configuration;
  
  protected BaseElasticMigration(FusionAuthConfiguration paramFusionAuthConfiguration) {
    this.configuration = paramFusionAuthConfiguration;
  }
  
  public void cleanup() throws Exception {}
  
  public void runOnce() throws Exception {
    if (this.configuration.searchEngineType() == SearchEngineType.database) {
      logger.info("Configured search engine is database, skipping migration");
      return;
    } 
    CloseableTuple closeableTuple = ElasticRestClientHelper.buildClient(List.of((Object[])this.configuration.searchServers()), true, this.configuration
        
        .searchSniffer());
    try {
      ElasticSearchClient elasticSearchClient = (new ElasticSearchClient()).withRestClient(closeableTuple.client).withIndexName(this.configuration.userSearchIndexName());
      Response response = elasticSearchClient.withEndpoint("/%s/_mapping/field/" + fieldName()).withMethod("GET").request();
      JsonNode jsonNode = ElasticSearchClient.deserializeToJsonNode(response);
      if (jsonNode.findParent(fieldName()) != null) {
        logger.info("Mapping already exists for [{}], [{}], skipping migration", fieldName(), jsonNode);
        if (closeableTuple != null)
          closeableTuple.close(); 
        return;
      } 
      logger.info("Now updating the mapping for the [{}] field", fieldName());
      NStringEntity nStringEntity = new NStringEntity(mappingJson(), ContentType.APPLICATION_JSON);
      try {
        elasticSearchClient.withEndpoint("/%s/_mapping").withMethod("POST").request((HttpEntity)nStringEntity);
        logger.info("Mapping update complete for [{}]", fieldName());
      } catch (SearchEngineRequestFailedException searchEngineRequestFailedException) {
        logger.error("Unable to update the mapping for the [{}] field, but failing gracefully. Status code: [{}], message: [{}]", new Object[] { fieldName(), Integer.valueOf(searchEngineRequestFailedException.statusCode), searchEngineRequestFailedException.getMessage() });
      } 
      if (closeableTuple != null)
        closeableTuple.close(); 
    } catch (Throwable throwable) {
      if (closeableTuple != null)
        try {
          closeableTuple.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  protected abstract String fieldName();
  
  protected abstract String mappingJson();
}
