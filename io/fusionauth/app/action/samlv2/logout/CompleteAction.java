package io.fusionauth.app.action.samlv2.logout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import io.fusionauth.api.security.KeyCache;
import io.fusionauth.api.service.samlv2.SAMLv2Helper;
import io.fusionauth.api.service.samlv2.SAMLv2ProviderService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.action.samlv2.BaseSAMLAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.samlv2.domain.Binding;
import io.fusionauth.samlv2.domain.LogoutRequest;
import io.fusionauth.samlv2.domain.ResponseStatus;
import io.fusionauth.samlv2.domain.SAMLException;
import io.fusionauth.samlv2.domain.SAMLRequest;
import io.fusionauth.samlv2.domain.Status;
import java.net.URI;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.util.QueryStringBuilder;

@Action
@Forward(code = "post-to-reply", page = "post.ftl")
public class CompleteAction extends BaseSAMLAction {
  @Inject
  public CompleteAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, KeyCache paramKeyCache, LoginIntentService paramLoginIntentService, SAMLv2ProviderService paramSAMLv2ProviderService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramKeyCache, paramLoginIntentService, paramSAMLv2ProviderService, paramSSOService, paramThreatDetectionService);
  }
  
  public String post() {
    return handleSAMLException(() -> {
          LogoutRequest logoutRequest = (this.binding == Binding.HTTP_POST) ? this.samlv2ProviderService.parseLogoutPostRequest(this.tenantId, this.SAMLRequest) : this.samlv2ProviderService.parseLogoutRedirectRequest(this.tenantId, QueryStringBuilder.builder().with("RelayState", this.RelayState).with("SAMLRequest", this.SAMLRequest).with("SigAlg", this.SigAlg).with("Signature", this.Signature).build());
          SAMLv2ProviderService.FusionAuthLogoutResponse fusionAuthLogoutResponse1 = this.samlv2ProviderService.validateLogoutRequest((SAMLRequest)logoutRequest, this.tenantId);
          if (fusionAuthLogoutResponse1.application == null) {
            this.oauthJSONError = this.frontEndSupport.writeToPrettyString(fusionAuthLogoutResponse1.status);
            return "render-error";
          } 
          SAMLv2ProviderService.FusionAuthLogoutResponse fusionAuthLogoutResponse2 = new SAMLv2ProviderService.FusionAuthLogoutResponse();
          fusionAuthLogoutResponse2.application = fusionAuthLogoutResponse1.application;
          URI uRI = (fusionAuthLogoutResponse1.application.samlv2Configuration.logoutURL != null) ? fusionAuthLogoutResponse1.application.samlv2Configuration.logoutURL : this.tenant.logoutURL;
          this.logoutURI = (uRI == null) ? "/" : uRI.toString();
          fusionAuthLogoutResponse2.destination = this.logoutURI;
          fusionAuthLogoutResponse2.issuer = SAMLv2Helper.getIdentityProviderEntityId(this.frontEndSupport.getFusionAuthBaseURL(), fusionAuthLogoutResponse1.application.tenantId);
          fusionAuthLogoutResponse2.inResponseTo = logoutRequest.id;
          fusionAuthLogoutResponse2.status = new Status();
          fusionAuthLogoutResponse2.status.code = ResponseStatus.Success;
          return buildSAMLLogoutResponse(fusionAuthLogoutResponse2);
        });
  }
  
  public void setBinding(Binding paramBinding) {
    this.binding = paramBinding;
  }
}
