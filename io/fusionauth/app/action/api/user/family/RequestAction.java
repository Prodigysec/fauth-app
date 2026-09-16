package io.fusionauth.app.action.api.user.family;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.family.FamilyService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.FamilyEmailRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api"})
public class RequestAction extends BaseTenantAPIAction {
  @JSONRequest
  public final FamilyEmailRequest request = new FamilyEmailRequest();
  
  private final FamilyService familyService;
  
  @Inject
  public RequestAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, FamilyService paramFamilyService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.familyService = paramFamilyService;
  }
  
  public String post() {
    this.familyService.sendFamilyRequestEmail(getTenant(), this.request.parentEmail);
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    this.frontEndSupport.transfer((new Validator()).notBlank(this.request.parentEmail, "parentEmail", new Object[0]).done());
  }
}
