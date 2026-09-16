package io.fusionauth.app.action.api.user;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.family.FamilyService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Family;
import io.fusionauth.domain.FamilyMember;
import io.fusionauth.domain.api.FamilyRequest;
import io.fusionauth.domain.api.FamilyResponse;
import java.util.Comparator;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{familyId}/{userId}", requiresAuthentication = true, scheme = {"api"})
public class FamilyAction extends BaseTenantAPIAction {
  @JSONRequest
  public final FamilyRequest request = new FamilyRequest();
  
  private final FamilyService familyService;
  
  public UUID familyId;
  
  @JSONResponse
  public FamilyResponse response;
  
  public UUID userId;
  
  private FamilyService.ValidationResult result;
  
  @Inject
  public FamilyAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, FamilyService paramFamilyService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.familyService = paramFamilyService;
  }
  
  public String delete() {
    if (!this.familyService.removeMember(getOptionalTenantId(), this.familyId, this.userId))
      return "missing"; 
    return "success";
  }
  
  public String get() {
    if (this.userId != null) {
      this.response = new FamilyResponse(this.familyService.retrieveByUserId(getOptionalTenantId(), this.userId));
      this.response.families.sort(Comparator.comparing(paramFamily -> paramFamily.id));
      this.response.families.forEach(paramFamily -> paramFamily.members.sort(Comparator.comparing(())));
    } else if (this.familyId != null) {
      this.response = new FamilyResponse(this.familyService.retrieveById(getOptionalTenantId(), this.familyId));
      if (this.response.family == null)
        return "missing"; 
    } 
    return "render";
  }
  
  public String post() {
    this.response = new FamilyResponse(this.familyService.upsertMember(this.familyId, this.result.family, this.result.user, this.request.familyMember));
    return "render";
  }
  
  public String put() {
    if (this.result.family == null)
      return "missing"; 
    this.response = new FamilyResponse(this.familyService.upsertMember(this.familyId, this.result.family, this.result.user, this.request.familyMember));
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.userId == null && this.familyId == null)
      this.frontEndSupport.addGeneralError("[invalid]", new Object[0]); 
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    if (this.userId == null && this.familyId == null)
      this.frontEndSupport.addGeneralError("[invalid]", new Object[0]); 
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.request.familyMember == null) {
      this.frontEndSupport.addFieldError("familyMember", "[missing]familyMember", new Object[0]);
    } else {
      this.result = this.familyService.validateCreate(getOptionalTenant(), this.familyId, this.request.familyMember);
      conditionallyUpdateTenant(this.result.tenant);
      this.frontEndSupport.transfer(this.result.errors);
    } 
  }
  
  @ValidationMethod(httpMethods = {"PUT"})
  public void validatePut() {
    if (this.request.familyMember == null) {
      this.frontEndSupport.addFieldError("familyMember", "[missing]familyMember", new Object[0]);
    } else {
      this.result = this.familyService.validateUpdate(getOptionalTenant(), this.familyId, this.request.familyMember);
      conditionallyUpdateTenant(this.result.tenant);
      this.frontEndSupport.transfer(this.result.errors);
    } 
  }
}
