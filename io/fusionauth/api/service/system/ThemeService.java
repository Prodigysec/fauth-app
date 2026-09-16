package io.fusionauth.api.service.system;

import com.inversoft.error.Errors;
import com.inversoft.io.IOTools;
import com.inversoft.util.MapBuilder;
import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.api.util.CollectionTools;
import io.fusionauth.app.service.theme.variables.ColorVar;
import io.fusionauth.app.service.theme.variables.FontVar;
import io.fusionauth.app.service.theme.variables.HrefVar;
import io.fusionauth.app.service.theme.variables.NumericVar;
import io.fusionauth.app.service.theme.variables.ThemeVar;
import io.fusionauth.domain.SimpleThemeVariables;
import io.fusionauth.domain.Theme;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.ThemeSearchCriteria;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ThemeService {
  public static final Map<String, Map<String, String>> TemplateCategoryMapping = CollectionTools.readOnly((new MapBuilder())
      
      .put("start", MapBuilder.map((Object[])new String[] { "index", "index.ftl", "oauth2Authorize", "oauth2/authorize.ftl", "oauth2Logout", "oauth2/logout.ftl", "oauth2Register", "oauth2/register.ftl", "oauth2AuthorizedNotRegistered", "oauth2/authorized-not-registered.ftl" })).put("account", MapBuilder.map((Object[])new String[] { 
            "accountEdit", "account/edit.ftl", "accountIndex", "account/index.ftl", "accountTwoFactorDisable", "account/two-factor/disable.ftl", "accountTwoFactorEdit", "account/two-factor/edit.ftl", "accountTwoFactorEnable", "account/two-factor/enable.ftl", 
            "accountTwoFactorIndex", "account/two-factor/index.ftl", "accountWebAuthnAdd", "account/webauthn/add.ftl", "accountWebAuthnDelete", "account/webauthn/delete.ftl", "accountWebAuthnIndex", "account/webauthn/index.ftl" })).put("device", MapBuilder.map((Object[])new String[] { "oauth2Device", "oauth2/device.ftl", "oauth2DeviceComplete", "oauth2/device-complete.ftl", "oauth2StartIdPLink", "oauth2/start-idp-link.ftl", "oauth2Wait", "oauth2/wait.ftl" })).put("magic", MapBuilder.map((Object[])new String[] { "oauth2Passwordless", "oauth2/passwordless.ftl", "confirmationRequired", "confirmation-required.ftl" })).put("mfa", MapBuilder.map((Object[])new String[] { 
            "oauth2TwoFactor", "oauth2/two-factor.ftl", "oauth2TwoFactorEnable", "oauth2/two-factor-enable.ftl", "oauth2TwoFactorEnableComplete", "oauth2/two-factor-enable-complete.ftl", "oauth2TwoFactorMethods", "oauth2/two-factor-methods.ftl", "oauth2WebAuthn", "oauth2/webauthn.ftl", 
            "oauth2WebAuthnReauth", "oauth2/webauthn-reauth.ftl", "oauth2WebAuthnReauthEnable", "oauth2/webauthn-reauth-enable.ftl" })).put("password", MapBuilder.map((Object[])new String[] { "passwordChange", "password/change.ftl", "passwordComplete", "password/complete.ftl", "passwordForgot", "password/forgot.ftl", "passwordSent", "password/sent.ftl" })).put("verification", MapBuilder.map((Object[])new String[] { 
            "emailComplete", "email/complete.ftl", "emailSent", "email/sent.ftl", "emailVerificationRequired", "email/verification-required.ftl", "emailVerify", "email/verify.ftl", "phoneComplete", "phone/complete.ftl", 
            "phoneSent", "phone/sent.ftl", "phoneVerificationRequired", "phone/verification-required.ftl", "phoneVerify", "phone/verify.ftl", "registrationComplete", "registration/complete.ftl", "registrationSent", "registration/sent.ftl", 
            "registrationVerificationRequired", "registration/verification-required.ftl", "registrationVerify", "registration/verify.ftl" })).put("other", MapBuilder.map((Object[])new String[] { 
            "oauth2CompleteRegistration", "oauth2/complete-registration.ftl", "oauth2Consent", "oauth2/consent.ftl", "oauth2Error", "oauth2/error.ftl", "samlv2Logout", "samlv2/logout.ftl", "oauth2ChildRegistrationNotAllowed", "oauth2/child-registration-not-allowed.ftl", 
            "oauth2ChildRegistrationNotAllowedComplete", "oauth2/child-registration-not-allowed-complete.ftl", "unauthorized", "unauthorized.ftl" })).map());
  
  public static final Map<String, String> TemplateMapping;
  
  public static final SimpleThemeVariables defaultSimpleThemeVariables;
  
  static {
    TemplateMapping = (Map<String, String>)TemplateCategoryMapping.entrySet().stream().flatMap(paramEntry -> ((Map)paramEntry.getValue()).entrySet().stream()).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    defaultSimpleThemeVariables = (new SimpleThemeVariables()).with(paramSimpleThemeVariables -> {
          paramSimpleThemeVariables.alertFontColor = "#0f172a";
          paramSimpleThemeVariables.alertBackgroundColor = "#ffffff";
          paramSimpleThemeVariables.errorFontColor = "#e11d48";
          paramSimpleThemeVariables.errorIconColor = "#fb7185";
          paramSimpleThemeVariables.fontColor = "#0f172a";
          paramSimpleThemeVariables.inputIconColor = "#4c4c4c";
          paramSimpleThemeVariables.iconBackgroundColor = "#f7f7f7";
          paramSimpleThemeVariables.inputBackgroundColor = "#ffffff";
          paramSimpleThemeVariables.inputTextColor = "#0f172a";
          paramSimpleThemeVariables.linkTextColor = "#6366f1";
          paramSimpleThemeVariables.linkTextFocusColor = "#4338ca";
          paramSimpleThemeVariables.panelBackgroundColor = "#ffffff";
          paramSimpleThemeVariables.pageBackgroundColor = "#f1f5f9";
          paramSimpleThemeVariables.primaryButtonColor = "#6366f1";
          paramSimpleThemeVariables.primaryButtonTextColor = "#ffffff";
          paramSimpleThemeVariables.primaryButtonTextFocusColor = "#f1f5f9";
          paramSimpleThemeVariables.primaryButtonFocusColor = "#4f46e5";
          paramSimpleThemeVariables.deleteButtonColor = "#be123c";
          paramSimpleThemeVariables.deleteButtonTextColor = "#ffffff";
          paramSimpleThemeVariables.deleteButtonTextFocusColor = "#f1f5f9";
          paramSimpleThemeVariables.iconColor = "#227cbb";
          paramSimpleThemeVariables.infoIconColor = "#818cf8";
          paramSimpleThemeVariables.deleteButtonFocusColor = "#9f1239";
          paramSimpleThemeVariables.fontFamily = "Inter, -apple-system, system-ui, BlinkMacSystemFont, \"Segoe UI\", Roboto, \"Helvetica Neue\", Arial, sans-serif";
          paramSimpleThemeVariables.monoFontColor = "#334155";
          paramSimpleThemeVariables.monoFontFamily = "\"Courier\", ui-monospace, \"Courier New\", monospace";
          paramSimpleThemeVariables.borderRadius = "0.25rem";
          paramSimpleThemeVariables.backgroundSize = "contain";
          paramSimpleThemeVariables.footerDisplay = true;
        });
  }
  
  static String messagesFromFilesystem(Path paramPath) {
    Path path = paramPath.resolve("messages");
    return IOTools.loadFile(path.resolve("theme/messages.properties"));
  }
  
  static void populateFusionAuthTheme(Theme paramTheme, Path paramPath) {
    paramTheme.templates = templatesFromFilesystem(paramPath);
    paramTheme.defaultMessages = messagesFromFilesystem(paramPath);
  }
  
  static void populateSimpleTheme(Theme paramTheme, Path paramPath) {
    paramTheme.templates = templatesFromFilesystem(paramPath);
    if (Theme.FUSIONAUTH_SIMPLE_THEME_ID.equals(paramTheme.id))
      paramTheme.variables = new SimpleThemeVariables(defaultSimpleThemeVariables); 
    if (paramTheme.variables != null) {
      HashMap<Object, Object> hashMap = new HashMap<>();
      addVarToMap((Map)hashMap, ColorVar.alertBackgroundColor, paramTheme.variables.alertBackgroundColor);
      addVarToMap((Map)hashMap, ColorVar.alertFontColor, paramTheme.variables.alertFontColor);
      addVarToMap((Map)hashMap, NumericVar.borderRadius, paramTheme.variables.borderRadius);
      addVarToMap((Map)hashMap, ColorVar.deleteButtonColor, paramTheme.variables.deleteButtonColor);
      addVarToMap((Map)hashMap, ColorVar.deleteButtonFocusColor, paramTheme.variables.deleteButtonFocusColor);
      addVarToMap((Map)hashMap, ColorVar.deleteButtonTextColor, paramTheme.variables.deleteButtonTextColor);
      addVarToMap((Map)hashMap, ColorVar.deleteButtonTextFocusColor, paramTheme.variables.deleteButtonTextFocusColor);
      addVarToMap((Map)hashMap, ColorVar.errorIconColor, paramTheme.variables.errorIconColor);
      addVarToMap((Map)hashMap, ColorVar.errorFontColor, paramTheme.variables.errorFontColor);
      addVarToMap((Map)hashMap, ColorVar.fontColor, paramTheme.variables.fontColor);
      addVarToMap((Map)hashMap, FontVar.fontFamily, paramTheme.variables.fontFamily);
      addVarToMap((Map)hashMap, ColorVar.iconBackgroundColor, paramTheme.variables.iconBackgroundColor);
      addVarToMap((Map)hashMap, ColorVar.iconColor, paramTheme.variables.iconColor);
      addVarToMap((Map)hashMap, ColorVar.infoIconColor, paramTheme.variables.infoIconColor);
      addVarToMap((Map)hashMap, ColorVar.inputBackgroundColor, paramTheme.variables.inputBackgroundColor);
      addVarToMap((Map)hashMap, ColorVar.inputIconColor, paramTheme.variables.inputIconColor);
      addVarToMap((Map)hashMap, ColorVar.inputTextColor, paramTheme.variables.inputTextColor);
      addVarToMap((Map)hashMap, ColorVar.linkTextColor, paramTheme.variables.linkTextColor);
      addVarToMap((Map)hashMap, ColorVar.linkTextFocusColor, paramTheme.variables.linkTextFocusColor);
      addVarToMap((Map)hashMap, NumericVar.logoImageSize, paramTheme.variables.logoImageSize);
      addVarToMap((Map)hashMap, ColorVar.monoFontColor, paramTheme.variables.monoFontColor);
      addVarToMap((Map)hashMap, FontVar.monoFontFamily, paramTheme.variables.monoFontFamily);
      addVarToMap((Map)hashMap, ColorVar.pageBackgroundColor, paramTheme.variables.pageBackgroundColor);
      addVarToMap((Map)hashMap, ColorVar.panelBackgroundColor, paramTheme.variables.panelBackgroundColor);
      addVarToMap((Map)hashMap, ColorVar.primaryButtonColor, paramTheme.variables.primaryButtonColor);
      addVarToMap((Map)hashMap, ColorVar.primaryButtonFocusColor, paramTheme.variables.primaryButtonFocusColor);
      addVarToMap((Map)hashMap, ColorVar.primaryButtonTextColor, paramTheme.variables.primaryButtonTextColor);
      addVarToMap((Map)hashMap, ColorVar.primaryButtonTextFocusColor, paramTheme.variables.primaryButtonTextFocusColor);
      if (paramTheme.variables.backgroundImageURL != null)
        addVarToMap((Map)hashMap, HrefVar.backgroundImageURL, paramTheme.variables.backgroundImageURL.toString()); 
      if (paramTheme.variables.logoImageURL != null)
        addVarToMap((Map)hashMap, HrefVar.logoImageURL, paramTheme.variables.logoImageURL.toString()); 
      hashMap.put("--footer-display", paramTheme.variables.footerDisplay ? "flex" : "none");
      if (paramTheme.variables.logoImageURL == null) {
        hashMap.put("--img-logo-display", "none");
      } else {
        hashMap.put("--img-logo-display", "flex");
      } 
      if (paramTheme.variables.backgroundImageURL != null)
        if (paramTheme.variables.backgroundSize.equals("repeat")) {
          hashMap.put("--background-size", "auto");
          hashMap.put("--background-repeat", "repeat");
        } else if (paramTheme.variables.backgroundSize.equals("cover")) {
          hashMap.put("--background-size", "cover");
          hashMap.put("--background-repeat", "no-repeat");
        } else {
          hashMap.put("--background-size", "contain");
          hashMap.put("--background-repeat", "no-repeat");
        }  
      String str = "{\n" + (String)hashMap.entrySet().stream().map(paramEntry -> (String)paramEntry.getKey() + ":" + (String)paramEntry.getKey() + ";").collect(Collectors.joining("\n")) + "\n}";
      paramTheme.stylesheet = ":root " + str + "\n" + paramTheme.stylesheet;
    } 
  }
  
  static String templateFromFilesystem(Path paramPath, String paramString) {
    Path path = paramPath.resolve("templates");
    try {
      return IOTools.loadFile(path.resolve(TemplateMapping.get(paramString)));
    } catch (Exception exception) {
      return null;
    } 
  }
  
  static Theme.Templates templatesFromFilesystem(Path paramPath) {
    Path path = paramPath.resolve("templates");
    return (new Theme.Templates())
      .with(paramTemplates -> paramTemplates.accountEdit = IOTools.loadFile(paramPath.resolve("account/edit.ftl")))
      .with(paramTemplates -> paramTemplates.accountIndex = IOTools.loadFile(paramPath.resolve("account/index.ftl")))
      .with(paramTemplates -> paramTemplates.accountTwoFactorDisable = IOTools.loadFile(paramPath.resolve("account/two-factor/disable.ftl")))
      .with(paramTemplates -> paramTemplates.accountTwoFactorEdit = IOTools.loadFile(paramPath.resolve("account/two-factor/edit.ftl")))
      .with(paramTemplates -> paramTemplates.accountTwoFactorEnable = IOTools.loadFile(paramPath.resolve("account/two-factor/enable.ftl")))
      .with(paramTemplates -> paramTemplates.accountTwoFactorIndex = IOTools.loadFile(paramPath.resolve("account/two-factor/index.ftl")))
      .with(paramTemplates -> paramTemplates.accountWebAuthnAdd = IOTools.loadFile(paramPath.resolve("account/webauthn/add.ftl")))
      .with(paramTemplates -> paramTemplates.accountWebAuthnDelete = IOTools.loadFile(paramPath.resolve("account/webauthn/delete.ftl")))
      .with(paramTemplates -> paramTemplates.accountWebAuthnIndex = IOTools.loadFile(paramPath.resolve("account/webauthn/index.ftl")))
      .with(paramTemplates -> paramTemplates.confirmationRequired = IOTools.loadFile(paramPath.resolve("confirmation-required.ftl")))
      .with(paramTemplates -> paramTemplates.emailComplete = IOTools.loadFile(paramPath.resolve("email/complete.ftl")))
      .with(paramTemplates -> paramTemplates.emailSent = IOTools.loadFile(paramPath.resolve("email/sent.ftl")))
      .with(paramTemplates -> paramTemplates.emailVerificationRequired = IOTools.loadFile(paramPath.resolve("email/verification-required.ftl")))
      .with(paramTemplates -> paramTemplates.emailVerify = IOTools.loadFile(paramPath.resolve("email/verify.ftl")))
      .with(paramTemplates -> paramTemplates.helpers = IOTools.loadFile(paramPath.resolve("_helpers.ftl")))
      .with(paramTemplates -> paramTemplates.index = IOTools.loadFile(paramPath.resolve("index.ftl")))
      .with(paramTemplates -> paramTemplates.oauth2Authorize = IOTools.loadFile(paramPath.resolve("oauth2/authorize.ftl")))
      .with(paramTemplates -> paramTemplates.oauth2AuthorizedNotRegistered = IOTools.loadFile(paramPath.resolve("oauth2/authorized-not-registered.ftl")))
      .with(paramTemplates -> paramTemplates.oauth2ChildRegistrationNotAllowed = IOTools.loadFile(paramPath.resolve("oauth2/child-registration-not-allowed.ftl")))
      .with(paramTemplates -> paramTemplates.oauth2ChildRegistrationNotAllowedComplete = IOTools.loadFile(paramPath.resolve("oauth2/child-registration-not-allowed-complete.ftl")))
      .with(paramTemplates -> paramTemplates.oauth2CompleteRegistration = IOTools.loadFile(paramPath.resolve("oauth2/complete-registration.ftl")))
      .with(paramTemplates -> paramTemplates.oauth2Consent = IOTools.loadFile(paramPath.resolve("oauth2/consent.ftl")))
      .with(paramTemplates -> paramTemplates.oauth2Device = IOTools.loadFile(paramPath.resolve("oauth2/device.ftl")))
      .with(paramTemplates -> paramTemplates.oauth2DeviceComplete = IOTools.loadFile(paramPath.resolve("oauth2/device-complete.ftl")))
      .with(paramTemplates -> paramTemplates.oauth2Error = IOTools.loadFile(paramPath.resolve("oauth2/error.ftl")))
      .with(paramTemplates -> paramTemplates.oauth2Logout = IOTools.loadFile(paramPath.resolve("oauth2/logout.ftl")))
      .with(paramTemplates -> paramTemplates.oauth2Passwordless = IOTools.loadFile(paramPath.resolve("oauth2/passwordless.ftl")))
      .with(paramTemplates -> paramTemplates.oauth2Register = IOTools.loadFile(paramPath.resolve("oauth2/register.ftl")))
      .with(paramTemplates -> paramTemplates.oauth2StartIdPLink = IOTools.loadFile(paramPath.resolve("oauth2/start-idp-link.ftl")))
      .with(paramTemplates -> paramTemplates.oauth2TwoFactor = IOTools.loadFile(paramPath.resolve("oauth2/two-factor.ftl")))
      .with(paramTemplates -> paramTemplates.oauth2TwoFactorEnable = IOTools.loadFile(paramPath.resolve("oauth2/two-factor-enable.ftl")))
      .with(paramTemplates -> paramTemplates.oauth2TwoFactorEnableComplete = IOTools.loadFile(paramPath.resolve("oauth2/two-factor-enable-complete.ftl")))
      .with(paramTemplates -> paramTemplates.oauth2TwoFactorMethods = IOTools.loadFile(paramPath.resolve("oauth2/two-factor-methods.ftl")))
      .with(paramTemplates -> paramTemplates.oauth2Wait = IOTools.loadFile(paramPath.resolve("oauth2/wait.ftl")))
      .with(paramTemplates -> paramTemplates.oauth2WebAuthn = IOTools.loadFile(paramPath.resolve("oauth2/webauthn.ftl")))
      .with(paramTemplates -> paramTemplates.oauth2WebAuthnReauth = IOTools.loadFile(paramPath.resolve("oauth2/webauthn-reauth.ftl")))
      .with(paramTemplates -> paramTemplates.oauth2WebAuthnReauthEnable = IOTools.loadFile(paramPath.resolve("oauth2/webauthn-reauth-enable.ftl")))
      .with(paramTemplates -> paramTemplates.passwordChange = IOTools.loadFile(paramPath.resolve("password/change.ftl")))
      .with(paramTemplates -> paramTemplates.passwordComplete = IOTools.loadFile(paramPath.resolve("password/complete.ftl")))
      .with(paramTemplates -> paramTemplates.passwordForgot = IOTools.loadFile(paramPath.resolve("password/forgot.ftl")))
      .with(paramTemplates -> paramTemplates.passwordSent = IOTools.loadFile(paramPath.resolve("password/sent.ftl")))
      .with(paramTemplates -> paramTemplates.phoneComplete = IOTools.loadFile(paramPath.resolve("phone/complete.ftl")))
      .with(paramTemplates -> paramTemplates.phoneSent = IOTools.loadFile(paramPath.resolve("phone/sent.ftl")))
      .with(paramTemplates -> paramTemplates.phoneVerificationRequired = IOTools.loadFile(paramPath.resolve("phone/verification-required.ftl")))
      .with(paramTemplates -> paramTemplates.phoneVerify = IOTools.loadFile(paramPath.resolve("phone/verify.ftl")))
      .with(paramTemplates -> paramTemplates.registrationComplete = IOTools.loadFile(paramPath.resolve("registration/complete.ftl")))
      .with(paramTemplates -> paramTemplates.registrationSent = IOTools.loadFile(paramPath.resolve("registration/sent.ftl")))
      .with(paramTemplates -> paramTemplates.registrationVerificationRequired = IOTools.loadFile(paramPath.resolve("registration/verification-required.ftl")))
      .with(paramTemplates -> paramTemplates.registrationVerify = IOTools.loadFile(paramPath.resolve("registration/verify.ftl")))
      .with(paramTemplates -> paramTemplates.samlv2Logout = IOTools.loadFile(paramPath.resolve("samlv2/logout.ftl")))
      .with(paramTemplates -> paramTemplates.unauthorized = IOTools.loadFile(paramPath.resolve("unauthorized.ftl")));
  }
  
  private static void addVarToMap(Map<String, String> paramMap, ThemeVar paramThemeVar, String paramString) {
    if (paramString != null)
      if (paramThemeVar instanceof HrefVar) {
        HrefVar hrefVar = (HrefVar)paramThemeVar;
        paramMap.put(hrefVar.getVariable(), "url('" + paramString + "')");
      } else {
        paramMap.put(paramThemeVar.getVariable(), paramString);
      }  
  }
  
  void create(Theme paramTheme);
  
  void delete(Theme paramTheme);
  
  List<Theme> retrieveAll();
  
  Theme retrieveById(UUID paramUUID);
  
  SearchResults<Theme> search(ThemeSearchCriteria paramThemeSearchCriteria);
  
  boolean templatesPresent(Theme paramTheme);
  
  void update(Theme paramTheme1, Theme paramTheme2);
  
  ValidationResult validateCopy(Theme paramTheme, UUID paramUUID);
  
  ValidationResult validateCreate(Theme paramTheme);
  
  Errors validateDefaultMessages(String paramString);
  
  ValidationResult validateDelete(UUID paramUUID);
  
  ValidationResult validateRetrieve(UUID paramUUID);
  
  ValidationResult validateUpdate(Theme paramTheme, UUID paramUUID);
  
  public static class ValidationResult extends BaseValidationResult {
    public Theme existing;
    
    public Theme theme;
  }
}
