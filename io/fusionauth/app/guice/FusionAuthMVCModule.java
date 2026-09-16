package io.fusionauth.app.guice;

import com.google.inject.AbstractModule;
import io.fusionauth.app.primeframework.FusionAuthCORSConfigurationProvider;
import io.fusionauth.app.primeframework.FusionAuthCORSDebuggerProvider;
import io.fusionauth.app.primeframework.FusionAuthMVCConfiguration;
import io.fusionauth.app.primeframework.FusionAuthUserLoginSecurityContext;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.primeframework.ThemedForwardResult;
import org.primeframework.mvc.action.result.ResultFactory;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.cors.CORSConfigurationProvider;
import org.primeframework.mvc.cors.CORSDebugger;
import org.primeframework.mvc.security.UserLoginSecurityContext;

public class FusionAuthMVCModule extends AbstractModule {
  public void configure() {
    ResultFactory.addResult(binder(), ThemedForward.class, ThemedForwardResult.class);
    bind(CORSConfigurationProvider.class).to(FusionAuthCORSConfigurationProvider.class);
    bind(CORSDebugger.class).toProvider(FusionAuthCORSDebuggerProvider.class);
    bind(MVCConfiguration.class).to(FusionAuthMVCConfiguration.class).asEagerSingleton();
    bind(UserLoginSecurityContext.class).to(FusionAuthUserLoginSecurityContext.class);
  }
}
