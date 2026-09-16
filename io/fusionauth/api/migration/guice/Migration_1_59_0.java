package io.fusionauth.api.migration.guice;

import com.google.inject.Inject;
import io.fusionauth.api.configuration.FusionAuthConfiguration;

public class Migration_1_59_0 extends BaseElasticMigration {
  @Inject
  public Migration_1_59_0(FusionAuthConfiguration paramFusionAuthConfiguration) {
    super(paramFusionAuthConfiguration);
  }
  
  protected String fieldName() {
    return "phoneNumber";
  }
  
  protected String mappingJson() {
    return "{\n  \"properties\": {\n    \"phoneNumber\": {\n      \"type\": \"text\",\n      \"fielddata\": true\n    }\n  }\n}\n";
  }
}
