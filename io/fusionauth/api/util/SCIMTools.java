package io.fusionauth.api.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import io.fusionauth.api.scim.SCIMException;
import io.fusionauth.scim.domain.SCIMListResponse;
import io.fusionauth.scim.domain.SCIMMeta;
import io.fusionauth.scim.domain.SCIMResource;
import io.fusionauth.scim.domain.SCIMResourceType;
import io.fusionauth.scim.domain.SCIMServiceProviderConfig;
import io.fusionauth.scim.domain.SchemaExtension;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SCIMTools {
  public static int MaxResultSize = 500;
  
  public static String findFieldWithMarker(JsonNode paramJsonNode, String paramString) {
    return findMarker(paramJsonNode, paramString, ".");
  }
  
  public static String findPathWithMarker(JsonNode paramJsonNode, String paramString) {
    String str = findMarker(paramJsonNode, paramString, "/");
    return (str != null) ? ("/" + str) : null;
  }
  
  public static SCIMResource getResourceTypeById(String paramString1, String paramString2) {
    Objects.requireNonNull(paramString2);
    return (SCIMResource)buildSCIMResourceType(paramString1, paramString2);
  }
  
  public static SCIMListResponse getResourceTypes(String paramString) {
    return (SCIMListResponse)((SCIMListResponse)((SCIMListResponse)((SCIMListResponse)((SCIMListResponse)((SCIMListResponse)(new SCIMListResponse())
      .with(paramSCIMListResponse -> paramSCIMListResponse.itemsPerPage = 10))
      .with(paramSCIMListResponse -> paramSCIMListResponse.startIndex = 1))
      .with(paramSCIMListResponse -> paramSCIMListResponse.totalResults = 2))
      .with(paramSCIMListResponse -> paramSCIMListResponse.schemas = List.of("urn:ietf:params:scim:api:messages:2.0:ListResponse")))

      
      .with(paramSCIMListResponse -> paramSCIMListResponse.Resources.add(buildSCIMResourceType(paramString, "User"))))
      
      .with(paramSCIMListResponse -> paramSCIMListResponse.Resources.add(buildSCIMResourceType(paramString, "Group")));
  }
  
  public static SCIMServiceProviderConfig getServerProviderConfig(String paramString) {
    return (SCIMServiceProviderConfig)((SCIMServiceProviderConfig)((SCIMServiceProviderConfig)((SCIMServiceProviderConfig)((SCIMServiceProviderConfig)((SCIMServiceProviderConfig)((SCIMServiceProviderConfig)((SCIMServiceProviderConfig)((SCIMServiceProviderConfig)(new SCIMServiceProviderConfig())
      .with(paramSCIMServiceProviderConfig -> paramSCIMServiceProviderConfig.authenticationSchemes = List.of(Map.of("description", "Authentication scheme using the OAuth Bearer Token Standard", "name", "OAuth2 Bearer Token", "primary", Boolean.valueOf(true), "specUri", "http://www.rfc-editor.org/info/rfc6750", "type", "oauthbearertoken"))))




      
      .with(paramSCIMServiceProviderConfig -> paramSCIMServiceProviderConfig.bulk = Map.of("supported", Boolean.valueOf(false))))
      .with(paramSCIMServiceProviderConfig -> paramSCIMServiceProviderConfig.changePassword = Map.of("supported", Boolean.valueOf(true))))
      .with(paramSCIMServiceProviderConfig -> paramSCIMServiceProviderConfig.etag = Map.of("supported", Boolean.valueOf(false))))
      .with(paramSCIMServiceProviderConfig -> paramSCIMServiceProviderConfig.filter = Map.of("supported", Boolean.valueOf(false), "maxResults", Integer.valueOf(MaxResultSize))))
      
      .with(paramSCIMServiceProviderConfig -> paramSCIMServiceProviderConfig.meta = (SCIMMeta)((SCIMMeta)(new SCIMMeta()).with(())).with(())))
      
      .with(paramSCIMServiceProviderConfig -> paramSCIMServiceProviderConfig.patch = Map.of("supported", Boolean.valueOf(false))))
      .with(paramSCIMServiceProviderConfig -> paramSCIMServiceProviderConfig.schemas = List.of("urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig")))
      .with(paramSCIMServiceProviderConfig -> paramSCIMServiceProviderConfig.sort = Map.of("supported", Boolean.valueOf(false)));
  }
  
  private static SCIMResourceType buildSCIMResourceType(String paramString1, String paramString2) {
    if ("User".equals(paramString2))
      return (SCIMResourceType)((SCIMResourceType)((SCIMResourceType)((SCIMResourceType)((SCIMResourceType)((SCIMResourceType)((SCIMResourceType)((SCIMResourceType)(new SCIMResourceType()).with(paramSCIMResourceType -> paramSCIMResourceType.schemas = List.of("urn:ietf:params:scim:schemas:core:2.0:ResourceType")))
        .with(paramSCIMResourceType -> paramSCIMResourceType.id = "User"))
        .with(paramSCIMResourceType -> paramSCIMResourceType.name = "User"))
        .with(paramSCIMResourceType -> paramSCIMResourceType.endpoint = "/Users"))
        .with(paramSCIMResourceType -> paramSCIMResourceType.description = "User Account"))
        .with(paramSCIMResourceType -> paramSCIMResourceType.schema = "urn:ietf:params:scim:schemas:core:2.0:User"))
        .with(paramSCIMResourceType -> paramSCIMResourceType.schemaExtensions = List.of(((SchemaExtension)(new SchemaExtension()).with(())).with(()))))

        
        .with(paramSCIMResourceType -> paramSCIMResourceType.meta = (SCIMMeta)((SCIMMeta)(new SCIMMeta()).with(())).with(())); 
    if ("Group".equals(paramString2))
      return (SCIMResourceType)((SCIMResourceType)((SCIMResourceType)((SCIMResourceType)((SCIMResourceType)((SCIMResourceType)((SCIMResourceType)(new SCIMResourceType()).with(paramSCIMResourceType -> paramSCIMResourceType.schemas = List.of("urn:ietf:params:scim:schemas:core:2.0:ResourceType")))
        .with(paramSCIMResourceType -> paramSCIMResourceType.id = "Group"))
        .with(paramSCIMResourceType -> paramSCIMResourceType.name = "Group"))
        .with(paramSCIMResourceType -> paramSCIMResourceType.endpoint = "/Groups"))
        .with(paramSCIMResourceType -> paramSCIMResourceType.description = "Group"))
        .with(paramSCIMResourceType -> paramSCIMResourceType.schema = "urn:ietf:params:scim:schemas:core:2.0:Group"))
        .with(paramSCIMResourceType -> paramSCIMResourceType.meta = (SCIMMeta)((SCIMMeta)(new SCIMMeta()).with(())).with(())); 
    throw new SCIMException("missing", String.format("ResourceType %s not found.", new Object[] { paramString2 }));
  }
  
  private static String findMarker(JsonNode paramJsonNode, String paramString1, String paramString2) {
    if (paramJsonNode instanceof ObjectNode) {
      ObjectNode objectNode = (ObjectNode)paramJsonNode;
      for (Iterator<String> iterator = objectNode.fieldNames(); iterator.hasNext(); ) {
        String str = iterator.next();
        JsonNode jsonNode = objectNode.get(str);
        if (jsonNode instanceof ObjectNode) {
          String str1 = findMarker(jsonNode, paramString1, paramString2);
          if (str1 != null)
            return str + str + paramString2; 
        } 
        boolean bool = matchNodeWithMarker(jsonNode, paramString1);
        if (bool)
          return str; 
      } 
    } else if (paramJsonNode instanceof ArrayNode) {
      ArrayNode arrayNode = (ArrayNode)paramJsonNode;
      for (JsonNode jsonNode : arrayNode) {
        boolean bool = matchNodeWithMarker(jsonNode, paramString1);
        if (bool)
          return null; 
      } 
    } 
    return null;
  }
  
  private static boolean matchNodeWithMarker(JsonNode paramJsonNode, String paramString) {
    if (paramJsonNode instanceof ValueNode) {
      ValueNode valueNode = (ValueNode)paramJsonNode;
      return paramString.equals(valueNode.textValue());
    } 
    return false;
  }
}
