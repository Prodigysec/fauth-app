package io.fusionauth.app;

import com.inversoft.util.LoggerTool;
import io.fusionauth.app.fips.FIPSClassLoader;
import io.fusionauth.domain.FIPS;
import java.nio.file.Path;
import java.security.Provider;
import java.security.Security;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.Logger;

public class FIPSActivator {
  public static void optionallyEnable(Logger paramLogger) {
    if (!FIPS.isEnabled())
      return; 
    LoggerTool.logPrettyInfoMessage(paramLogger, "Enabling FIPS [fusionauth.fips.enabled=true]");
    Path path1 = Path.of(System.getProperty("fusionauth.home.directory"), new String[0]);
    Path path2 = path1.resolve("lib/fips");
    FIPSClassLoader fIPSClassLoader = FIPSClassLoader.makeFIPSClassLoader(path2, FIPSActivator.class.getClassLoader());
    if (fIPSClassLoader == null) {
      paramLogger.error("\n\n------------------------------------------------------------------------------------------------\n                                       !!!FATAL ERROR!!!\n\nThe FIPS library directory does not exist. This could mean that it was deleted. Regardless,\nsince FIPS is required, FusionAuth must shutdown.\n------------------------------------------------------------------------------------------------\n");
      System.exit(1);
    } 
    System.setProperty("org.bouncycastle.fips.approved_only", "true");
    try {
      Class<?> clazz1 = fIPSClassLoader.loadClass("org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider");
      Provider provider1 = clazz1.getConstructor(new Class[0]).newInstance(new Object[0]);
      Security.insertProviderAt(provider1, 1);
      Class<?> clazz2 = fIPSClassLoader.loadClass("org.bouncycastle.jsse.provider.BouncyCastleJsseProvider");
      Provider provider2 = clazz2.getConstructor(new Class[] { String.class }).newInstance(new Object[] { "fips:BCFIPS" });
      Security.insertProviderAt(provider2, 2);
      Logger.getLogger("org.bouncycastle").setLevel(Level.WARNING);
      Class<?> clazz3 = fIPSClassLoader.loadClass("org.bouncycastle.crypto.CryptoServicesRegistrar");
      if (!((Boolean)clazz3.getMethod("isInApprovedOnlyMode", new Class[0]).invoke(null, new Object[0])).booleanValue()) {
        paramLogger.error("\n\n------------------------------------------------------------------------------------------------\n                                       !!!FATAL ERROR!!!\n\nBouncy Castle was not configured properly and FIPS cannot be enabled. Since the configuration\nrequired FIPS to be enabled, FusionAuth must shutdown.\n------------------------------------------------------------------------------------------------\n");
        System.exit(1);
      } 
    } catch (Exception exception) {
      paramLogger.error("\n\n------------------------------------------------------------------------------------------------\n                                       !!!FATAL ERROR!!!\n\nAn exception was encountered while trying to activate FIPS. The exception is below.\n------------------------------------------------------------------------------------------------\n");
      paramLogger.error("Exception", exception);
      System.exit(1);
    } 
  }
}
