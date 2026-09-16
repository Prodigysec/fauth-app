package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPContext;
import org.savantbuild.domain.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FusionHTTPContextAuthSetup {
  private static final Logger logger = LoggerFactory.getLogger(FusionHTTPContextAuthSetup.class);
  
  @Inject
  public FusionHTTPContextAuthSetup(HTTPContext paramHTTPContext) {
    logger.info("Initializing the FusionAuth HTTP Context.");
    Package package_ = FusionHTTPContextAuthSetup.class.getPackage();
    String str = package_.getImplementationVersion();
    paramHTTPContext.setAttribute("version", (str != null) ? new Version(str) : new Version("0.0.0+dev"));
  }
}
