package io.fusionauth.app.guice;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.domain.guice.TenantManagerFusionAuthClientProvider;
import io.fusionauth.api.security.TenantManagerConstraintValidator;
import io.fusionauth.app.primeframework.TenantManagerCSRFProvider;
import io.fusionauth.app.primeframework.TenantManagerUserLoginSecurityContext;
import io.fusionauth.app.service.user.CustomFormFrontendService;
import io.fusionauth.app.service.user.TenantManagerCustomFormFrontendService;
import java.lang.annotation.Annotation;
import java.util.UUID;
import org.primeframework.mvc.security.UserLoginConstraintsValidator;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.csrf.CSRFProvider;

public class TenantManagerModule extends AbstractModule {
  protected void configure() {
    binder()
      .bind(CustomFormFrontendService.class)
      .annotatedWith((Annotation)Names.named("TenantManagerCustomFormFrontendService"))
      .to(TenantManagerCustomFormFrontendService.class);
    binder()
      .bind(UserLoginSecurityContext.class)
      .annotatedWith((Annotation)Names.named("TenantManagerSecurityContext"))
      .to(TenantManagerUserLoginSecurityContext.class);
    binder()
      .bind(UserLoginConstraintsValidator.class)
      .annotatedWith((Annotation)Names.named("TenantManagerConstraintValidator"))
      .to(TenantManagerConstraintValidator.class);
    binder()
      .bind(CSRFProvider.class)
      .annotatedWith((Annotation)Names.named("TenantManagerCSRFProvider"))
      .to(TenantManagerCSRFProvider.class);
    binder()
      .bind(FusionAuthClientProvider.class)
      .annotatedWith((Annotation)Names.named("TenantManagerFusionAuthClientProvider"))
      .to(TenantManagerFusionAuthClientProvider.class);
    bind(UUID.class)
      .annotatedWith(TenantManagerApplicationId.class)
      .toProvider(TenantManagerApplicationIdProvider.class);
  }
}
