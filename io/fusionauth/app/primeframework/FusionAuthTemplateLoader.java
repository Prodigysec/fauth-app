package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import com.inversoft.cache.Cache;
import io.fusionauth.api.domain.CachedTheme;
import io.fusionauth.domain.Theme;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;
import org.primeframework.mvc.container.ContainerResolver;
import org.primeframework.mvc.freemarker.OverridingTemplateLoader;

public class FusionAuthTemplateLoader extends OverridingTemplateLoader {
  private static final Set<String> databaseTemplates = Set.of((Object[])new String[] { 
        "templates/account/edit.ftl", "templates/account/index.ftl", "templates/account/two-factor/disable.ftl", "templates/account/two-factor/edit.ftl", "templates/account/two-factor/enable.ftl", "templates/account/two-factor/index.ftl", "templates/account/webauthn/add.ftl", "templates/account/webauthn/delete.ftl", "templates/account/webauthn/index.ftl", "templates/confirmation-required.ftl", 
        "templates/email/complete.ftl", "templates/email/sent.ftl", "templates/email/verification-required.ftl", "templates/email/verify.ftl", "templates/index.ftl", "templates/oauth2/authorize.ftl", "templates/oauth2/authorized-not-registered.ftl", "templates/oauth2/child-registration-not-allowed.ftl", "templates/oauth2/child-registration-not-allowed-complete.ftl", "templates/oauth2/complete-registration.ftl", 
        "templates/oauth2/consent.ftl", "templates/oauth2/device.ftl", "templates/oauth2/device-complete.ftl", "templates/oauth2/error.ftl", "templates/oauth2/logout.ftl", "templates/oauth2/passwordless.ftl", "templates/oauth2/register.ftl", "templates/oauth2/start-idp-link.ftl", "templates/oauth2/two-factor.ftl", "templates/oauth2/two-factor-enable.ftl", 
        "templates/oauth2/two-factor-enable-complete.ftl", "templates/oauth2/two-factor-methods.ftl", "templates/oauth2/wait.ftl", "templates/oauth2/webauthn.ftl", "templates/oauth2/webauthn-reauth.ftl", "templates/oauth2/webauthn-reauth-enable.ftl", "templates/password/change.ftl", "templates/password/complete.ftl", "templates/password/forgot.ftl", "templates/password/sent.ftl", 
        "templates/phone/complete.ftl", "templates/phone/sent.ftl", "templates/phone/verification-required.ftl", "templates/phone/verify.ftl", "templates/registration/complete.ftl", "templates/registration/sent.ftl", "templates/registration/verification-required.ftl", "templates/registration/verify.ftl", "templates/samlv2/logout.ftl", "templates/unauthorized.ftl", 
        "templates/_helpers.ftl" });
  
  private final Cache<UUID, CachedTheme> themeCache;
  
  @Inject
  public FusionAuthTemplateLoader(Cache<UUID, CachedTheme> paramCache, ContainerResolver paramContainerResolver) {
    super(paramContainerResolver);
    this.themeCache = paramCache;
  }
  
  public void closeTemplateSource(Object paramObject) throws IOException {
    if (paramObject instanceof FusionAuthUITemplate)
      return; 
    super.closeTemplateSource(paramObject);
  }
  
  public Object findTemplateSource(String paramString) throws IOException {
    ThemedTemplate themedTemplate = resolveTemplate(paramString);
    if (themedTemplate.themeable) {
      boolean bool = (themedTemplate.theme == null || themedTemplate.theme.id.equals(Theme.FUSIONAUTH_THEME_ID)) ? true : false;
      if (bool)
        return super.findTemplateSource(themedTemplate.name); 
      Theme.Templates templates = themedTemplate.theme.templates;
      ZonedDateTime zonedDateTime = themedTemplate.theme.lastUpdateInstant;
      switch (themedTemplate.name) {
        case "templates/account/edit.ftl":
        
        case "templates/account/index.ftl":
        
        case "templates/account/two-factor/disable.ftl":
        
        case "templates/account/two-factor/edit.ftl":
        
        case "templates/account/two-factor/enable.ftl":
        
        case "templates/account/two-factor/index.ftl":
        
        case "templates/account/webauthn/add.ftl":
        
        case "templates/account/webauthn/delete.ftl":
        
        case "templates/account/webauthn/index.ftl":
        
        case "templates/confirmation-required.ftl":
        
        case "templates/email/complete.ftl":
        
        case "templates/email/sent.ftl":
        
        case "templates/email/verification-required.ftl":
        
        case "templates/email/verify.ftl":
        
        case "templates/index.ftl":
        
        case "templates/oauth2/authorize.ftl":
        
        case "templates/oauth2/authorized-not-registered.ftl":
        
        case "templates/oauth2/child-registration-not-allowed.ftl":
        
        case "templates/oauth2/child-registration-not-allowed-complete.ftl":
        
        case "templates/oauth2/complete-registration.ftl":
        
        case "templates/oauth2/consent.ftl":
        
        case "templates/oauth2/device.ftl":
        
        case "templates/oauth2/device-complete.ftl":
        
        case "templates/oauth2/error.ftl":
        
        case "templates/oauth2/logout.ftl":
        
        case "templates/oauth2/passwordless.ftl":
        
        case "templates/oauth2/register.ftl":
        
        case "templates/oauth2/start-idp-link.ftl":
        
        case "templates/oauth2/two-factor.ftl":
        
        case "templates/oauth2/two-factor-enable.ftl":
        
        case "templates/oauth2/two-factor-enable-complete.ftl":
        
        case "templates/oauth2/two-factor-methods.ftl":
        
        case "templates/oauth2/wait.ftl":
        
        case "templates/oauth2/webauthn.ftl":
        
        case "templates/oauth2/webauthn-reauth.ftl":
        
        case "templates/oauth2/webauthn-reauth-enable.ftl":
        
        case "templates/password/change.ftl":
        
        case "templates/password/complete.ftl":
        
        case "templates/password/forgot.ftl":
        
        case "templates/password/sent.ftl":
        
        case "templates/phone/complete.ftl":
        
        case "templates/phone/sent.ftl":
        
        case "templates/phone/verification-required.ftl":
        
        case "templates/phone/verify.ftl":
        
        case "templates/registration/complete.ftl":
        
        case "templates/registration/sent.ftl":
        
        case "templates/registration/verification-required.ftl":
        
        case "templates/registration/verify.ftl":
        
        case "templates/samlv2/logout.ftl":
        
        case "templates/unauthorized.ftl":
        
        case "templates/_helpers.ftl":
        
        default:
          break;
      } 
      Object object = 


















































        
        null;
      if (object != null) {
        if (((FusionAuthUITemplate)object).template == null)
          return null; 
        if (((FusionAuthUITemplate)object).lastModified != null)
          return object; 
      } 
    } 
    return super.findTemplateSource(themedTemplate.name);
  }
  
  public long getLastModified(Object paramObject) {
    if (paramObject instanceof FusionAuthUITemplate)
      return ((FusionAuthUITemplate)paramObject).lastModified.toInstant().toEpochMilli(); 
    return super.getLastModified(paramObject);
  }
  
  public Reader getReader(Object paramObject, String paramString) throws IOException {
    if (paramObject instanceof FusionAuthUITemplate)
      return new StringReader(((FusionAuthUITemplate)paramObject).template); 
    return super.getReader(paramObject, paramString);
  }
  
  private ThemedTemplate resolveTemplate(String paramString) {
    ThemedTemplate themedTemplate = ThemedPageEncoder.decode(paramString);
    themedTemplate.themeable = databaseTemplates.contains(themedTemplate.name);
    if (themedTemplate.themeable) {
      if (themedTemplate.themeId != null)
        themedTemplate.theme = (CachedTheme)this.themeCache.get(themedTemplate.themeId); 
      if (themedTemplate.theme == null)
        themedTemplate.theme = (CachedTheme)this.themeCache.get(Theme.FUSIONAUTH_THEME_ID); 
    } 
    return themedTemplate;
  }
  
  public static class ThemedPageEncoder {
    public static FusionAuthTemplateLoader.ThemedTemplate decode(String param1String) {
      UUID uUID = null;
      String str = param1String;
      if (param1String.startsWith("##")) {
        int i = param1String.indexOf("##", 2);
        String str1 = param1String.substring(2, i);
        try {
          uUID = UUID.fromString(str1);
          int j = param1String.indexOf('?');
          str = (j != -1) ? param1String.substring(i + 2, j) : param1String.substring(i + 2);
        } catch (Exception exception) {}
      } 
      return new FusionAuthTemplateLoader.ThemedTemplate(str, uUID);
    }
    
    public static String encode(String param1String, UUID param1UUID) {
      if (param1UUID != null)
        return "##" + String.valueOf(param1UUID) + "##" + param1String; 
      return param1String;
    }
  }
  
  public static class ThemedTemplate {
    public String name;
    
    public CachedTheme theme;
    
    public UUID themeId;
    
    public boolean themeable;
    
    public ThemedTemplate(String param1String, UUID param1UUID) {
      this.name = param1String;
      this.themeId = param1UUID;
    }
  }
  
  private static class FusionAuthUITemplate {
    public ZonedDateTime lastModified;
    
    public String template;
    
    public FusionAuthUITemplate(ZonedDateTime param1ZonedDateTime, String param1String) {
      this.lastModified = param1ZonedDateTime;
      this.template = param1String;
    }
  }
}
