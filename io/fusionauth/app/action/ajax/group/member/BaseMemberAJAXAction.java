package io.fusionauth.app.action.ajax.group.member;

import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.IllegalTenantViolationException;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Group;
import io.fusionauth.domain.GroupMember;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.GroupResponse;
import io.fusionauth.domain.api.user.SearchRequest;
import io.fusionauth.domain.api.user.SearchResponse;
import io.fusionauth.domain.search.UserSearchCriteria;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;

public abstract class BaseMemberAJAXAction extends BaseAJAXAction {
  @FTLVariable
  public boolean bulkManagement;
  
  public Group group;
  
  public UUID groupId;
  
  public List<Group> groups;
  
  @JSONRequest
  public Request request;
  
  public List<UUID> userId;
  
  public List<User> users;
  
  protected BaseMemberAJAXAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  @PostParameterMethod
  public void loadUsers() {
    this.groups = ((GroupResponse)this.delegate.execute(FusionAuthClient::retrieveGroups)).groups;
    if (this.request != null && !this.request.userIds.isEmpty()) {
      this.users = ((SearchResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.searchUsersByQuery(new SearchRequest((new UserSearchCriteria()).with(()))))).users;
    } else if (this.userId != null && !this.userId.isEmpty()) {
      this.users = ((SearchResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.searchUsersByQuery(new SearchRequest((new UserSearchCriteria()).with(()))))).users;
    } 
    if (this.groups != null) {
      this.group = this.groups.stream().filter(paramGroup -> paramGroup.id.equals(this.groupId)).findFirst().orElse(null);
      this.groups.sort(Comparator.comparing(paramGroup -> paramGroup.name));
    } 
    if (this.users != null && this.users.size() == 1 && this.groups != null) {
      Set set = (Set)((User)this.users.get(0)).getMemberships().stream().map(paramGroupMember -> paramGroupMember.groupId).collect(Collectors.toSet());
      this.groups.removeIf(paramGroup -> paramSet.contains(paramGroup.id));
    } 
    Set set1 = (this.users != null) ? (Set)this.users.stream().map(paramUser -> paramUser.tenantId).collect(Collectors.toSet()) : Collections.emptySet();
    if (set1.size() == 1 && this.groups != null)
      this.groups.removeIf(paramGroup -> !paramGroup.tenantId.equals(paramSet.iterator().next())); 
    Set set2 = (this.groups != null) ? (Set)this.groups.stream().map(paramGroup -> paramGroup.tenantId).collect(Collectors.toSet()) : Collections.emptySet();
    if (set2.size() > 1 || set1.size() > 1) {
      this.groups = Collections.emptyList();
      throw new IllegalTenantViolationException();
    } 
    if (set2.size() == 1 && set1.size() == 1 && !set2.equals(set1)) {
      this.groups = Collections.emptyList();
      throw new IllegalTenantViolationException();
    } 
  }
  
  @ValidationMethod
  public void validate() {
    if (this.request == null && this.groupId == null) {
      this.frontEndSupport.addFieldError("groupId", "[missing]groupId", new Object[0]);
    } else if (doesNotHaveRole(new String[] { "admin", "user_manager" })) {
      this.group = ((GroupResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveGroup(this.groupId))).group;
      if (this.group != null && this.group.roles.containsKey(Application.FUSIONAUTH_APP_ID))
        this.frontEndSupport.addFieldError("groupId", "[unauthorized]groupId", new Object[0]); 
    } 
  }
  
  public static class Request {
    public List<UUID> userIds = new ArrayList<>();
  }
}
