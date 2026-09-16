package io.fusionauth.api.migration.guice;

import com.google.inject.Inject;
import com.inversoft.migration.Migration;
import io.fusionauth.api.service.system.SystemDefaultsSingleton;
import org.savantbuild.domain.Version;

public class Migration_1_30_0 implements Migration {
  private final SystemDefaultsSingleton systemDefaults;
  
  @Inject
  public Migration_1_30_0(SystemDefaultsSingleton paramSystemDefaultsSingleton) {
    this.systemDefaults = paramSystemDefaultsSingleton;
  }
  
  public void cleanup() throws Exception {}
  
  public void runOnce() throws Exception {
    this.systemDefaults.set(new Version("1.30.0"));
  }
}
