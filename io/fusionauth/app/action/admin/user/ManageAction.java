package io.fusionauth.app.action.admin.user;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.action.ajax.user.twoFactor.DisableAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.app.service.security.TwoFactorFrontendService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.client.LambdaDelegate;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Consent;
import io.fusionauth.domain.Family;
import io.fusionauth.domain.FamilyMember;
import io.fusionauth.domain.Group;
import io.fusionauth.domain.GroupMember;
import io.fusionauth.domain.IdentityProviderLink;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.UserActionLog;
import io.fusionauth.domain.UserComment;
import io.fusionauth.domain.UserConsent;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.WebAuthnCredential;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.ConsentResponse;
import io.fusionauth.domain.api.FamilyResponse;
import io.fusionauth.domain.api.GroupResponse;
import io.fusionauth.domain.api.IdentityProviderResponse;
import io.fusionauth.domain.api.TenantResponse;
import io.fusionauth.domain.api.UserActionResponse;
import io.fusionauth.domain.api.UserCommentResponse;
import io.fusionauth.domain.api.UserConsentResponse;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.api.WebAuthnCredentialResponse;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLinkResponse;
import io.fusionauth.domain.api.jwt.RefreshTokenResponse;
import io.fusionauth.domain.api.user.ActionResponse;
import io.fusionauth.domain.api.user.SearchRequest;
import io.fusionauth.domain.api.user.SearchResponse;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.search.UserSearchCriteria;
import io.fusionauth.http.Cookie;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.scope.annotation.ManagedCookie;

@Action(value = "{user.id}", requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager", "user_support_viewer"})
@List({@Redirect(code = "api-error", uri = "/admin/user/"), @Redirect(code = "missing", uri = "/admin/user/")})
public class ManageAction extends BaseAction {
  private final SSOService ssoService;
  
  private final TwoFactorFrontendService twoFactorFrontendService;
  
  public List<UserActionLog> actions = new ArrayList<>();
  
  public boolean actionsAvailable;
  
  public Map<UUID, Application> applications = new HashMap<>();
  
  public List<String> availableTwoFactorMethods = new ArrayList<>();
  
  public boolean canDisableMFAMethods;
  
  public boolean consentsAvailable;
  
  public List<Family> families;
  
  public Map<UUID, Group> groups = new HashMap<>();
  
  @FTLVariable
  public boolean hasUnverifiedIdentity;
  
  @FTLVariable
  public boolean hasVerifiableIdentity;
  
  public List<IdentityProviderLink> identityProviderLinks = new ArrayList<>();
  
  public Map<UUID, BaseIdentityProvider<?>> identityProviders = new HashMap<>();
  
  public boolean membershipsAvailable;
  
  public List<RefreshToken> refreshTokens;
  
  public boolean registrationsAvailable;
  
  @ManagedCookie(name = "fusionauth.sso", encrypt = false)
  public Cookie ssoCookie;
  
  public Tenant tenant;
  
  public UUID thisSSOSessionId;
  
  public String thisSession;
  
  public User user;
  
  public List<UserConsent> userConsents;
  
  public Map<UUID, User> users = new HashMap<>();
  
  public List<WebAuthnCredential> webAuthnCredentials = new ArrayList<>();
  
  @Inject
  public ManageAction(FrontEndSupport paramFrontEndSupport, SSOService paramSSOService, TwoFactorFrontendService paramTwoFactorFrontendService) {
    super(paramFrontEndSupport);
    this.ssoService = paramSSOService;
    this.twoFactorFrontendService = paramTwoFactorFrontendService;
  }
  
  public String get() {
    this.user = ((UserResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUser(this.user.id))).user;
    this.tenant = ((TenantResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTenant(this.user.tenantId))).tenant;
    List list1 = this.user.identities.stream().filter(UserIdentity::verificationRequired).toList();
    this.hasUnverifiedIdentity = !list1.isEmpty();
    this
      
      .hasVerifiableIdentity = ((this.tenant.emailConfiguration.verifyEmail && list1.stream().anyMatch(paramUserIdentity -> paramUserIdentity.type.is(IdentityType.email))) || (this.tenant.phoneConfiguration.verifyPhoneNumber && list1.stream().anyMatch(paramUserIdentity -> paramUserIdentity.type.is(IdentityType.phoneNumber))));
    List<Application> list2 = ((ApplicationResponse)this.delegate.execute(FusionAuthClient::retrieveApplications)).applications;
    if (list2 != null)
      list2.forEach(paramApplication -> this.applications.put(paramApplication.id, paramApplication)); 
    this

      
      .registrationsAvailable = !((Set)this.user.getRegistrations().stream().map(paramUserRegistration -> paramUserRegistration.applicationId).collect(Collectors.toSet())).containsAll(this.applications.keySet());
    List<Group> list = ((GroupResponse)this.delegate.execute(FusionAuthClient::retrieveGroups)).groups;
    if (list != null && !list.isEmpty()) {
      ((GroupResponse)this.delegate.execute(FusionAuthClient::retrieveGroups)).groups.forEach(paramGroup -> this.groups.put(paramGroup.id, paramGroup));
      this


        
        .membershipsAvailable = !((Set)this.user.getMemberships().stream().map(paramGroupMember -> paramGroupMember.groupId).collect(Collectors.toSet())).containsAll(this.groups.keySet());
    } 
    List<Application> list3 = ((ApplicationResponse)this.delegate.execute(FusionAuthClient::retrieveInactiveApplications)).applications;
    if (list3 != null)
      list3.forEach(paramApplication -> this.applications.put(paramApplication.id, paramApplication)); 
    List<UserActionLog> list4 = ((ActionResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveActions(this.user.id))).actions;
    if (list4 != null)
      this.actions.addAll(list4); 
    List<UserComment> list5 = ((UserCommentResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserComments(this.user.id))).userComments;
    if (list5 != null)
      this.actions.addAll((Collection<? extends UserActionLog>)list5.stream().map(UserComment::toUserActionLog).collect(Collectors.toList())); 
    this.actions.sort((paramUserActionLog1, paramUserActionLog2) -> paramUserActionLog2.insertInstant.compareTo(paramUserActionLog1.insertInstant));
    LambdaDelegate lambdaDelegate = superDelegate();
    this.actions.stream()
      .filter(paramUserActionLog -> (paramUserActionLog.actionerUserId != null))
      .map(paramUserActionLog -> paramUserActionLog.actionerUserId)
      .distinct()
      .forEach(paramUUID -> this.users.put(paramUUID, ((UserResponse)paramLambdaDelegate.execute(())).user));
    this.refreshTokens = ((RefreshTokenResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveRefreshTokens(this.user.id))).refreshTokens;
    Cookie cookie = this.frontEndSupport.getCookie("fusionauth.rt");
    if (cookie != null)
      this.thisSession = cookie.value; 
    SSOService.SSOSession sSOSession = this.ssoService.getSession(this.codeCurrentTenant, this.ssoCookie);
    this.thisSSOSessionId = (sSOSession.user != null) ? sSOSession.id : null;
    if (!this.user.getRegistrations().isEmpty()) {
      List<UserAction> list9 = ((UserActionResponse)superDelegate().execute(FusionAuthClient::retrieveUserActions)).userActions;
      this.actionsAvailable = (list9 != null && !list9.isEmpty());
    } 
    this.families = (List<Family>)Objects.requireNonNullElseGet(((FamilyResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveFamilies(this.user.id))).families, Collections::emptyList);
    List<?> list6 = this.families.stream().flatMap(paramFamily -> paramFamily.members.stream()).map(paramFamilyMember -> paramFamilyMember.userId).distinct().toList();
    this.userConsents = (List<UserConsent>)Objects.requireNonNullElseGet(((UserConsentResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserConsents(this.user.id))).userConsents, Collections::emptyList);
    if (list6.size() > 0 || this.userConsents.size() > 0) {
      Set set = (Set)Stream.concat(list6.stream(), this.userConsents.stream().map(paramUserConsent -> paramUserConsent.giverUserId)).collect(Collectors.toSet());
      ((SearchResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.searchUsersByQuery((new SearchRequest((new UserSearchCriteria()).with(()))).with(())))).users
        .forEach(paramUser -> this.users.put(paramUser.id, paramUser));
    } 
    List list7 = (List)Objects.requireNonNullElseGet(((ConsentResponse)this.delegate.execute(FusionAuthClient::retrieveConsents)).consents, Collections::emptyList);
    List list8 = this.userConsents.stream().map(paramUserConsent -> paramUserConsent.consentId).toList();
    list7.removeIf(paramConsent -> paramList.contains(paramConsent.id));
    this.consentsAvailable = (list7.size() > 0);
    this.availableTwoFactorMethods = this.twoFactorFrontendService.availableMethodsToConfigureForUser(this.tenants.get(this.user.tenantId), this.user);
    this.webAuthnCredentials = ((WebAuthnCredentialResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveWebAuthnCredentialsForUser(this.user.id))).credentials;
    this.identityProviderLinks = (List<IdentityProviderLink>)Objects.requireNonNullElseGet(((IdentityProviderLinkResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserLinksByUserId(null, this.user.id))).identityProviderLinks, Collections::emptyList);
    this.identityProviderLinks.stream().map(paramIdentityProviderLink -> paramIdentityProviderLink.identityProviderId).forEach(paramUUID -> this.identityProviders.put(paramUUID, ((IdentityProviderResponse)paramLambdaDelegate.execute(())).identityProvider));
    ManageAction manageAction = this;
    this.canDisableMFAMethods = DisableAction.canRemoveMFAMethod(this.user.id, this.codeCurrentUser, paramString -> Boolean.valueOf(paramManageAction.hasRole(new String[] { paramString })));
    return "input";
  }
}
