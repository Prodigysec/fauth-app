package io.fusionauth.app.action.api.group;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.group.GroupService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.GroupMember;
import io.fusionauth.domain.api.MemberDeleteRequest;
import io.fusionauth.domain.api.MemberRequest;
import io.fusionauth.domain.api.MemberResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{memberId}", requiresAuthentication = true, scheme = {"api"})
public class MemberAction extends BaseTenantAPIAction {
  @JSONRequest(httpMethods = {"DELETE"})
  public final MemberDeleteRequest deleteRequest = new MemberDeleteRequest();
  
  @JSONRequest(httpMethods = {"POST", "PUT"})
  public final MemberRequest request = new MemberRequest();
  
  private final GroupService groupService;
  
  public UUID groupId;
  
  public UUID memberId;
  
  @JSONResponse
  public MemberResponse response;
  
  public UUID userId;
  
  private GroupService.ValidationResult result;
  
  @Inject
  public MemberAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, GroupService paramGroupService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.groupService = paramGroupService;
  }
  
  public String delete() {
    if (this.memberId != null) {
      if (this.result.member == null)
        return "missing"; 
      this.groupService.removeMember(this.result.tenant, this.result.group, this.result.member, this.frontEndSupport.buildEventInfo());
      return "success";
    } 
    if (this.userId != null && this.groupId != null) {
      if (this.result.member == null)
        return "missing"; 
      this.groupService.removeMember(this.result.tenant, this.result.group, this.result.member, this.frontEndSupport.buildEventInfo());
      return "success";
    } 
    if (this.groupId != null) {
      if (this.result.group == null)
        return "missing"; 
      this.groupService.removeAllMembers(this.result.tenant, this.result.group, this.frontEndSupport.buildEventInfo());
      return "success";
    } 
    this.groupService.removeMembers(this.result.tenants, this.result.groups, this.result.users, this.result.members, this.frontEndSupport.buildEventInfo());
    return "success";
  }
  
  public String post() {
    this.groupService.addMembers(this.result.tenants, this.result.groups, this.request.members, this.result.users, true, this.frontEndSupport.buildEventInfo());
    this.request.members.values().forEach(paramList -> paramList.forEach(()));
    this.response = new MemberResponse(this.request.members);
    return "render";
  }
  
  public String put() {
    this.groupService.updateMembers(this.result.tenants, this.result.groups, this.result.users, this.request.members, true, this.frontEndSupport.buildEventInfo());
    this.request.members.values().forEach(paramList -> paramList.forEach(()));
    this.response = new MemberResponse(this.request.members);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"POST", "PUT"})
  public void validate() {
    if (this.request.members == null || this.request.members.isEmpty()) {
      this.frontEndSupport.addFieldError("members", "[missing]members", new Object[0]);
      return;
    } 
    normalizeMap(this.request.members);
    this.result = this.groupService.validateAddOrUpdateMembers(getOptionalTenant(), this.request.members);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.deleteRequest.members != null) {
      normalizeMap(this.deleteRequest.members);
      this.result = this.groupService.validateRemoveMembers(getOptionalTenant(), this.deleteRequest.members);
    } else if (this.memberId != null) {
      this.result = this.groupService.validateRemoveMemberById(getOptionalTenant(), this.memberId);
    } else if (this.userId != null && this.groupId != null) {
      this.result = this.groupService.validateRemoveMemberByUserAndGroup(getOptionalTenant(), this.groupId, this.userId);
    } else if (this.groupId != null) {
      this.result = this.groupService.validateRemoveMembersByGroupId(getOptionalTenant(), this.groupId);
    } else {
      if (this.deleteRequest.memberIds == null) {
        this.frontEndSupport.addFieldError("memberId", "[missing]memberId", new Object[0]);
        return;
      } 
      this.result = this.groupService.validateRemoveMembersByIds(getOptionalTenant(), this.deleteRequest.memberIds);
    } 
    if (this.result != null)
      this.frontEndSupport.transfer(this.result.errors); 
  }
  
  private <T, U> void normalizeMap(Map<T, List<U>> paramMap) {
    paramMap.entrySet()
      .removeIf(paramEntry -> (paramEntry.getValue() == null));
  }
}
