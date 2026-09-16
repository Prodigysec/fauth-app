package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.cache.IdentityProviderCache;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.IdentityProviderLink;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLinkResponse;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.PendingIdPLink;
import io.fusionauth.http.server.HTTPRequest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class DeviceCompleteAction extends BaseOAuthAction {
  private final HTTPRequest httpRequest;
  
  private final IdentityProviderCache identityProviderCache;
  
  public List<PendingIdPLink> completedLinks = new ArrayList<>(1);
  
  public OAuthService.OAuthValidationResult result;
  
  @Inject
  public DeviceCompleteAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, IdentityProviderCache paramIdentityProviderCache, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService, HTTPRequest paramHTTPRequest) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    this.identityProviderCache = paramIdentityProviderCache;
    this.httpRequest = paramHTTPRequest;
  }
  
  public String get() {
    retrieveRecentIdPLInks();
    return "input";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    this.result = this.oauthService.validateDeviceRequest(this.tenantId, this.client_id, null, this.httpRequest);
    setAntiClickJackingHeader(this.result.application);
    if (this.result.error != null) {
      this.oauthJSONError = this.frontEndSupport.writeToPrettyString(this.result.error);
      throw new ErrorException("render-error");
    } 
    setResultValues(this.result);
  }
  
  private void retrieveRecentIdPLInks() {
    User user = this.ssoSession.user;
    if (user != null) {
      ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(2L);
      List list1 = (List)Objects.requireNonNullElseGet(((IdentityProviderLinkResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserLinksByUserId(null, paramUser.id))).identityProviderLinks, Collections::emptyList);
      List list2 = (List)list1.stream().filter(paramIdentityProviderLink -> paramIdentityProviderLink.insertInstant.isAfter(paramZonedDateTime)).collect(Collectors.toList());
      list2.forEach(paramIdentityProviderLink -> this.completedLinks.add((new PendingIdPLink()).with(()).with(()).with(()).with(()).with(()).with(()).with(())));
    } 
  }
}
