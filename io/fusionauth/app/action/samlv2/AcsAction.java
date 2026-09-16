package io.fusionauth.app.action.samlv2;

import com.google.inject.Inject;
import io.fusionauth.api.service.cache.IdentityProviderCache;
import io.fusionauth.api.service.identity.IdentityProviderReaderService;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.system.CipherService;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.app.action.oauth2.CallbackAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Status;

@Action("{identityProviderId}/{client_id}")
@Status(code = "missing", status = 404)
public class AcsAction extends CallbackAction {
  public String binding;
  
  @Inject
  public AcsAction(ExternalIdentifierReaderService paramExternalIdentifierReaderService, FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, CipherService paramCipherService, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, IdentityProviderCache paramIdentityProviderCache, IdentityProviderReaderService paramIdentityProviderReaderService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramExternalIdentifierReaderService, paramFrontEndSupport, paramFrontEndThemeResolver, paramIdentityProviderCache, paramIdentityProviderReaderService, paramLoginIntentService, paramSSOService, paramOAuthService, paramThreatDetectionService, paramCipherService);
  }
}
