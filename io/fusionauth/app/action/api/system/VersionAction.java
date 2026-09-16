package io.fusionauth.app.action.api.system;

import com.google.inject.Inject;
import com.inversoft.support.service.guice.ProductVersionString;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.VersionResponse;
import io.fusionauth.jwt.domain.JWT;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.security.annotation.JWTAuthorizeMethod;

@Action(requiresAuthentication = true, scheme = {"api", "api-basic-auth", "scoped-jwt", "user"}, constraints = {"admin", "system_manager"})
public class VersionAction extends BaseAPIAction {
  private final String productVersion;
  
  @JSONResponse
  public VersionResponse response;
  
  @Inject
  public VersionAction(FrontEndSupport paramFrontEndSupport, @ProductVersionString String paramString) {
    super(paramFrontEndSupport);
    this.productVersion = paramString;
  }
  
  @JWTAuthorizeMethod
  public boolean authorizeJWT(JWT paramJWT) {
    return true;
  }
  
  public String get() {
    this.response = new VersionResponse(this.productVersion);
    return "render";
  }
}
