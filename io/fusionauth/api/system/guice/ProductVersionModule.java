package io.fusionauth.api.system.guice;

import com.google.inject.AbstractModule;
import com.inversoft.support.service.guice.LatestProductVersionURL;
import com.inversoft.support.service.guice.ProductVersionString;

public class ProductVersionModule extends AbstractModule {
  protected void configure() {
    String str = ProductVersionModule.class.getPackage().getImplementationVersion();
    if (str == null)
      str = "1.9999.9999"; 
    bindConstant().annotatedWith(ProductVersionString.class).to(str);
    bindConstant().annotatedWith(LatestProductVersionURL.class).to("https://metrics.fusionauth.io/api/latest-version");
  }
}
