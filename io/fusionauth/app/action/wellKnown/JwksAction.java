package io.fusionauth.app.action.wellKnown;

import com.google.inject.Inject;
import io.fusionauth.api.security.KeyCache;
import io.fusionauth.app.action.BaseFasterAction;
import io.fusionauth.domain.oauth2.JWKSResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action
@JSON(code = "render", status = 200)
public class JwksAction extends BaseFasterAction {
  private final KeyCache keyCache;
  
  @JSONResponse(prettyPrint = true)
  public JWKSResponse response;
  
  @Inject
  public JwksAction(KeyCache paramKeyCache) {
    this.keyCache = paramKeyCache;
  }
  
  public String get() {
    this.response = new JWKSResponse(this.keyCache.getWebKeys());
    return "render";
  }
}
