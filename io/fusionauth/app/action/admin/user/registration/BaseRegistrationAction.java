package io.fusionauth.app.action.admin.user.registration;

import com.inversoft.cache.Cache;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.CachedTheme;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.action.BaseThemedAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.user.CustomFormFrontendService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.Group;
import io.fusionauth.domain.GroupMember;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Theme;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.GroupResponse;
import io.fusionauth.domain.form.FormField;
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

public abstract class BaseRegistrationAction extends BaseAction {
  protected final CustomFormFrontendService customFormFrontendService;
  
  private final Cache<UUID, CachedTheme> themeCache;
  
  @FTLVariable
  public Application application;
  
  @FTLVariable
  public UUID applicationId;
  
  @FTLVariable
  public boolean canEditRoles = true;
  
  @FTLVariable
  public CustomFormFrontendService.ConfirmUserRegistration confirm = new CustomFormFrontendService.ConfirmUserRegistration();
  
  @FTLVariable
  public Map<Integer, List<FormField>> fields = new HashMap<>();
  
  public boolean groupManagedRoles;
  
  public List<String> groupNames = Collections.emptyList();
  
  public BaseThemedAction.LocaleResolvedCachedTheme theme;
  
  public UUID themeId;
  
  @FTLVariable
  public User user;
  
  protected BaseRegistrationAction(CustomFormFrontendService paramCustomFormFrontendService, FrontEndSupport paramFrontEndSupport, Cache<UUID, CachedTheme> paramCache) {
    super(paramFrontEndSupport);
    this.customFormFrontendService = paramCustomFormFrontendService;
    this.themeCache = paramCache;
  }
  
  protected void checkForGroupManagedRoles(User paramUser, UserRegistration paramUserRegistration) {
    if (!paramUser.getMemberships().isEmpty()) {
      List<Group> list = ((GroupResponse)this.delegate.execute(FusionAuthClient::retrieveGroups)).groups;
      Set set1 = (Set)paramUser.getMemberships().stream().map(paramGroupMember -> paramGroupMember.groupId).collect(Collectors.toSet());
      list.removeIf(paramGroup -> !paramSet.contains(paramGroup.id));
      Application application = ((ApplicationResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveApplication(paramUserRegistration.applicationId))).application;
      Set set2 = (Set)application.roles.stream().map(paramApplicationRole -> paramApplicationRole.id).collect(Collectors.toSet());
      this.groupNames = (List<String>)list.stream().map(paramGroup -> paramGroup.name).collect(Collectors.toList());
      Set set3 = (Set)list.stream().flatMap(paramGroup -> paramGroup.roles.values().stream().flatMap(Collection::stream).map(())).collect(Collectors.toSet());
      Objects.requireNonNull(set3);
      this.groupManagedRoles = set2.stream().anyMatch(set3::contains);
    } 
  }
  
  protected void loadTheme() {
    CachedTheme cachedTheme = (CachedTheme)this.themeCache.get(((Tenant)this.tenants.get(this.tenantId)).themeId);
    this.theme = new BaseThemedAction.LocaleResolvedCachedTheme(this.locale, cachedTheme, ((CachedTheme)this.themeCache.get(Theme.FUSIONAUTH_THEME_ID)).defaultProperties, this.frontEndSupport.messageProvider);
    this.themeId = this.theme.id;
  }
  
  protected void setRegistrationRolesField() {
    List list = (List)this.application.roles.stream().map(paramApplicationRole -> paramApplicationRole.name).collect(Collectors.toList());
    this.fields.values().stream().flatMap(Collection::stream).filter(paramFormField -> paramFormField.key.equals("registration.roles")).forEach(paramFormField -> paramFormField.options = paramList);
  }
  
  protected void sortRoles() {
    this.application.roles.sort((paramApplicationRole1, paramApplicationRole2) -> (paramApplicationRole1.isSuperRole == paramApplicationRole2.isSuperRole) ? paramApplicationRole1.getDisplay().compareTo(paramApplicationRole2.getDisplay()) : (paramApplicationRole1.isSuperRole ? 0 : 1));
  }
}
