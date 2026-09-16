package io.fusionauth.app.action.api.system;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import io.fusionauth.api.domain.SearchEngineType;
import io.fusionauth.api.service.reindex.ReindexService;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.ReindexRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class ReindexAction extends BaseAPIAction {
  @JSONRequest
  public final ReindexRequest request = new ReindexRequest();
  
  private final ReindexService reindexService;
  
  @Inject
  public ReindexAction(FrontEndSupport paramFrontEndSupport, ReindexService paramReindexService) {
    super(paramFrontEndSupport);
    this.reindexService = paramReindexService;
  }
  
  public String get() {
    return this.reindexService.inProgress() ? "accepted-status" : "missing";
  }
  
  public String post() {
    if (this.request.index.equals("fusionauth_user")) {
      this.reindexService.reindexUsers();
    } else {
      this.reindexService.reindexEntities();
    } 
    return "accepted-status";
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validate() {
    if (this.frontEndSupport.configuration.searchEngineType() == SearchEngineType.database) {
      this.frontEndSupport.addGeneralError("[invalid]", new Object[0]);
      return;
    } 
    if (this.frontEndSupport.isPOST()) {
      Errors errors = this.reindexService.validate(this.request.index);
      this.frontEndSupport.transfer(errors);
    } 
  }
}
