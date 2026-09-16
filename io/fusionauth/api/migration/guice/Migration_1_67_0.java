package io.fusionauth.api.migration.guice;

import com.google.inject.Inject;
import io.fusionauth.api.configuration.FusionAuthConfiguration;

public class Migration_1_67_0 extends BaseElasticMigration {
  @Inject
  public Migration_1_67_0(FusionAuthConfiguration paramFusionAuthConfiguration) {
    super(paramFusionAuthConfiguration);
  }
  
  protected String fieldName() {
    return "legacyIdentifier";
  }
  
  protected String mappingJson() {
    return "{\n  \"properties\": {\n    \"legacyIdentifier\": {\n      \"type\": \"keyword\"\n    }\n  }\n}\n";
  }
}
