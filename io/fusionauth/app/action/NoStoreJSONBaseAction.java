package io.fusionauth.app.action;

import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.action.result.annotation.JSON.List;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.action.result.annotation.Status.List;

@List({@JSON(code = "render", status = 200, cacheControl = "no-store"), @JSON(code = "input", status = 400, cacheControl = "no-store"), @JSON(code = "invalid-client", status = 401, cacheControl = "no-store"), @JSON(code = "error", status = 500, cacheControl = "no-store")})
@List({@Status(code = "not-found", status = 404), @Status(code = "success", status = 200), @Status(code = "unauthorized", status = 401), @Status(code = "not-allowed", status = 405)})
public abstract class NoStoreJSONBaseAction {}
