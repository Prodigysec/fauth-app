package io.fusionauth.app.action.samlv2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import io.fusionauth.api.security.KeyCache;
import io.fusionauth.api.service.samlv2.SAMLv2ProviderService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.primeframework.ThemedForward.List;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.samlv2.domain.Binding;
import io.fusionauth.samlv2.domain.LogoutRequest;
import io.fusionauth.samlv2.domain.ResponseStatus;
import io.fusionauth.samlv2.domain.SAMLException;
import io.fusionauth.samlv2.domain.SAMLRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.action.result.annotation.Status;

@Action("{localTenantId}")
@List({@ThemedForward(code = "post-to-reply", page = "./logout/post.ftl"), @ThemedForward(code = "render-error", page = "../oauth2/error.ftl")})
@List({@Redirect(code = "default-redirect", uri = "/"), @Redirect(code = "redirect", uri = "${logoutURL}")})
@Status(code = "missing", status = 404)
public class LogoutAction extends BaseSAMLAction {
  @FTLVariable
  public List<URI> allLogoutURLs = new ArrayList<>();
  
  public String logoutURL;
  
  @FTLVariable
  public List<URI> registeredLogoutURLs = new ArrayList<>();
  
  public SAMLv2ProviderService.FusionAuthLogoutResponse response;
  
  @Inject
  public LogoutAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, KeyCache paramKeyCache, LoginIntentService paramLoginIntentService, SAMLv2ProviderService paramSAMLv2ProviderService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramKeyCache, paramLoginIntentService, paramSAMLv2ProviderService, paramSSOService, paramThreatDetectionService);
  }
  
  public String get() {
    String str = commonSAMLValidation();
    if (str != null)
      return str; 
    this.binding = this.frontEndSupport.isGET() ? Binding.HTTP_Redirect : Binding.HTTP_POST;
    return handleSAMLException(() -> {
          LogoutRequest logoutRequest = (this.binding == Binding.HTTP_POST) ? this.samlv2ProviderService.parseLogoutPostRequest(this.tenantId, this.SAMLRequest) : this.samlv2ProviderService.parseLogoutRedirectRequest(this.tenantId, this.frontEndSupport.request.getQueryString());
          normalize((SAMLRequest)logoutRequest);
          this.response = this.samlv2ProviderService.validateLogoutRequest((SAMLRequest)logoutRequest, this.tenantId);
          if (this.response.application == null) {
            this.oauthJSONError = this.frontEndSupport.writeToPrettyString(this.response.status);
            return "render-error";
          } 
          this.client_id = this.response.application.id.toString();
          resolveApplicationTenantAndTheme();
          this.samlv2ProviderService.logSAMLRequest(this.response.application.samlv2Configuration.debug, this.binding, "LogoutRequest", (SAMLRequest)logoutRequest, this.SAMLRequest);
          if (this.response.status.code != ResponseStatus.Success) {
            if (this.response.application.samlv2Configuration.debug)
              EventLogHelper.create(new EventLog(EventLogType.Debug, "The SAML LogoutRequest was invalid or did not pass validation.\nThe error code is [" + String.valueOf(this.response.status.code) + "] and the error message is [" + this.response.status.message + "]")); 
            URI uRI = (this.response.application.samlv2Configuration.logoutURL != null) ? this.response.application.samlv2Configuration.logoutURL : this.tenant.logoutURL;
            this.logoutURI = (uRI == null) ? "/" : uRI.toString();
            this.response.destination = this.logoutURI;
            return buildSAMLLogoutResponse(this.response);
          } 
          if (logoutRequest.sessionIndex != null && this.ssoSession.id != null && this.ssoSession.id.toString().equals(logoutRequest.sessionIndex))
            this.ssoService.logout(this.ssoSession, this.ssoCookie, this.codeTenant, this.frontEndSupport.buildEventInfo(null)); 
          if (this.response.application.samlv2Configuration.logout.behavior == Application.SAMLv2Configuration.SAMLLogoutBehavior.AllParticipants)
            setupSessionParticipantLogoutURLs(); 
          return "input";
        });
  }
  
  public Binding getBinding() {
    return this.binding;
  }
  
  public String post() {
    return get();
  }
  
  private void setupSessionParticipantLogoutURLs() throws SAMLException {
    Map<UUID, URI> map = this.samlv2ProviderService.getSessionParticipantLogoutURLs(this.tenantId, this.response.application, this.frontEndSupport.getFusionAuthBaseURL());
    this.allLogoutURLs.addAll(map.values());
    if (this.codeUser != null) {
      Objects.requireNonNull(map);
      this.registeredLogoutURLs.addAll(map.keySet().stream().filter(paramUUID -> (this.codeUser.getRegistrationForApplication(paramUUID) != null)).map(map::get)
          .toList());
    } 
  }
}
