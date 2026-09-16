package io.fusionauth.app.action.api.jwt;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.jwt.JWTService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.jwt.JWTVendRequest;
import io.fusionauth.domain.api.jwt.JWTVendResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api"})
public class VendAction extends BaseTenantAPIAction {
  private final JWTService jwtService;
  
  @JSONRequest
  public JWTVendRequest request;
  
  @JSONResponse
  public JWTVendResponse response;
  
  private JWTService.VendValidationResult result;
  
  @Inject
  public VendAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, JWTService paramJWTService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.jwtService = paramJWTService;
  }
  
  public String post() {
    JWTService.JWTResult jWTResult = this.jwtService.createVendedJWT(this.result.key, this.result.timeToLiveInSeconds, this.request.claims);
    this.response = new JWTVendResponse(jWTResult.encodedJWT);
    return "render";
  }
  
  @ValidationMethod
  public void validate() {
    this.result = this.jwtService.validateVend(getTenant(), this.request.keyId, this.request.timeToLiveInSeconds);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  protected UUID askActionForTenantId() {
    return this.frontEndSupport.fusionAuthTenantId;
  }
}
