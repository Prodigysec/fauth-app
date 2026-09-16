package io.fusionauth.app.action.legacy.wellKnown;

import com.google.inject.Inject;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.security.KeyCache;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.domain.oauth2.JWKSResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.action.result.annotation.JSON.List;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action
@List({@JSON(code = "render", status = 200), @JSON(code = "input", status = 400)})
@Status(code = "disabled", status = 404)
public class JwksAction extends BaseLegacyWellKnownAction {
  private final KeyCache keyCache;
  
  @JSONResponse(prettyPrint = true)
  public Object response;
  
  @Inject
  public JwksAction(FusionAuthConfiguration paramFusionAuthConfiguration, KeyCache paramKeyCache, ReactorStatusService paramReactorStatusService) {
    super(paramFusionAuthConfiguration, paramReactorStatusService);
    this.keyCache = paramKeyCache;
  }
  
  public String get() {
    if (isDisabled())
      return "disabled"; 
    if (isNotLicensed()) {
      this.response = buildNotLicensedError();
      return "input";
    } 
    this.response = new JWKSResponse(this.keyCache.getWebKeys());
    return "render";
  }
}
