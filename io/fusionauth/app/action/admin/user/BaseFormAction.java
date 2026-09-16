package io.fusionauth.app.action.admin.user;

import com.inversoft.cache.Cache;
import com.inversoft.error.Errors;
import io.fusionauth.api.domain.CachedTheme;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.action.BaseThemedAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.user.CustomFormFrontendService;
import io.fusionauth.domain.PasswordValidationRules;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Theme;
import io.fusionauth.domain.User;
import io.fusionauth.domain.form.FormField;
import io.fusionauth.domain.form.ManagedFields;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;

public abstract class BaseFormAction extends BaseAction {
  protected final CustomFormFrontendService customFormFrontendService;
  
  private final Cache<UUID, CachedTheme> themeCache;
  
  @FTLVariable
  public boolean allowSubClaimOverride;
  
  @FTLVariable
  public CustomFormFrontendService.ConfirmUser confirm = new CustomFormFrontendService.ConfirmUser();
  
  @FTLVariable
  public Map<Integer, List<FormField>> fields = new HashMap<>();
  
  @FTLVariable
  public Set<String> managedFields = ManagedFields.Values
    .keySet();
  
  public PasswordValidationRules passwordValidationRules;
  
  @FTLVariable
  public boolean showLegacyIdentifierField;
  
  public BaseThemedAction.LocaleResolvedCachedTheme theme;
  
  public UUID themeId;
  
  public User user = new User();
  
  protected BaseFormAction(CustomFormFrontendService paramCustomFormFrontendService, FrontEndSupport paramFrontEndSupport, Cache<UUID, CachedTheme> paramCache) {
    super(paramFrontEndSupport);
    this.customFormFrontendService = paramCustomFormFrontendService;
    this.themeCache = paramCache;
    paramFrontEndSupport.customPreTransferErrorConsumer = (paramErrors -> {
        UUID uUID = ((Tenant)this.tenants.get(this.tenantId)).formConfiguration.adminUserFormId;
        Map<Integer, List<FormField>> map = paramCustomFormFrontendService.retrieveFieldsByFormId(uUID);
        if (map.values().stream().flatMap(Collection::stream).noneMatch(()))
          paramFrontEndSupport.moveFieldErrorToGeneral(paramErrors, "user.parentEmail", "[blank]user.parentEmail", "[ParentEmailRequired]"); 
      });
  }
  
  @FormPrepareMethod
  public void formPrepare() {
    this.allowSubClaimOverride = this.frontEndSupport.configuration.allowSubClaimOverride();
    this.showLegacyIdentifierField = (this.allowSubClaimOverride && ReactorStatusValidator.isLicensedFor(this.reactorStatus, paramReactorStatus -> paramReactorStatus.legacyAdapter));
    if (this.tenants.size() == 1)
      this.tenantId = this.codeCurrentUser.tenantId; 
    if (this.tenantId != null) {
      Tenant tenant = this.tenants.get(this.tenantId);
      this.fields = this.customFormFrontendService.retrieveFieldsByFormId(tenant.formConfiguration.adminUserFormId);
      CachedTheme cachedTheme = (CachedTheme)this.themeCache.get(tenant.themeId);
      this.theme = new BaseThemedAction.LocaleResolvedCachedTheme(this.locale, cachedTheme, ((CachedTheme)this.themeCache.get(Theme.FUSIONAUTH_THEME_ID)).defaultProperties, this.frontEndSupport.messageProvider);
      this.themeId = this.theme.id;
    } 
  }
}
