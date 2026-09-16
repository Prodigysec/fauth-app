package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.group.GroupReaderService;
import io.fusionauth.api.service.group.GroupService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.Group;
import io.fusionauth.domain.api.GroupRequest;
import io.fusionauth.domain.api.GroupResponse;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{groupId}", requiresAuthentication = true, scheme = {"api"})
public class GroupAction extends BaseTenantAPIAction implements Patchable {
  private final GroupReaderService groupReader;
  
  private final GroupService groupService;
  
  @PreParameter
  public UUID groupId;
  
  @JSONPatch
  @JSONRequest
  public GroupRequest request = new GroupRequest();
  
  @JSONResponse
  public GroupResponse response;
  
  private GroupService.ValidationResult result;
  
  @Inject
  public GroupAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, GroupReaderService paramGroupReaderService, GroupService paramGroupService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.groupReader = paramGroupReaderService;
    this.groupService = paramGroupService;
  }
  
  public String delete() {
    if (this.result.existing == null)
      return "missing"; 
    this.groupService.delete(this.result.tenant, this.result.existing, this.frontEndSupport.buildEventInfo());
    return "success";
  }
  
  public String get() {
    if (this.groupId != null && this.result.group == null)
      return "missing"; 
    if (this.groupId == null) {
      this.response = new GroupResponse(this.groupReader.retrieveAll(getOptionalTenantId()));
      this.response.groups.forEach(Group::sort);
    } else {
      this.response = new GroupResponse(this.result.group);
      this.response.group.sort();
    } 
    return "render";
  }
  
  public void loadExisting() {
    if (this.groupId != null) {
      this.request.group = this.groupReader.retrieveById(getOptionalTenantId(), this.groupId);
      if (this.request.group != null) {
        this.request.group.sort();
        this.request.roleIds = (List<UUID>)this.request.group.roles.values().stream().flatMap(Collection::stream).map(paramApplicationRole -> paramApplicationRole.id).collect(Collectors.toList());
      } 
    } 
  }
  
  public String post() {
    this.groupService.create(getTenant(), this.request.group, this.result.roles, true, this.frontEndSupport.buildEventInfo());
    this.response = new GroupResponse(this.request.group.sort());
    return "render";
  }
  
  public String put() {
    if (this.result.existing == null)
      return "missing"; 
    this.groupService.update(this.result.tenant, this.result.existing, this.request.group, this.result.roles, true, this.frontEndSupport.buildEventInfo());
    this.response = new GroupResponse(this.request.group.sort());
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.groupId == null) {
      this.frontEndSupport.addFieldError("groupId", "[missing]groupId", new Object[0]);
      return;
    } 
    this.result = this.groupService.validateDelete(getOptionalTenant(), this.groupId);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    this.result = this.groupService.validateRetrieve(getOptionalTenant(), this.groupId);
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.request.group == null) {
      this.frontEndSupport.addFieldError("group", "[missing]group", new Object[0]);
      return;
    } 
    this.request.group.id = this.groupId;
    this.result = this.groupService.validateCreate(getTenant(), this.request.group, this.request.roleIds);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"PUT", "PATCH"})
  public void validatePutAndPatch() {
    if (this.groupId == null) {
      this.frontEndSupport.addFieldError("groupId", "[missing]groupId", new Object[0]);
      return;
    } 
    if (this.request.group == null) {
      this.frontEndSupport.addFieldError("group", "[missing]group", new Object[0]);
      return;
    } 
    this.request.group.id = this.groupId;
    this.result = this.groupService.validateUpdate(getOptionalTenant(), this.request.group, this.request.roleIds);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
