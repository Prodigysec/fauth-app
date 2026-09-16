package io.fusionauth.app.action.ajax;

import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.Forward.List;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.action.result.annotation.JSON.List;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.action.result.annotation.Status.List;

@List({@Forward(code = "render", status = 200, cacheControl = "no-store"), @Forward(code = "input", status = 400, cacheControl = "no-store"), @Forward(code = "api-error", status = 400, cacheControl = "no-store"), @Forward(code = "missing", status = 404, cacheControl = "no-store")})
@List({@Status(code = "success", status = 200), @Status(code = "unauthenticated", status = 401), @Status(code = "unauthorized", status = 401), @Status(code = "not-allowed", status = 405), @Status(code = "content-length-required", status = 411), @Status(code = "bad-parameter", status = 420), @Status(code = "error", status = 500), @Status(code = "not-implemented", status = 501)})
@List({@JSON(code = "render-json", status = 200, cacheControl = "no-store"), @JSON(code = "render-input-json", status = 400, cacheControl = "no-store"), @JSON(code = "render-error-json", status = 500, cacheControl = "no-store")})
public abstract class BaseAJAXAction extends BaseAction {
  protected BaseAJAXAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
}
