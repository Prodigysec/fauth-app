package io.fusionauth.app.action.oauth2.device;

import com.google.inject.Inject;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.app.action.oauth2.BaseIntrospectAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.domain.oauth2.DeviceApprovalResponse;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class ApproveAction extends BaseIntrospectAction {
  private final FrontEndSupport frontEndSupport;
  
  public RefreshToken.MetaData metaData = new RefreshToken.MetaData();
  
  public String token;
  
  public String user_code;
  
  private OAuthService.OAuthValidationResult result;
  
  @Inject
  public ApproveAction(FrontEndSupport paramFrontEndSupport, OAuthService paramOAuthService, HTTPRequest paramHTTPRequest, HTTPResponse paramHTTPResponse) {
    super(paramHTTPRequest, paramHTTPResponse, paramOAuthService);
    this.frontEndSupport = paramFrontEndSupport;
  }
  
  public String post() {
    OAuthService.DeviceApproveResult deviceApproveResult = this.oauthService.approveDevice(this.frontEndSupport.buildEventInfo(this.metaData), this.result.tenant, this.result.application, this.result.user, this.result.externalIdentifier, this.result.scopes);
    this


      
      .response = (new DeviceApprovalResponse()).with(paramDeviceApprovalResponse -> paramDeviceApprovalResponse.deviceGrantStatus = paramDeviceApproveResult.deviceGrantStatus).with(paramDeviceApprovalResponse -> paramDeviceApprovalResponse.deviceInfo = paramDeviceApproveResult.deviceInfo).with(paramDeviceApprovalResponse -> paramDeviceApprovalResponse.identityProviderLink = paramDeviceApproveResult.identityProviderLink).with(paramDeviceApprovalResponse -> paramDeviceApprovalResponse.tenantId = paramDeviceApproveResult.tenantId).with(paramDeviceApprovalResponse -> paramDeviceApprovalResponse.userId = paramDeviceApproveResult.userId);
    return "render";
  }
  
  @ValidationMethod
  public void validate() {
    this.result = this.oauthService.validateDeviceApproveRequest(this.httpRequest.getHeader("Authorization"), this.client_id, this.client_secret, this.token, this.user_code, this.tenantId);
    handleValidationResult(this.result);
  }
}
