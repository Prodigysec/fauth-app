package io.fusionauth.app.action.admin.theme;

import com.inversoft.cache.Cache;
import com.inversoft.error.Error;
import io.fusionauth.api.domain.CachedTheme;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Theme;
import java.util.Set;
import java.util.UUID;
import org.primeframework.mvc.parameter.annotation.PreParameterMethod;

public abstract class BaseFormAction extends BaseAction {
  public Set<String> defaultMessageKeys;
  
  public Theme theme = new Theme();
  
  public UUID themeId;
  
  protected BaseFormAction(FrontEndSupport paramFrontEndSupport, Cache<UUID, CachedTheme> paramCache) {
    super(paramFrontEndSupport);
    paramFrontEndSupport.errorMapperFunction = BaseFormAction::mapError;
    paramFrontEndSupport.fieldMapperFunction = (paramString -> paramString);
    CachedTheme cachedTheme = (CachedTheme)paramCache.get(Theme.FUSIONAUTH_THEME_ID);
    this.defaultMessageKeys = cachedTheme.defaultProperties.stringPropertyNames();
  }
  
  private static Error mapError(Error paramError) {
    boolean bool = Theme.Templates.Names.stream().map(paramString -> "[invalid]theme.templates." + paramString).anyMatch(paramString -> paramError.code.equals(paramString));
    if (bool) {
      int i = paramError.message.indexOf('[', 60);
      paramError.values = (Object[])new String[] { paramError.message.substring(i + 1, paramError.message.length() - 2) };
    } 
    return paramError;
  }
  
  @PreParameterMethod
  public void addXSSProtectionHeader() {
    this.frontEndSupport.response.setHeader("X-XSS-Protection", "0");
  }
}
