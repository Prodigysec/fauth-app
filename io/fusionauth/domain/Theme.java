package io.fusionauth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.domain.util.Normalizer;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Stream;

public class Theme implements Buildable<Theme>, JSONColumnable {
  public static final UUID FUSIONAUTH_SIMPLE_THEME_ID = UUID.fromString("3c717291-5d83-4014-bd51-97c76475dc86");
  
  public static final UUID FUSIONAUTH_THEME_ID = UUID.fromString("75a068fd-e94b-451a-9aeb-3ddb9a3b5987");
  
  public Map<String, Object> data = new HashMap<>();
  
  @JSONColumn
  public String defaultMessages;
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public ZonedDateTime lastUpdateInstant;
  
  @JSONColumn
  public LocalizedStrings localizedMessages = new LocalizedStrings();
  
  public String name;
  
  @JSONColumn
  public String stylesheet;
  
  @JSONColumn
  public Templates templates;
  
  @JSONColumn
  public ThemeType type = ThemeType.advanced;
  
  @JSONColumn
  public SimpleThemeVariables variables;
  
  public Theme(Theme paramTheme) {
    this.data.putAll(paramTheme.data);
    this.defaultMessages = paramTheme.defaultMessages;
    this.id = paramTheme.id;
    this.insertInstant = paramTheme.insertInstant;
    this.lastUpdateInstant = paramTheme.lastUpdateInstant;
    this.localizedMessages.putAll(paramTheme.localizedMessages);
    this.name = paramTheme.name;
    this.variables = paramTheme.variables;
    this.stylesheet = paramTheme.stylesheet;
    this.type = paramTheme.type;
    if (paramTheme.templates != null)
      this.templates = new Templates(paramTheme.templates); 
  }
  
  public Set<Locale> additionalLocales() {
    return Collections.emptySet();
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof Theme))
      return false; 
    Theme theme = (Theme)paramObject;
    return (Objects.equals(this.data, theme.data) && 
      Objects.equals(this.defaultMessages, theme.defaultMessages) && 
      Objects.equals(this.id, theme.id) && 
      Objects.equals(this.insertInstant, theme.insertInstant) && 
      Objects.equals(this.lastUpdateInstant, theme.lastUpdateInstant) && 
      Objects.equals(this.localizedMessages, theme.localizedMessages) && 
      Objects.equals(this.name, theme.name) && 
      Objects.equals(this.variables, theme.variables) && 
      Objects.equals(this.stylesheet, theme.stylesheet) && 
      Objects.equals(this.templates, theme.templates) && 
      Objects.equals(this.type, theme.type));
  }
  
  public String formatZoneDateTime(ZonedDateTime paramZonedDateTime, String paramString) {
    return paramZonedDateTime.format(DateTimeFormatter.ofPattern(paramString));
  }
  
  public String formatZoneDateTime(ZonedDateTime paramZonedDateTime, String paramString, ZoneId paramZoneId) {
    return paramZonedDateTime.withZoneSameInstant(paramZoneId).format(DateTimeFormatter.ofPattern(paramString));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.data, this.defaultMessages, this.id, this.insertInstant, this.lastUpdateInstant, this.localizedMessages, this.name, this.variables, this.stylesheet, this.templates, 
          this.type });
  }
  
  public String message(String paramString, Object... paramVarArgs) {
    return "";
  }
  
  public boolean missingMessages(Set<String> paramSet) {
    if (this.type != ThemeType.advanced || FUSIONAUTH_THEME_ID.equals(this.id) || paramSet == null || paramSet.isEmpty())
      return false; 
    Properties properties = new Properties();
    if (this.defaultMessages != null)
      try {
        properties.load(new StringReader(this.defaultMessages));
      } catch (IOException iOException) {
        throw new UncheckedIOException(iOException);
      }  
    return paramSet.stream().anyMatch(paramString -> !paramProperties.containsKey(paramString));
  }
  
  @JsonIgnore
  public boolean missingTemplate() {
    if (ThemeType.simple.equals(this.type))
      return false; 
    if (this.templates == null)
      return true; 
    return Stream.<String>of(new String[] { 
          this.templates.accountEdit, this.templates.accountIndex, this.templates.accountTwoFactorDisable, this.templates.accountTwoFactorEdit, this.templates.accountTwoFactorEnable, this.templates.accountTwoFactorIndex, this.templates.accountWebAuthnAdd, this.templates.accountWebAuthnDelete, this.templates.accountWebAuthnIndex, this.templates.confirmationRequired, 
          this.templates.emailComplete, this.templates.emailSent, this.templates.emailVerificationRequired, this.templates.emailVerify, this.templates.helpers, this.templates.index, this.templates.oauth2Authorize, this.templates.oauth2AuthorizedNotRegistered, this.templates.oauth2ChildRegistrationNotAllowed, this.templates.oauth2ChildRegistrationNotAllowedComplete, 
          this.templates.oauth2CompleteRegistration, this.templates.oauth2Consent, this.templates.oauth2Device, this.templates.oauth2DeviceComplete, this.templates.oauth2Error, this.templates.oauth2Logout, this.templates.oauth2Passwordless, this.templates.oauth2Register, this.templates.oauth2StartIdPLink, this.templates.oauth2TwoFactor, 
          this.templates.oauth2TwoFactorEnable, this.templates.oauth2TwoFactorEnableComplete, this.templates.oauth2TwoFactorMethods, this.templates.oauth2Wait, this.templates.oauth2WebAuthn, this.templates.oauth2WebAuthnReauth, this.templates.oauth2WebAuthnReauthEnable, this.templates.passwordChange, this.templates.passwordComplete, this.templates.passwordForgot, 
          this.templates.passwordSent, this.templates.phoneComplete, this.templates.phoneSent, this.templates.phoneVerificationRequired, this.templates.phoneVerify, this.templates.registrationComplete, this.templates.registrationSent, this.templates.registrationVerificationRequired, this.templates.registrationVerify, this.templates.samlv2Logout, 
          this.templates.unauthorized }).anyMatch(Objects::isNull);
  }
  
  public void normalize() {
    if (this.defaultMessages != null)
      this.defaultMessages = Normalizer.lineReturns(this.defaultMessages); 
    if (this.templates != null)
      this.templates.normalize(); 
    if (this.localizedMessages != null)
      this.localizedMessages.normalize(); 
    this.stylesheet = Normalizer.lineReturns(this.stylesheet);
  }
  
  public String optionalMessage(String paramString, Object... paramVarArgs) {
    return "";
  }
  
  public String stylesheet() {
    if (this.stylesheet == null)
      return ""; 
    return Normalizer.lineReturns(this.stylesheet);
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  public Theme() {}
  
  public static class Templates implements Buildable<Templates> {
    public static final SortedSet<String> Names = Collections.unmodifiableSortedSet(new TreeSet<>(Arrays.asList(new String[] { 
              "accountEdit", "accountIndex", "accountTwoFactorDisable", "accountTwoFactorEdit", "accountTwoFactorEnable", "accountTwoFactorIndex", "accountWebAuthnAdd", "accountWebAuthnDelete", "accountWebAuthnIndex", "confirmationRequired", 
              "emailComplete", "emailSent", "emailVerificationRequired", "emailVerify", "helpers", "index", "oauth2Authorize", "oauth2AuthorizedNotRegistered", "oauth2ChildRegistrationNotAllowed", "oauth2ChildRegistrationNotAllowedComplete", 
              "oauth2CompleteRegistration", "oauth2Consent", "oauth2Device", "oauth2DeviceComplete", "oauth2Error", "oauth2Logout", "oauth2Passwordless", "oauth2Register", "oauth2StartIdPLink", "oauth2TwoFactor", 
              "oauth2TwoFactorEnable", "oauth2TwoFactorEnableComplete", "oauth2TwoFactorMethods", "oauth2Wait", "oauth2WebAuthn", "oauth2WebAuthnReauth", "oauth2WebAuthnReauthEnable", "passwordChange", "passwordComplete", "passwordForgot", 
              "passwordSent", "phoneComplete", "phoneSent", "phoneVerificationRequired", "phoneVerify", "registrationComplete", "registrationSent", "registrationVerificationRequired", "registrationVerify", "samlv2Logout", 
              "unauthorized" })));
    
    public String accountEdit;
    
    public String accountIndex;
    
    public String accountTwoFactorDisable;
    
    public String accountTwoFactorEdit;
    
    public String accountTwoFactorEnable;
    
    public String accountTwoFactorIndex;
    
    public String accountWebAuthnAdd;
    
    public String accountWebAuthnDelete;
    
    public String accountWebAuthnIndex;
    
    public String confirmationRequired;
    
    public String emailComplete;
    
    public String emailSent;
    
    public String emailVerificationRequired;
    
    public String emailVerify;
    
    public String helpers;
    
    public String index;
    
    public String oauth2Authorize;
    
    public String oauth2AuthorizedNotRegistered;
    
    public String oauth2ChildRegistrationNotAllowed;
    
    public String oauth2ChildRegistrationNotAllowedComplete;
    
    public String oauth2CompleteRegistration;
    
    public String oauth2Consent;
    
    public String oauth2Device;
    
    public String oauth2DeviceComplete;
    
    public String oauth2Error;
    
    public String oauth2Logout;
    
    public String oauth2Passwordless;
    
    public String oauth2Register;
    
    public String oauth2StartIdPLink;
    
    public String oauth2TwoFactor;
    
    public String oauth2TwoFactorEnable;
    
    public String oauth2TwoFactorEnableComplete;
    
    public String oauth2TwoFactorMethods;
    
    public String oauth2Wait;
    
    public String oauth2WebAuthn;
    
    public String oauth2WebAuthnReauth;
    
    public String oauth2WebAuthnReauthEnable;
    
    public String passwordChange;
    
    public String passwordComplete;
    
    public String passwordForgot;
    
    public String passwordSent;
    
    public String phoneComplete;
    
    public String phoneSent;
    
    public String phoneVerificationRequired;
    
    public String phoneVerify;
    
    public String registrationComplete;
    
    public String registrationSent;
    
    public String registrationVerificationRequired;
    
    public String registrationVerify;
    
    public String samlv2Logout;
    
    public String unauthorized;
    
    public Templates() {}
    
    public Templates(Templates param1Templates) {
      this.accountEdit = param1Templates.accountEdit;
      this.accountIndex = param1Templates.accountIndex;
      this.accountTwoFactorDisable = param1Templates.accountTwoFactorDisable;
      this.accountTwoFactorEdit = param1Templates.accountTwoFactorEdit;
      this.accountTwoFactorEnable = param1Templates.accountTwoFactorEnable;
      this.accountTwoFactorIndex = param1Templates.accountTwoFactorIndex;
      this.accountWebAuthnAdd = param1Templates.accountWebAuthnAdd;
      this.accountWebAuthnDelete = param1Templates.accountWebAuthnDelete;
      this.accountWebAuthnIndex = param1Templates.accountWebAuthnIndex;
      this.confirmationRequired = param1Templates.confirmationRequired;
      this.emailComplete = param1Templates.emailComplete;
      this.emailSent = param1Templates.emailSent;
      this.emailVerificationRequired = param1Templates.emailVerificationRequired;
      this.emailVerify = param1Templates.emailVerify;
      this.helpers = param1Templates.helpers;
      this.index = param1Templates.index;
      this.oauth2Authorize = param1Templates.oauth2Authorize;
      this.oauth2AuthorizedNotRegistered = param1Templates.oauth2AuthorizedNotRegistered;
      this.oauth2ChildRegistrationNotAllowed = param1Templates.oauth2ChildRegistrationNotAllowed;
      this.oauth2ChildRegistrationNotAllowedComplete = param1Templates.oauth2ChildRegistrationNotAllowedComplete;
      this.oauth2CompleteRegistration = param1Templates.oauth2CompleteRegistration;
      this.oauth2Consent = param1Templates.oauth2Consent;
      this.oauth2Device = param1Templates.oauth2Device;
      this.oauth2DeviceComplete = param1Templates.oauth2DeviceComplete;
      this.oauth2Error = param1Templates.oauth2Error;
      this.oauth2Logout = param1Templates.oauth2Logout;
      this.oauth2Passwordless = param1Templates.oauth2Passwordless;
      this.oauth2Register = param1Templates.oauth2Register;
      this.oauth2StartIdPLink = param1Templates.oauth2StartIdPLink;
      this.oauth2TwoFactor = param1Templates.oauth2TwoFactor;
      this.oauth2TwoFactorEnable = param1Templates.oauth2TwoFactorEnable;
      this.oauth2TwoFactorEnableComplete = param1Templates.oauth2TwoFactorEnableComplete;
      this.oauth2TwoFactorMethods = param1Templates.oauth2TwoFactorMethods;
      this.oauth2Wait = param1Templates.oauth2Wait;
      this.oauth2WebAuthn = param1Templates.oauth2WebAuthn;
      this.oauth2WebAuthnReauth = param1Templates.oauth2WebAuthnReauth;
      this.oauth2WebAuthnReauthEnable = param1Templates.oauth2WebAuthnReauthEnable;
      this.passwordChange = param1Templates.passwordChange;
      this.passwordComplete = param1Templates.passwordComplete;
      this.passwordForgot = param1Templates.passwordForgot;
      this.passwordSent = param1Templates.passwordSent;
      this.phoneComplete = param1Templates.phoneComplete;
      this.phoneSent = param1Templates.phoneSent;
      this.phoneVerificationRequired = param1Templates.phoneVerificationRequired;
      this.phoneVerify = param1Templates.phoneVerify;
      this.registrationComplete = param1Templates.registrationComplete;
      this.registrationSent = param1Templates.registrationSent;
      this.registrationVerificationRequired = param1Templates.registrationVerificationRequired;
      this.registrationVerify = param1Templates.registrationVerify;
      this.samlv2Logout = param1Templates.samlv2Logout;
      this.unauthorized = param1Templates.unauthorized;
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (param1Object == null || getClass() != param1Object.getClass())
        return false; 
      Templates templates = (Templates)param1Object;
      return (Objects.equals(this.accountEdit, templates.accountEdit) && 
        Objects.equals(this.accountIndex, templates.accountIndex) && 
        Objects.equals(this.accountTwoFactorDisable, templates.accountTwoFactorDisable) && 
        Objects.equals(this.accountTwoFactorEdit, templates.accountTwoFactorEdit) && 
        Objects.equals(this.accountTwoFactorEnable, templates.accountTwoFactorEnable) && 
        Objects.equals(this.accountTwoFactorIndex, templates.accountTwoFactorIndex) && 
        Objects.equals(this.accountWebAuthnAdd, templates.accountWebAuthnAdd) && 
        Objects.equals(this.accountWebAuthnDelete, templates.accountWebAuthnDelete) && 
        Objects.equals(this.accountWebAuthnIndex, templates.accountWebAuthnIndex) && 
        Objects.equals(this.confirmationRequired, templates.confirmationRequired) && 
        Objects.equals(this.emailComplete, templates.emailComplete) && 
        Objects.equals(this.emailSent, templates.emailSent) && 
        Objects.equals(this.emailVerificationRequired, templates.emailVerificationRequired) && 
        Objects.equals(this.emailVerify, templates.emailVerify) && 
        Objects.equals(this.helpers, templates.helpers) && 
        Objects.equals(this.index, templates.index) && 
        Objects.equals(this.oauth2Authorize, templates.oauth2Authorize) && 
        Objects.equals(this.oauth2AuthorizedNotRegistered, templates.oauth2AuthorizedNotRegistered) && 
        Objects.equals(this.oauth2ChildRegistrationNotAllowed, templates.oauth2ChildRegistrationNotAllowed) && 
        Objects.equals(this.oauth2ChildRegistrationNotAllowedComplete, templates.oauth2ChildRegistrationNotAllowedComplete) && 
        Objects.equals(this.oauth2CompleteRegistration, templates.oauth2CompleteRegistration) && 
        Objects.equals(this.oauth2Consent, templates.oauth2Consent) && 
        Objects.equals(this.oauth2Device, templates.oauth2Device) && 
        Objects.equals(this.oauth2DeviceComplete, templates.oauth2DeviceComplete) && 
        Objects.equals(this.oauth2Error, templates.oauth2Error) && 
        Objects.equals(this.oauth2Logout, templates.oauth2Logout) && 
        Objects.equals(this.oauth2Passwordless, templates.oauth2Passwordless) && 
        Objects.equals(this.oauth2Register, templates.oauth2Register) && 
        Objects.equals(this.oauth2StartIdPLink, templates.oauth2StartIdPLink) && 
        Objects.equals(this.oauth2TwoFactor, templates.oauth2TwoFactor) && 
        Objects.equals(this.oauth2TwoFactorEnable, templates.oauth2TwoFactorEnable) && 
        Objects.equals(this.oauth2TwoFactorEnableComplete, templates.oauth2TwoFactorEnableComplete) && 
        Objects.equals(this.oauth2TwoFactorMethods, templates.oauth2TwoFactorMethods) && 
        Objects.equals(this.oauth2Wait, templates.oauth2Wait) && 
        Objects.equals(this.oauth2WebAuthn, templates.oauth2WebAuthn) && 
        Objects.equals(this.oauth2WebAuthnReauth, templates.oauth2WebAuthnReauth) && 
        Objects.equals(this.oauth2WebAuthnReauthEnable, templates.oauth2WebAuthnReauthEnable) && 
        Objects.equals(this.passwordChange, templates.passwordChange) && 
        Objects.equals(this.passwordComplete, templates.passwordComplete) && 
        Objects.equals(this.passwordForgot, templates.passwordForgot) && 
        Objects.equals(this.passwordSent, templates.passwordSent) && 
        Objects.equals(this.phoneComplete, templates.phoneComplete) && 
        Objects.equals(this.phoneSent, templates.phoneSent) && 
        Objects.equals(this.phoneVerificationRequired, templates.phoneVerificationRequired) && 
        Objects.equals(this.phoneVerify, templates.phoneVerify) && 
        Objects.equals(this.registrationComplete, templates.registrationComplete) && 
        Objects.equals(this.registrationSent, templates.registrationSent) && 
        Objects.equals(this.registrationVerificationRequired, templates.registrationVerificationRequired) && 
        Objects.equals(this.registrationVerify, templates.registrationVerify) && 
        Objects.equals(this.samlv2Logout, templates.samlv2Logout) && 
        Objects.equals(this.unauthorized, templates.unauthorized));
    }
    
    @Deprecated
    public String getEmailSend() {
      return this.emailSent;
    }
    
    @Deprecated
    public void setEmailSend(String param1String) {
      this.emailSent = param1String;
    }
    
    @Deprecated
    public String getRegistrationSend() {
      return this.registrationSent;
    }
    
    @Deprecated
    public void setRegistrationSend(String param1String) {
      this.registrationSent = param1String;
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { 
            this.accountEdit, this.accountIndex, this.accountTwoFactorDisable, this.accountTwoFactorEdit, this.accountTwoFactorEnable, this.accountTwoFactorIndex, this.accountWebAuthnAdd, this.accountWebAuthnDelete, this.accountWebAuthnIndex, this.confirmationRequired, 
            this.emailComplete, this.emailSent, this.emailVerificationRequired, this.emailVerify, this.helpers, this.index, this.oauth2Authorize, this.oauth2AuthorizedNotRegistered, this.oauth2ChildRegistrationNotAllowed, this.oauth2ChildRegistrationNotAllowedComplete, 
            this.oauth2CompleteRegistration, this.oauth2Consent, this.oauth2Device, this.oauth2DeviceComplete, this.oauth2Error, this.oauth2Logout, this.oauth2Passwordless, this.oauth2Register, this.oauth2StartIdPLink, this.oauth2TwoFactor, 
            this.oauth2TwoFactorEnable, this.oauth2TwoFactorEnableComplete, this.oauth2TwoFactorMethods, this.oauth2Wait, this.oauth2WebAuthn, this.oauth2WebAuthnReauth, this.oauth2WebAuthnReauthEnable, this.passwordChange, this.passwordComplete, this.passwordForgot, 
            this.passwordSent, this.phoneComplete, this.phoneSent, this.phoneVerificationRequired, this.phoneVerify, this.registrationComplete, this.registrationSent, this.registrationVerificationRequired, this.registrationVerify, this.samlv2Logout, 
            this.unauthorized });
    }
    
    public void normalize() {
      this.accountEdit = Normalizer.lineReturns(Normalizer.trimToNull(this.accountEdit));
      this.accountIndex = Normalizer.lineReturns(Normalizer.trimToNull(this.accountIndex));
      this.accountTwoFactorDisable = Normalizer.lineReturns(Normalizer.trimToNull(this.accountTwoFactorDisable));
      this.accountTwoFactorEdit = Normalizer.lineReturns(Normalizer.trimToNull(this.accountTwoFactorEdit));
      this.accountTwoFactorEnable = Normalizer.lineReturns(Normalizer.trimToNull(this.accountTwoFactorEnable));
      this.accountTwoFactorIndex = Normalizer.lineReturns(Normalizer.trimToNull(this.accountTwoFactorIndex));
      this.accountWebAuthnAdd = Normalizer.lineReturns(Normalizer.trimToNull(this.accountWebAuthnAdd));
      this.accountWebAuthnDelete = Normalizer.lineReturns(Normalizer.trimToNull(this.accountWebAuthnDelete));
      this.accountWebAuthnIndex = Normalizer.lineReturns(Normalizer.trimToNull(this.accountWebAuthnIndex));
      this.confirmationRequired = Normalizer.lineReturns(Normalizer.trim(this.confirmationRequired));
      this.emailComplete = Normalizer.lineReturns(Normalizer.trimToNull(this.emailComplete));
      this.emailSent = Normalizer.lineReturns(Normalizer.trimToNull(this.emailSent));
      this.emailVerificationRequired = Normalizer.lineReturns(Normalizer.trimToNull(this.emailVerificationRequired));
      this.emailVerify = Normalizer.lineReturns(Normalizer.trimToNull(this.emailVerify));
      this.helpers = Normalizer.lineReturns(Normalizer.trimToNull(this.helpers));
      this.index = Normalizer.lineReturns(Normalizer.trimToNull(this.index));
      this.oauth2Authorize = Normalizer.lineReturns(Normalizer.trimToNull(this.oauth2Authorize));
      this.oauth2AuthorizedNotRegistered = Normalizer.lineReturns(Normalizer.trimToNull(this.oauth2AuthorizedNotRegistered));
      this.oauth2ChildRegistrationNotAllowed = Normalizer.lineReturns(Normalizer.trimToNull(this.oauth2ChildRegistrationNotAllowed));
      this.oauth2ChildRegistrationNotAllowedComplete = Normalizer.lineReturns(Normalizer.trimToNull(this.oauth2ChildRegistrationNotAllowedComplete));
      this.oauth2CompleteRegistration = Normalizer.lineReturns(Normalizer.trimToNull(this.oauth2CompleteRegistration));
      this.oauth2Consent = Normalizer.lineReturns(Normalizer.trimToNull(this.oauth2Consent));
      this.oauth2Device = Normalizer.lineReturns(Normalizer.trimToNull(this.oauth2Device));
      this.oauth2DeviceComplete = Normalizer.lineReturns(Normalizer.trimToNull(this.oauth2DeviceComplete));
      this.oauth2Error = Normalizer.lineReturns(Normalizer.trimToNull(this.oauth2Error));
      this.oauth2Logout = Normalizer.lineReturns(Normalizer.trimToNull(this.oauth2Logout));
      this.oauth2Passwordless = Normalizer.lineReturns(Normalizer.trimToNull(this.oauth2Passwordless));
      this.oauth2Register = Normalizer.lineReturns(Normalizer.trimToNull(this.oauth2Register));
      this.oauth2StartIdPLink = Normalizer.lineReturns(Normalizer.trimToNull(this.oauth2StartIdPLink));
      this.oauth2TwoFactor = Normalizer.lineReturns(Normalizer.trimToNull(this.oauth2TwoFactor));
      this.oauth2TwoFactorEnable = Normalizer.lineReturns(Normalizer.trimToNull(this.oauth2TwoFactorEnable));
      this.oauth2TwoFactorEnableComplete = Normalizer.lineReturns(Normalizer.trimToNull(this.oauth2TwoFactorEnableComplete));
      this.oauth2TwoFactorMethods = Normalizer.lineReturns(Normalizer.trimToNull(this.oauth2TwoFactorMethods));
      this.oauth2Wait = Normalizer.lineReturns(Normalizer.trimToNull(this.oauth2Wait));
      this.oauth2WebAuthn = Normalizer.lineReturns(Normalizer.trimToNull(this.oauth2WebAuthn));
      this.oauth2WebAuthnReauth = Normalizer.lineReturns(Normalizer.trimToNull(this.oauth2WebAuthnReauth));
      this.oauth2WebAuthnReauthEnable = Normalizer.lineReturns(Normalizer.trimToNull(this.oauth2WebAuthnReauthEnable));
      this.passwordChange = Normalizer.lineReturns(Normalizer.trimToNull(this.passwordChange));
      this.passwordComplete = Normalizer.lineReturns(Normalizer.trimToNull(this.passwordComplete));
      this.passwordForgot = Normalizer.lineReturns(Normalizer.trimToNull(this.passwordForgot));
      this.passwordSent = Normalizer.lineReturns(Normalizer.trimToNull(this.passwordSent));
      this.phoneComplete = Normalizer.lineReturns(Normalizer.trimToNull(this.phoneComplete));
      this.phoneSent = Normalizer.lineReturns(Normalizer.trimToNull(this.phoneSent));
      this.phoneVerificationRequired = Normalizer.lineReturns(Normalizer.trimToNull(this.phoneVerificationRequired));
      this.phoneVerify = Normalizer.lineReturns(Normalizer.trimToNull(this.phoneVerify));
      this.registrationComplete = Normalizer.lineReturns(Normalizer.trimToNull(this.registrationComplete));
      this.registrationSent = Normalizer.lineReturns(Normalizer.trimToNull(this.registrationSent));
      this.registrationVerificationRequired = Normalizer.lineReturns(Normalizer.trimToNull(this.registrationVerificationRequired));
      this.registrationVerify = Normalizer.lineReturns(Normalizer.trimToNull(this.registrationVerify));
      this.samlv2Logout = Normalizer.lineReturns(Normalizer.trimToNull(this.samlv2Logout));
      this.unauthorized = Normalizer.lineReturns(Normalizer.trimToNull(this.unauthorized));
    }
    
    public String toString() {
      return ToString.toString(this);
    }
  }
}
