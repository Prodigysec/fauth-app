package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import com.inversoft.cache.CacheNotifier;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.fusionauth.api.domain.CachedTheme;
import io.fusionauth.api.domain.ThemeMapper;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.domain.SimpleThemeVariables;
import io.fusionauth.domain.Theme;
import io.fusionauth.domain.ThemeType;
import io.fusionauth.domain.html.Favicon;
import io.fusionauth.domain.search.SearchResults;
import io.fusionauth.domain.search.ThemeSearchCriteria;
import io.fusionauth.http.server.HTTPContext;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class DefaultThemeService implements ThemeService {
  public static List<String> backgroundSizes = List.of("cover", "contain", "repeat");
  
  public static Pattern cssNumericValuePattern = Pattern.compile("(\\d+(\\.\\d+)?)(px|em|rem)");
  
  public static Pattern fontFamilyPattern = Pattern.compile("^[\\p{L}\\p{M}\\p{N}_\\h\"',./()-]+$");
  
  public static Pattern hexColorPattern = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
  
  public static Pattern hslPattern = Pattern.compile("^hsl\\(\\s*(\\d{1,3})\\s*,?\\s*(100|[1-9]?\\d(\\.\\d{1,2})?)%?\\s*,?\\s*(100|[1-9]?\\d(\\.\\d{1,2})?)%?\\s*\\)");
  
  public static Pattern hslaPattern = Pattern.compile("^hsla\\(\\s*([0-9]{1,3}|[0-9]{1,3}\\.\\d+)\\s*,?\\s*([0-9]{1,3}%|[0-9]{1,3}\\.\\d+%)\\s*,?\\s*([0-9]{1,3}%|[0-9]{1,3}\\.\\d+%)\\s*,?\\s*(0|1|0\\.\\d+|1\\.\\d+)\\s*\\)$");
  
  public static Pattern rgbPattern = Pattern.compile("^rgb\\(\\s*(\\d{1,3})\\s*,?\\s*(\\d{1,3})\\s*,?\\s*(\\d{1,3})\\s*\\)");
  
  public static Pattern rgbaPattern = Pattern.compile("^rgba\\(\\s*(\\d{1,3}|\\d{1,3}\\.\\d+)\\s*,?\\s*(\\d{1,3}|\\d{1,3}\\.\\d+)\\s*,?\\s*(\\d{1,3}|\\d{1,3}\\.\\d+)\\s*,?\\s*(0|1|0\\.\\d+|1\\.\\d+)\\s*\\)$");
  
  private final Pattern END_STYLE_TAG = Pattern.compile(".*</style>.*", 34);
  
  private final ApplicationReaderService applicationReader;
  
  private final CacheNotifier cacheNotifier;
  
  private final HTTPContext context;
  
  private final Configuration freeMarkerConfiguration;
  
  private final ReactorStatusService reactorStatusService;
  
  private final TenantReaderService tenantReader;
  
  private final ThemeMapper themeMapper;
  
  @Inject
  public DefaultThemeService(ApplicationReaderService paramApplicationReaderService, CacheNotifier paramCacheNotifier, TenantReaderService paramTenantReaderService, ThemeMapper paramThemeMapper, Configuration paramConfiguration, HTTPContext paramHTTPContext, ReactorStatusService paramReactorStatusService) {
    this.applicationReader = paramApplicationReaderService;
    this.cacheNotifier = paramCacheNotifier;
    this.tenantReader = paramTenantReaderService;
    this.themeMapper = paramThemeMapper;
    this.freeMarkerConfiguration = paramConfiguration;
    this.context = paramHTTPContext;
    this.reactorStatusService = paramReactorStatusService;
  }
  
  public void create(Theme paramTheme) {
    if (paramTheme.id == null)
      paramTheme.id = UUID.randomUUID(); 
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    paramTheme.insertInstant = zonedDateTime;
    paramTheme.lastUpdateInstant = zonedDateTime;
    this.themeMapper.create(paramTheme);
    this.cacheNotifier.reload("Themes");
  }
  
  public void delete(Theme paramTheme) {
    if (this.themeMapper.delete(paramTheme.id) == 1)
      this.cacheNotifier.reload("Themes"); 
  }
  
  public List<Theme> retrieveAll() {
    List<Theme> list = this.themeMapper.retrieveAll();
    list.stream()
      .filter(paramTheme -> paramTheme.id.equals(Theme.FUSIONAUTH_THEME_ID))
      .findFirst()
      .ifPresent(paramTheme -> ThemeService.populateFusionAuthTheme(paramTheme, this.context.resolve("/")));
    return list;
  }
  
  public Theme retrieveById(UUID paramUUID) {
    Theme theme = this.themeMapper.retrieveById(paramUUID);
    if (theme != null)
      if (theme.id.equals(Theme.FUSIONAUTH_THEME_ID)) {
        ThemeService.populateFusionAuthTheme(theme, this.context.resolve("/"));
      } else if (ThemeType.simple.equals(theme.type) && Theme.FUSIONAUTH_SIMPLE_THEME_ID.equals(paramUUID)) {
        theme.variables = new SimpleThemeVariables(defaultSimpleThemeVariables);
      }  
    return theme;
  }
  
  public SearchResults<Theme> search(ThemeSearchCriteria paramThemeSearchCriteria) {
    int i = this.themeMapper.retrieveCountByCriteria(paramThemeSearchCriteria);
    List<Theme> list = (i > 0) ? this.themeMapper.retrieveByCriteria(paramThemeSearchCriteria) : List.of();
    list.stream()
      .filter(paramTheme -> paramTheme.id.equals(Theme.FUSIONAUTH_THEME_ID))
      .findFirst()
      .ifPresent(paramTheme -> ThemeService.populateFusionAuthTheme(paramTheme, this.context.resolve("/")));
    return new SearchResults<>(list, i);
  }
  
  public boolean templatesPresent(Theme paramTheme) {
    return (paramTheme.templates != null && (paramTheme.templates.accountEdit != null || paramTheme.templates.accountIndex != null || paramTheme.templates.accountTwoFactorDisable != null || paramTheme.templates.accountTwoFactorEdit != null || paramTheme.templates.accountTwoFactorEnable != null || paramTheme.templates.accountTwoFactorIndex != null || paramTheme.templates.accountWebAuthnAdd != null || paramTheme.templates.accountWebAuthnDelete != null || paramTheme.templates.accountWebAuthnIndex != null || paramTheme.templates.confirmationRequired != null || paramTheme.templates.emailComplete != null || paramTheme.templates.emailSent != null || paramTheme.templates.emailVerificationRequired != null || paramTheme.templates.emailVerify != null || paramTheme.templates.helpers != null || paramTheme.templates.index != null || paramTheme.templates.oauth2Authorize != null || paramTheme.templates.oauth2AuthorizedNotRegistered != null || paramTheme.templates.oauth2ChildRegistrationNotAllowed != null || paramTheme.templates.oauth2ChildRegistrationNotAllowedComplete != null || paramTheme.templates.oauth2CompleteRegistration != null || paramTheme.templates.oauth2Consent != null || paramTheme.templates.oauth2Device != null || paramTheme.templates.oauth2Error != null || paramTheme.templates.oauth2Logout != null || paramTheme.templates.oauth2Passwordless != null || paramTheme.templates.oauth2Register != null || paramTheme.templates.oauth2StartIdPLink != null || paramTheme.templates.oauth2TwoFactor != null || paramTheme.templates.oauth2TwoFactorEnable != null || paramTheme.templates.oauth2TwoFactorEnableComplete != null || paramTheme.templates.oauth2TwoFactorMethods != null || paramTheme.templates.oauth2Wait != null || paramTheme.templates.oauth2WebAuthn != null || paramTheme.templates.oauth2WebAuthnReauth != null || paramTheme.templates.oauth2WebAuthnReauthEnable != null || paramTheme.templates.passwordChange != null || paramTheme.templates.passwordComplete != null || paramTheme.templates.passwordForgot != null || paramTheme.templates.passwordSent != null || paramTheme.templates.phoneComplete != null || paramTheme.templates.phoneSent != null || paramTheme.templates.phoneVerificationRequired != null || paramTheme.templates.phoneVerify != null || paramTheme.templates.registrationComplete != null || paramTheme.templates.registrationSent != null || paramTheme.templates.registrationVerificationRequired != null || paramTheme.templates.registrationVerify != null || paramTheme.templates.samlv2Logout != null || paramTheme.templates.unauthorized != null));
  }
  
  public void update(Theme paramTheme1, Theme paramTheme2) {
    paramTheme2.insertInstant = paramTheme1.insertInstant;
    paramTheme2.lastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC);
    if (paramTheme2.id.equals(Theme.FUSIONAUTH_THEME_ID))
      paramTheme2.templates = null; 
    this.themeMapper.update(paramTheme2);
    this.cacheNotifier.reload("Themes");
    if (paramTheme2.id.equals(Theme.FUSIONAUTH_THEME_ID))
      ThemeService.populateFusionAuthTheme(paramTheme2, this.context.resolve("/")); 
  }
  
  public ThemeService.ValidationResult validateCopy(Theme paramTheme, UUID paramUUID) {
    ThemeService.ValidationResult validationResult = new ThemeService.ValidationResult();
    Theme theme = retrieveById(paramUUID);
    validationResult.theme = paramTheme;
    if (theme != null) {
      validationResult.theme.defaultMessages = theme.defaultMessages;
      validationResult.theme.localizedMessages = theme.localizedMessages;
      if (ThemeType.simple.equals(theme.type)) {
        validationResult.theme.variables = theme.variables;
        validationResult.theme.type = ThemeType.simple;
      } else {
        validationResult.theme.templates = theme.templates;
        validationResult.theme.stylesheet = theme.stylesheet;
      } 
    } 
    validationResult
      
      .errors = (new Validator()).withErrors(checkForDuplicates(paramTheme)).valid((theme != null), "sourceThemeId", new Object[] { paramUUID }).done();
    return validationResult;
  }
  
  public ThemeService.ValidationResult validateCreate(Theme paramTheme) {
    ThemeService.ValidationResult validationResult = new ThemeService.ValidationResult();
    validationResult.theme = paramTheme;
    validationResult



      
      .errors = (new Validator()).withErrors(checkForDuplicates(paramTheme)).withErrors(commonValidation(paramTheme)).ifTrue(ThemeType.simple.equals(paramTheme.type), paramValidator -> paramValidator.withErrors(simpleThemeValidation(paramTheme))).done();
    return validationResult;
  }
  
  public Errors validateDefaultMessages(String paramString) {
    Theme theme = new Theme();
    theme.defaultMessages = paramString;
    Validator validator = new Validator();
    validateMessageKeys(validator, theme);
    return validator.done();
  }
  
  public ThemeService.ValidationResult validateDelete(UUID paramUUID) {
    ThemeService.ValidationResult validationResult = new ThemeService.ValidationResult();
    validationResult.existing = (paramUUID != null) ? retrieveById(paramUUID) : null;
    validationResult











      
      .errors = (new Validator()).notMissing(paramUUID, "themeId", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.ensureWithCode((!Theme.FUSIONAUTH_THEME_ID.equals(paramUUID) && !Theme.FUSIONAUTH_SIMPLE_THEME_ID.equals(paramUUID)), "themeId", "[fusionAuth]themeId", new Object[0]).notInUse((this.tenantReader.retrieveCountByThemeId(paramUUID) == 0), "themeId", new Object[] { paramUUID }).ifLastCheckHadNoError(())).done();
    return validationResult;
  }
  
  public ThemeService.ValidationResult validateRetrieve(UUID paramUUID) {
    ThemeService.ValidationResult validationResult = new ThemeService.ValidationResult();
    validationResult.existing = retrieveById(paramUUID);
    return validationResult;
  }
  
  public ThemeService.ValidationResult validateUpdate(Theme paramTheme, UUID paramUUID) {
    ThemeService.ValidationResult validationResult = new ThemeService.ValidationResult();
    validationResult.theme = paramTheme;
    validationResult.existing = retrieveById(paramTheme.id);
    validationResult







      
      .errors = (new Validator()).missing(paramUUID, "sourceThemeId", new Object[0]).ifTrue((ThemeType.advanced.equals(paramTheme.type) && !Theme.FUSIONAUTH_THEME_ID.equals(paramTheme.id)), paramValidator -> paramValidator.notMissing(paramTheme.templates, "theme.templates", new Object[0])).withErrors(commonValidation(paramTheme)).ifTrue(ThemeType.simple.equals(paramTheme.type), paramValidator -> paramValidator.withErrors(simpleThemeValidation(paramTheme))).ifLastCheckHadNoError(paramValidator -> paramValidator.notDuplicate(this.themeMapper.retrieveExistingByName(paramTheme.id, paramTheme.name), "theme.name", new Object[] { paramTheme.name })).done();
    return validationResult;
  }
  
  protected Errors validateTemplate(String paramString1, String paramString2) {
    if (paramString1 == null)
      return (new Errors()).addFieldError(paramString2, "[blank]" + paramString2, null, new Object[] { paramString2 }); 
    if (paramString1.contains("newInstance('") || paramString1
      .contains("newInstance(\"") || paramString1
      .contains("java.lang.Runtime"))
      return (new Errors()).addFieldError(paramString2, "[invalid]" + paramString2, null, new Object[] { "Suspect usage detected" }); 
    try {
      new Template(null, paramString1, this.freeMarkerConfiguration);
    } catch (ParseException parseException) {
      return (new Errors()).addFieldError(paramString2, "[invalid]" + paramString2, null, new Object[] { parseException.getMessage() });
    } catch (IOException iOException) {}
    return null;
  }
  
  private Errors checkForDuplicates(Theme paramTheme) {
    return (new Validator()).notDuplicate((paramTheme.id != null) ? retrieveById(paramTheme.id) : null, "themeId", new Object[] { paramTheme.id }).notDuplicate(this.themeMapper.retrieveExistingByName(null, paramTheme.name), "theme.name", new Object[] { paramTheme.name }).done();
  }
  
  private Errors commonValidation(Theme paramTheme) {
    return (new Validator())
      .notBlank(paramTheme.name, "theme.name", new Object[0])




      
      .ifTrue((!Theme.FUSIONAUTH_THEME_ID.equals(paramTheme.id) && !ThemeType.simple.equals(paramTheme.type)), paramValidator -> paramValidator.notBlank(paramTheme.defaultMessages, "theme.defaultMessages", new Object[0]).forEach(paramTheme.localizedMessages.entrySet(), ()).ensure(validateStyleSheet(paramTheme.stylesheet), "theme.stylesheet", "[invalid]", new Object[0]).ifTrue(templatesPresent(paramTheme), ()))


























































      
      .ifNoFieldErrors("theme.defaultMessages", paramValidator -> validateMessageKeys(paramValidator, paramTheme))
      
      .done();
  }
  
  private boolean containsScript(String paramString) {
    if (paramString.toLowerCase().contains("t:alert("))
      return true; 
    try {
      Document document = Jsoup.parse(paramString);
      if (document.selectFirst("script, [onerror], [onload], [onmouseover]") != null)
        return true; 
    } catch (Exception exception) {}
    return false;
  }
  
  private Errors simpleThemeValidation(Theme paramTheme) {
    return (new Validator())
      .notMissing(paramTheme.variables, "theme.variables", new Object[0])
      .missing(paramTheme.templates, "theme.templates", new Object[0])
      .missing(paramTheme.stylesheet, "theme.stylesheet", new Object[0])
      .ifTrue((paramTheme.variables != null), paramValidator -> paramValidator.withErrors(validateColorVariable(paramTheme.variables.alertBackgroundColor, "theme.variables.alertBackgroundColor")).withErrors(validateColorVariable(paramTheme.variables.alertFontColor, "theme.variables.alertFontColor")).ifTrue((paramTheme.variables.backgroundImageURL != null), ()).withErrors(validateNumericVariable(paramTheme.variables.borderRadius, "theme.variables.borderRadius")).withErrors(validateColorVariable(paramTheme.variables.deleteButtonColor, "theme.variables.deleteButtonColor")).withErrors(validateColorVariable(paramTheme.variables.deleteButtonFocusColor, "theme.variables.deleteButtonFocusColor")).withErrors(validateColorVariable(paramTheme.variables.deleteButtonTextColor, "theme.variables.deleteButtonTextColor")).withErrors(validateColorVariable(paramTheme.variables.deleteButtonTextFocusColor, "theme.variables.deleteButtonTextFocusColor")).withErrors(validateColorVariable(paramTheme.variables.errorFontColor, "theme.variables.errorFontColor")).withErrors(validateColorVariable(paramTheme.variables.errorIconColor, "theme.variables.errorIconColor")).ifTrue((paramTheme.variables.favicons != null), ()).withErrors(validateColorVariable(paramTheme.variables.fontColor, "theme.variables.fontColor")).withErrors(validateFontFamilyVariable(paramTheme.variables.fontFamily, "theme.variables.fontFamily")).ifFalse(paramTheme.variables.footerDisplay, ()).withErrors(validateColorVariable(paramTheme.variables.iconBackgroundColor, "theme.variables.iconBackgroundColor")).withErrors(validateColorVariable(paramTheme.variables.iconColor, "theme.variables.iconColor")).withErrors(validateColorVariable(paramTheme.variables.infoIconColor, "theme.variables.infoIconColor")).withErrors(validateColorVariable(paramTheme.variables.inputBackgroundColor, "theme.variables.inputBackgroundColor")).withErrors(validateColorVariable(paramTheme.variables.inputIconColor, "theme.variables.inputIconColor")).withErrors(validateColorVariable(paramTheme.variables.inputTextColor, "theme.variables.inputTextColor")).withErrors(validateColorVariable(paramTheme.variables.linkTextColor, "theme.variables.linkTextColor")).withErrors(validateColorVariable(paramTheme.variables.linkTextFocusColor, "theme.variables.linkTextFocusColor")).ifTrue((paramTheme.variables.logoImageURL != null), ()).withErrors(validateColorVariable(paramTheme.variables.monoFontColor, "theme.variables.monoFontColor")).withErrors(validateFontFamilyVariable(paramTheme.variables.monoFontFamily, "theme.variables.monoFontFamily")).withErrors(validateColorVariable(paramTheme.variables.pageBackgroundColor, "theme.variables.pageBackgroundColor")).withErrors(validateColorVariable(paramTheme.variables.panelBackgroundColor, "theme.variables.panelBackgroundColor")).withErrors(validateColorVariable(paramTheme.variables.primaryButtonColor, "theme.variables.primaryButtonColor")).withErrors(validateColorVariable(paramTheme.variables.primaryButtonFocusColor, "theme.variables.primaryButtonFocusColor")).withErrors(validateColorVariable(paramTheme.variables.primaryButtonTextColor, "theme.variables.primaryButtonTextColor")).withErrors(validateColorVariable(paramTheme.variables.primaryButtonTextFocusColor, "theme.variables.primaryButtonTextFocusColor")))

















































      
      .done();
  }
  
  private boolean validColorValue(String paramString) {
    return (paramString != null && (validHexColor(paramString) || validRgb(paramString) || validHsl(paramString) || validHsla(paramString) || validRgba(paramString)));
  }
  
  private boolean validFontFamilyValue(String paramString) {
    if (paramString == null || paramString.isBlank())
      return false; 
    return fontFamilyPattern.matcher(paramString.trim()).matches();
  }
  
  private boolean validHexColor(String paramString) {
    return hexColorPattern.matcher(paramString).matches();
  }
  
  private boolean validHsl(String paramString) {
    return hslPattern.matcher(paramString).matches();
  }
  
  private boolean validHsla(String paramString) {
    return hslaPattern.matcher(paramString).matches();
  }
  
  private boolean validNumericVariable(String paramString) {
    if ("0".equals(paramString))
      return true; 
    return cssNumericValuePattern.matcher(paramString).matches();
  }
  
  private boolean validRgb(String paramString) {
    Matcher matcher = rgbPattern.matcher(paramString);
    if (matcher.matches()) {
      int i = Integer.parseInt(matcher.group(1));
      int j = Integer.parseInt(matcher.group(2));
      int k = Integer.parseInt(matcher.group(3));
      return (i >= 0 && i <= 255 && j >= 0 && j <= 255 && k >= 0 && k <= 255);
    } 
    return false;
  }
  
  private boolean validRgba(String paramString) {
    return rgbaPattern.matcher(paramString).matches();
  }
  
  private Errors validateColorVariable(String paramString1, String paramString2) {
    return (new Validator()).notBlank(paramString1, paramString2, new Object[0])
      .ifLastCheckHadNoError(paramValidator -> paramValidator.ensure(validColorValue(paramString1), paramString2, "[invalid]", new Object[0]))
      .done();
  }
  
  private Errors validateFontFamilyVariable(String paramString1, String paramString2) {
    return (new Validator()).notBlank(paramString1, paramString2, new Object[0])
      .ifLastCheckHadNoError(paramValidator -> paramValidator.ensure(validFontFamilyValue(paramString1), paramString2, "[invalid]", new Object[0]))
      .done();
  }
  
  private void validateMessageKeys(Validator paramValidator, Theme paramTheme) {
    CachedTheme cachedTheme = new CachedTheme(paramTheme);
    Set<?> set = (Set)cachedTheme.defaultProperties.keySet().stream().map(Object::toString).collect(Collectors.toSet());
    if (paramTheme.type == ThemeType.advanced && !Theme.FUSIONAUTH_THEME_ID.equals(paramTheme.id)) {
      Theme theme = new Theme();
      theme.defaultMessages = ThemeService.messagesFromFilesystem(this.context.resolve("/"));
      CachedTheme cachedTheme1 = new CachedTheme(theme);
      Set<? extends CharSequence> set1 = (Set)cachedTheme1.defaultProperties.keySet().stream().map(Object::toString).collect(Collectors.toSet());
      set1.removeAll(set);
      LinkedHashMap<Object, Object> linkedHashMap = new LinkedHashMap<>();
      set1.forEach(paramString -> paramMap.put(paramString, paramCachedTheme.defaultProperties.getProperty(paramString)));
      paramValidator.valid(set1.isEmpty(), "theme.defaultMessages", new Object[] { String.join(", ", set1), "\n" + (String)linkedHashMap
            .keySet()
            .stream()
            .map(paramString -> paramString + "=" + paramString).collect(Collectors.joining("\n")) });
    } 
    for (String str1 : set) {
      String str2 = cachedTheme.defaultProperties.getProperty(str1);
      if (containsScript(str2)) {
        paramValidator.validWithCode(false, "theme.defaultMessages", "[noScript]theme.defaultMessages", new Object[0]);
        break;
      } 
    } 
    for (Locale locale : cachedTheme.localizedProperties.keySet()) {
      Properties properties = cachedTheme.localizedProperties.get(locale);
      for (String str1 : properties.stringPropertyNames()) {
        String str2 = properties.getProperty(str1);
        if (containsScript(str2))
          paramValidator.validWithCode(false, "theme.localizedMessages['" + String.valueOf(locale) + "']", "[noScript]theme.localizedMessages", new Object[0]); 
      } 
    } 
  }
  
  private Errors validateNumericVariable(String paramString1, String paramString2) {
    return (new Validator()).notBlank(paramString1, paramString2, new Object[0])
      .ifLastCheckHadNoError(paramValidator -> paramValidator.ensure(validNumericVariable(paramString1), paramString2, "[invalid]", new Object[0]))
      .done();
  }
  
  private boolean validateStyleSheet(String paramString) {
    if (paramString == null)
      return true; 
    if (containsScript(paramString))
      return false; 
    return !this.END_STYLE_TAG.matcher(paramString).matches();
  }
}
