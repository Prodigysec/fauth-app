package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import io.fusionauth.api.service.ip.IPAccessControlListReaderService;
import io.fusionauth.api.service.ip.IPAccessControlListService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.IPAccessControlListRequest;
import io.fusionauth.domain.api.IPAccessControlListResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{ipAccessControlListId}", requiresAuthentication = true, scheme = {"api-no-tenant"})
public class IpAclAction extends BaseAPIAction implements Patchable {
  private final IPAccessControlListReaderService ipAccessControlListReader;
  
  private final IPAccessControlListService ipAccessControlListService;
  
  public UUID ipAccessControlListId;
  
  @JSONPatch
  @JSONRequest
  public IPAccessControlListRequest request = new IPAccessControlListRequest();
  
  @JSONResponse
  public IPAccessControlListResponse response;
  
  private IPAccessControlListService.ValidationResult result;
  
  @Inject
  public IpAclAction(FrontEndSupport paramFrontEndSupport, IPAccessControlListReaderService paramIPAccessControlListReaderService, IPAccessControlListService paramIPAccessControlListService) {
    super(paramFrontEndSupport);
    this.ipAccessControlListReader = paramIPAccessControlListReaderService;
    this.ipAccessControlListService = paramIPAccessControlListService;
  }
  
  public String delete() {
    if (this.result.existing == null)
      return "missing"; 
    this.ipAccessControlListService.delete(this.result.existing);
    return "success";
  }
  
  public String get() {
    this.response = new IPAccessControlListResponse(this.ipAccessControlListReader.retrieveById(this.ipAccessControlListId));
    if (this.response.ipAccessControlList == null)
      return "missing"; 
    return "render";
  }
  
  public void loadExisting() {
    if (this.ipAccessControlListId != null)
      this.request.ipAccessControlList = this.ipAccessControlListReader.retrieveById(this.ipAccessControlListId); 
  }
  
  public String post() {
    this.ipAccessControlListService.create(this.request.ipAccessControlList);
    this.response = new IPAccessControlListResponse(this.request.ipAccessControlList);
    return "render";
  }
  
  public String put() {
    if (this.result.existing == null)
      return "missing"; 
    this.ipAccessControlListService.update(this.result.existing, this.request.ipAccessControlList);
    this.response = new IPAccessControlListResponse(this.request.ipAccessControlList);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.ipAccessControlListId == null) {
      this.frontEndSupport.addFieldError("accessControlListId", "[missing]ipAccessControlListId", new Object[0]);
      return;
    } 
    this.result = this.ipAccessControlListService.validateDelete(this.ipAccessControlListId);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    if (this.ipAccessControlListId == null)
      this.frontEndSupport.addFieldError("ipAccessControlListId", "[missing]ipAccessControlListId", new Object[0]); 
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.request.ipAccessControlList == null) {
      this.frontEndSupport.addFieldError("ipAccessControlList", "[missing]ipAccessControlList", new Object[0]);
      return;
    } 
    this.request.ipAccessControlList.id = this.ipAccessControlListId;
    this.request.ipAccessControlList.normalize();
    this.result = this.ipAccessControlListService.validateCreate(this.request.ipAccessControlList);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"PUT", "PATCH"})
  public void validatePutAndPatch() {
    if (this.request.ipAccessControlList == null) {
      this.frontEndSupport.addFieldError("ipAccessControlList", "[missing]ipAccessControlList", new Object[0]);
      return;
    } 
    this.request.ipAccessControlList.id = this.ipAccessControlListId;
    this.request.ipAccessControlList.normalize();
    this.result = this.ipAccessControlListService.validateUpdate(this.request.ipAccessControlList);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
