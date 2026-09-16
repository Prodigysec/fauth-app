package io.fusionauth.app.action.api.report;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.LoginMapper;
import io.fusionauth.api.service.count.RegistrationCountService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.api.report.TotalsReportResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class TotalsAction extends BaseAPIAction {
  private final String ApplicationTotals = "applicationTotals";
  
  private final Set<String> AllExcludeTypes = Set.of("applicationTotals");
  
  private final ApplicationReaderService applicationReader;
  
  private final LoginMapper loginMapper;
  
  private final RegistrationCountService registrationCountService;
  
  public List<String> excludes = new ArrayList<>();
  
  @JSONResponse
  public TotalsReportResponse response = new TotalsReportResponse();
  
  @Inject
  public TotalsAction(FrontEndSupport paramFrontEndSupport, ApplicationReaderService paramApplicationReaderService, LoginMapper paramLoginMapper, RegistrationCountService paramRegistrationCountService) {
    super(paramFrontEndSupport);
    this.applicationReader = paramApplicationReaderService;
    this.loginMapper = paramLoginMapper;
    this.registrationCountService = paramRegistrationCountService;
  }
  
  public String get() {
    if (this.excludes.isEmpty() || !this.excludes.contains("applicationTotals")) {
      List<Application> list = this.applicationReader.retrieveAll(null, Collections.emptySet());
      list.forEach(paramApplication -> this.response.applicationTotals.put(paramApplication.id, new TotalsReportResponse.Totals(this.loginMapper.retrieveApplicationTotal(paramApplication.id), this.registrationCountService.retrieveApplicationCurrentTotal(paramApplication.id), this.registrationCountService.retrieveApplicationTotal(paramApplication.id))));
    } else {
      this.response.applicationTotals = null;
    } 
    this.response.globalRegistrations = this.registrationCountService.retrieveGlobalCurrentTotal();
    this.response.totalGlobalRegistrations = this.registrationCountService.retrieveGlobalTotal();
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validate() {
    Errors errors = (new Validator()).forEach(this.excludes, (paramValidator, paramString, paramInteger) -> paramValidator.ensureWithCode(this.AllExcludeTypes.contains(paramString), "excludes[" + paramInteger + "]", "[invalid]excludes", new Object[] { paramString, String.join(", ", (Iterable)this.AllExcludeTypes) })).done();
    this.frontEndSupport.transfer(errors);
  }
}
