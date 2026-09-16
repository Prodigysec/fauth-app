package io.fusionauth.app.action.api.twoFactor;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.jwt.FusionAuthJWTDecoder;
import io.fusionauth.api.service.jwt.JWTService;
import io.fusionauth.api.service.jwt.JWTValidationContext;
import io.fusionauth.api.service.jwt.ValidatedJWTResult;
import io.fusionauth.api.service.mfa.MFAService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.api.service.risk.CompositeRisk;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.MultiFactorAction;
import io.fusionauth.domain.api.twoFactor.TwoFactorStatusRequest;
import io.fusionauth.domain.api.twoFactor.TwoFactorStatusResponse;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{twoFactorTrustId}", requiresAuthentication = true, scheme = {"api"})
@JSON(code = "two-factor-challenge", status = 242)
public class StatusAction extends BaseTenantAPIAction {
  @JSONResponse
  public final TwoFactorStatusResponse response = new TwoFactorStatusResponse();
  
  private final ApplicationCache applicationCache;
  
  private final JWTService jwtService;
  
  private final MFAService mfaService;
  
  public UUID applicationId;
  
  @JSONRequest
  public TwoFactorStatusRequest request = new TwoFactorStatusRequest(null);
  
  public String twoFactorTrustId;
  
  public UUID userId;
  
  private MFAService.ValidationResult result;
  
  @Inject
  public StatusAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ApplicationCache paramApplicationCache, MFAService paramMFAService, JWTService paramJWTService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.applicationCache = paramApplicationCache;
    this.mfaService = paramMFAService;
    this.jwtService = paramJWTService;
  }
  
  public String get() {
    this.response.twoFactorTrustId = this.twoFactorTrustId;
    populateResultTrustData();
    return 




      
      this.mfaService.determineChallengeRequired(MultiFactorAction.login, getTenant(), this.result.user, this.result.application, this.result.id, null, CompositeRisk.NO_RISK, null, null, false).required() ? 
      "two-factor-challenge" : 
      "render";
  }
  
  public String post() {
    this.response.twoFactorTrustId = this.request.twoFactorTrustId;
    populateResultTrustData();
    String str = null;
    if (this.request.accessToken != null)
      try {
        JWTValidationContext jWTValidationContext = (JWTValidationContext)((this.result.application == null) ? new JWTValidationContext.SuppliedTenantAudienceApplicationIfResolvable(getTenant()) : new JWTValidationContext.SuppliedTenantAndApplication(getTenant(), this.result.application));
        ValidatedJWTResult validatedJWTResult = this.jwtService.validateJWT(this.request.accessToken, FusionAuthJWTDecoder.JWTConstraints.UserAccessToken, jWTValidationContext);
        if (validatedJWTResult.valid)
          str = this.request.accessToken; 
      } catch (Exception exception) {} 
    MFAService.ChallengeResult challengeResult = this.mfaService.determineChallengeRequired(this.request.action, getTenant(), this.result.user, this.result.application, this.result.id, this.request.eventInfo, CompositeRisk.NO_RISK, str, null, false);
    return challengeResult.required() ? 
      "two-factor-challenge" : 
      "render";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    this.result = this.mfaService.validateTwoFactorStatus(getOptionalTenant(), this.applicationId, this.userId, this.twoFactorTrustId);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    this.result = this.mfaService.validateTwoFactorStatus(getOptionalTenant(), this.request.applicationId, this.request.userId, this.request.twoFactorTrustId);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  private void populateResultTrustData() {
    if (this.result.id != null && this.result.id.data != null && this.result.id.data.startInstants != null) {
      if (ReactorStatusValidator.isLicensedFor(this.result.reactorStatus, paramReactorStatus -> paramReactorStatus.applicationMultiFactorAuthentication)) {
        this.result.id.data.startInstants.applications.keySet()
          .forEach(paramUUID -> this.response.trusts.add(new TwoFactorStatusResponse.TwoFactorTrust(paramUUID, ((ZonedDateTime)this.result.id.data.startInstants.applications.get(paramUUID)).plusSeconds(ExternalIdentifier.getTTL(ExternalIdentifier.ExternalIdType.TwoFactorTrust, this.tenant, ())), this.result.id.data.startInstants.applications.get(paramUUID))));
      } else {
        ZonedDateTime zonedDateTime = this.result.id.data.startInstants.applications.get(Application.FUSIONAUTH_APP_ID);
        if (zonedDateTime != null)
          this.response.trusts.add(new TwoFactorStatusResponse.TwoFactorTrust(Application.FUSIONAUTH_APP_ID, zonedDateTime
                .plusSeconds(ExternalIdentifier.getTTL(ExternalIdentifier.ExternalIdType.TwoFactorTrust, this.tenant, () -> (Application)this.applicationCache.get(Application.FUSIONAUTH_APP_ID))), zonedDateTime)); 
      } 
      if (this.result.id.data.startInstants.tenant != null)
        this.response.trusts.add(new TwoFactorStatusResponse.TwoFactorTrust(null, this.result.id.data.startInstants.tenant
              .plusSeconds(ExternalIdentifier.getTTL(ExternalIdentifier.ExternalIdType.TwoFactorTrust, this.tenant)), this.result.id.data.startInstants.tenant)); 
    } 
  }
}
