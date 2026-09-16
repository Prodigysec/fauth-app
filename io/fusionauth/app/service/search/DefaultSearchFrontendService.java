package io.fusionauth.app.service.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.inversoft.json.ToString;
import com.inversoft.search.client.domain.BoolBuilder;

public class DefaultSearchFrontendService implements SearchFrontendService {
  private final ObjectMapper objectMapper;
  
  @Inject
  public DefaultSearchFrontendService(ObjectMapper paramObjectMapper) {
    this.objectMapper = paramObjectMapper;
  }
  
  public String buildEntitySearchQuery(SearchFrontendService.FrontendEntitySearchCriteria paramFrontendEntitySearchCriteria) {
    if (paramFrontendEntitySearchCriteria.queryString == null) {
      paramFrontendEntitySearchCriteria.queryString = "";
    } else {
      paramFrontendEntitySearchCriteria.queryString = paramFrontendEntitySearchCriteria.queryString.trim();
    } 
    String str = paramFrontendEntitySearchCriteria.queryString;
    JsonNode jsonNode = null;
    try {
      jsonNode = this.objectMapper.readTree(paramFrontendEntitySearchCriteria.queryString);
    } catch (JsonProcessingException jsonProcessingException) {}
    BoolBuilder boolBuilder = new BoolBuilder();
    if (jsonNode != null && !jsonNode.isNull() && !jsonNode.isEmpty()) {
      boolBuilder.must(jsonNode);
      paramFrontendEntitySearchCriteria.queryString = "";
    } 
    if (paramFrontendEntitySearchCriteria.typeId != null)
      if (paramFrontendEntitySearchCriteria.queryString.equals("")) {
        paramFrontendEntitySearchCriteria.queryString = "typeId:" + paramFrontendEntitySearchCriteria.typeId.toString();
      } else {
        paramFrontendEntitySearchCriteria.queryString = paramFrontendEntitySearchCriteria.queryString + " AND typeId:" + paramFrontendEntitySearchCriteria.queryString;
      }  
    if (!boolBuilder.bool.isEmpty()) {
      if (paramFrontendEntitySearchCriteria.queryString != null && !paramFrontendEntitySearchCriteria.queryString.equals(""))
        boolBuilder.mustQueryString(paramFrontendEntitySearchCriteria.queryString); 
      paramFrontendEntitySearchCriteria.query = ToString.toJSONString(boolBuilder);
      if (paramFrontendEntitySearchCriteria.tenantId != null)
        boolBuilder.mustQueryString("tenantId:" + paramFrontendEntitySearchCriteria.tenantId.toString()); 
      paramFrontendEntitySearchCriteria.queryString = ToString.toString(boolBuilder);
    } else {
      if (paramFrontendEntitySearchCriteria.queryString.equals(""))
        paramFrontendEntitySearchCriteria.queryString = "*"; 
      if (paramFrontendEntitySearchCriteria.tenantId != null)
        if (paramFrontendEntitySearchCriteria.queryString.equals("*")) {
          paramFrontendEntitySearchCriteria.queryString = "tenantId:" + paramFrontendEntitySearchCriteria.tenantId.toString();
        } else {
          paramFrontendEntitySearchCriteria.queryString = "(" + paramFrontendEntitySearchCriteria.queryString + ") AND tenantId:" + paramFrontendEntitySearchCriteria.tenantId.toString();
        }  
    } 
    return str;
  }
}
