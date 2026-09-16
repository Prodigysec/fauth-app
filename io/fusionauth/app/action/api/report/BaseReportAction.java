package io.fusionauth.app.action.api.report;

import com.inversoft.validator.Validator;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import org.primeframework.mvc.validation.ValidationMethod;

public abstract class BaseReportAction extends BaseAPIAction {
  public UUID applicationId;
  
  public ZonedDateTime end;
  
  public ZonedDateTime start;
  
  protected BaseReportAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validate() {
    Objects.requireNonNull(this.frontEndSupport);
    (new Validator()).notMissing(this.end, "end", new Object[0]).notMissing(this.start, "start", new Object[0]).done(this.frontEndSupport::transfer);
  }
}
