package io.fusionauth.app.action.api.twoFactor;

import com.google.inject.Inject;
import io.fusionauth.api.util.EncoderTools;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.twoFactor.SecretResponse;
import io.fusionauth.twofactor.TwoFactor;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api", "scoped-jwt"})
public class SecretAction extends BaseAPIAction {
  @JSONResponse
  public SecretResponse response;
  
  @Inject
  public SecretAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    String str = TwoFactor.generateBase64EncodedSecret();
    this.response = new SecretResponse(str, EncoderTools.Base64.toBase32(str));
    return "render";
  }
}
