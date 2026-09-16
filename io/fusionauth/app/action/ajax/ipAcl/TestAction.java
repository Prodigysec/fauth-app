package io.fusionauth.app.action.ajax.ipAcl;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.IPAccessControlList;
import io.fusionauth.domain.api.IPAccessControlListResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{ipAccessControlListId}", requiresAuthentication = true, constraints = {"admin", "acl_manager"})
public class TestAction extends BaseAJAXAction {
  private final ThreatDetectionService threatDetectionService;
  
  public Boolean blocked;
  
  public IPAccessControlList ipAccessControlList;
  
  public UUID ipAccessControlListId;
  
  public String testIPAddress;
  
  @Inject
  protected TestAction(FrontEndSupport paramFrontEndSupport, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport);
    this.threatDetectionService = paramThreatDetectionService;
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    this.blocked = Boolean.valueOf(this.threatDetectionService.isIPAddressBlocked(this.ipAccessControlListId, this.testIPAddress));
    return "input";
  }
  
  @FormPrepareMethod
  public void prepare() {
    this.ipAccessControlList = ((IPAccessControlListResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveIPAccessControlList(this.ipAccessControlListId))).ipAccessControlList;
  }
  
  @ValidationMethod
  public void validate() {
    Errors errors = (new Validator()).notBlank(this.testIPAddress, "testIPAddress", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.ipv4(this.testIPAddress, "testIPAddress", new Object[0])).done();
    this.frontEndSupport.transfer(errors);
  }
}
