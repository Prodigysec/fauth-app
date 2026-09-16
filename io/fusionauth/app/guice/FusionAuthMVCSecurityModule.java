package io.fusionauth.app.guice;

import com.google.inject.AbstractModule;
import io.fusionauth.app.primeframework.AccountUserLoginSecurityScheme;
import io.fusionauth.app.primeframework.FusionAPIKeySecurityScheme;
import io.fusionauth.app.primeframework.FusionAuthBasicAuthAPIKeySecurityScheme;
import io.fusionauth.app.primeframework.FusionAuthCSRFProvider;
import io.fusionauth.app.primeframework.FusionAuthInternalAPIKeySecurityScheme;
import io.fusionauth.app.primeframework.FusionAuthLocalMetricsSecurityScheme;
import io.fusionauth.app.primeframework.FusionAuthSCIMSecurityScheme;
import io.fusionauth.app.primeframework.FusionAuthScopedJWTSecurityScheme;
import io.fusionauth.app.primeframework.FusionAuthUnsafeJWTSecurityScheme;
import io.fusionauth.app.primeframework.FusionAuthUserLoginSecurityScheme;
import io.fusionauth.app.primeframework.FusionAuthVerifierProvider;
import io.fusionauth.app.primeframework.FusionNoTenantAPIKeySecurityScheme;
import io.fusionauth.app.primeframework.TenantManagerUserLoginSecurityScheme;
import java.util.Set;
import org.primeframework.mvc.security.VerifierProvider;
import org.primeframework.mvc.security.csrf.CSRFProvider;
import org.primeframework.mvc.security.guice.SecuritySchemeFactory;

public class FusionAuthMVCSecurityModule extends AbstractModule {
  public static final String SCHEME_API_NO_TENANT = "api-no-tenant";
  
  private static final String SCHEME_API = "api";
  
  public static final Set<String> apiSecuritySchemes = Set.of("api", "api-no-tenant");
  
  protected void configure() {
    SecuritySchemeFactory.addSecurityScheme(binder(), "account-user", AccountUserLoginSecurityScheme.class);
    SecuritySchemeFactory.addSecurityScheme(binder(), "api", FusionAPIKeySecurityScheme.class);
    SecuritySchemeFactory.addSecurityScheme(binder(), "api-no-tenant", FusionNoTenantAPIKeySecurityScheme.class);
    SecuritySchemeFactory.addSecurityScheme(binder(), "api-basic-auth", FusionAuthBasicAuthAPIKeySecurityScheme.class);
    SecuritySchemeFactory.addSecurityScheme(binder(), "api-scim", FusionAuthSCIMSecurityScheme.class);
    SecuritySchemeFactory.addSecurityScheme(binder(), "api-internal", FusionAuthInternalAPIKeySecurityScheme.class);
    SecuritySchemeFactory.addSecurityScheme(binder(), "local-metrics", FusionAuthLocalMetricsSecurityScheme.class);
    SecuritySchemeFactory.addSecurityScheme(binder(), "tenant-manager", TenantManagerUserLoginSecurityScheme.class);
    SecuritySchemeFactory.addSecurityScheme(binder(), "user", FusionAuthUserLoginSecurityScheme.class);
    SecuritySchemeFactory.addSecurityScheme(binder(), "unsafe-jwt", FusionAuthUnsafeJWTSecurityScheme.class);
    SecuritySchemeFactory.addSecurityScheme(binder(), "scoped-jwt", FusionAuthScopedJWTSecurityScheme.class);
    bind(CSRFProvider.class).to(FusionAuthCSRFProvider.class);
    bind(VerifierProvider.class).to(FusionAuthVerifierProvider.class);
  }
}
