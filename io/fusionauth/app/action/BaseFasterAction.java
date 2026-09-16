package io.fusionauth.app.action;

import java.util.UUID;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.action.result.annotation.Status.List;

@List({@Status(code = "not-allowed", status = 405), @Status(code = "not-implemented", status = 501)})
public abstract class BaseFasterAction {
  public UUID tenantId;
}
