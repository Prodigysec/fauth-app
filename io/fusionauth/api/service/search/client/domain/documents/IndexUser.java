package io.fusionauth.api.service.search.client.domain.documents;

import com.inversoft.search.client.domain.ElasticRequest;
import io.fusionauth.api.annotation.PublicDoc;
import io.fusionauth.domain.BreachedPasswordStatus;
import io.fusionauth.domain.GroupMember;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.UserTwoFactorConfiguration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class IndexUser implements ElasticRequest {
  @PublicDoc(description = "The user's data object. This is arbitrary JSON and all fields in this object are indexed.")
  public final Map<String, Object> data = new LinkedHashMap<>();
  
  @PublicDoc(description = "A list of the locales of the preferred languages of the user.")
  public final List<Locale> preferredLanguages = new ArrayList<>();
  
  @PublicDoc(description = "Whether the user is active.")
  public Boolean active;
  
  @PublicDoc(description = "The birthdate of the user.")
  public String birthDate;
  
  @PublicDoc(description = "The date the user's password was last checked for breach, as an Instant.")
  public ZonedDateTime breachedPasswordLastCheckedInstant;
  
  @PublicDoc(description = "The user's breached password status.")
  public BreachedPasswordStatus breachedPasswordStatus;
  
  @PublicDoc(description = "The user's email address.")
  public String email;
  
  @PublicDoc(description = "The full name of the user.")
  public String fullName;
  
  @PublicDoc(description = "The Id of the user.")
  public String id;
  
  @PublicDoc(description = "The date the user was inserted, as an Instant.")
  public ZonedDateTime insertInstant;
  
  @PublicDoc(description = "The date the user last logged in, as an Instant.")
  public ZonedDateTime lastLoginInstant;
  
  @PublicDoc(description = "The date the user was most recently updated, as an Instant.")
  public ZonedDateTime lastUpdateInstant;
  
  @PublicDoc(description = "The legacy identifier of the user.", since = "1.67.0")
  public String legacyIdentifier;
  
  @PublicDoc(description = "The login identifier of the user.")
  public String login;
  
  @PublicDoc(description = "The groups the user is a member of. See the memberships field of the [User object](/docs/apis/registrations) for details.")
  public List<GroupMember> memberships;
  
  @PublicDoc(description = "The mobile phone number associated with the user.")
  public String mobilePhone;
  
  @PublicDoc(description = "The parent email associated with this user.")
  public String parentEmail;
  
  @PublicDoc(description = "Whether or not the user must change their password.", since = "1.63.0")
  public boolean passwordChangeRequired;
  
  @PublicDoc(description = "The primary phoneNumber of the user.", since = "1.59.0")
  public String phoneNumber;
  
  @PublicDoc(description = "The registrations for the user. See the [Registration object](/docs/apis/registrations) for details.")
  public List<UserRegistration> registrations;
  
  @PublicDoc(description = "The tenant Id of the tenant of which the user is a member.")
  public UUID tenantId;
  
  @PublicDoc(description = "The timezone of the user.")
  public String timezone;
  
  @PublicDoc(description = "The two factor configuration of the user. See the twoFactor field of the [User object](/docs/apis/registrations) for details.")
  public IndexUserTwoFactorConfiguration twoFactor;
  
  @PublicDoc(description = "The username of the user.")
  public String username;
  
  @PublicDoc(description = "Whether or not the user's email address has been verified.")
  public boolean verified;
  
  @PublicDoc(description = "The date the user's email address was verified, as an Instant.", since = "1.48.0")
  public ZonedDateTime verifiedInstant;
  
  public IndexUser(User paramUser) {
    this.active = Boolean.valueOf(paramUser.active);
    this.birthDate = (paramUser.birthDate != null) ? paramUser.birthDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
    this.data.putAll(paramUser.data);
    this.email = paramUser.lookupEmail();
    if (paramUser.fullName != null) {
      this.fullName = paramUser.fullName;
    } else if (paramUser.firstName != null) {
      this.fullName = paramUser.firstName + paramUser.firstName + ((paramUser.middleName != null) ? (" " + paramUser.middleName) : "");
    } 
    if (this.fullName != null)
      this.fullName = this.fullName.toLowerCase(); 
    this.id = (paramUser.id != null) ? paramUser.id.toString() : null;
    this.insertInstant = paramUser.insertInstant;
    this.lastLoginInstant = paramUser.lastLoginInstant;
    this.lastUpdateInstant = paramUser.lastUpdateInstant;
    this.login = paramUser.getLogin();
    if (this.login != null)
      this.login = this.login.toLowerCase(); 
    this.breachedPasswordLastCheckedInstant = paramUser.breachedPasswordLastCheckedInstant;
    this.breachedPasswordStatus = paramUser.breachedPasswordStatus;
    this.legacyIdentifier = paramUser.legacyIdentifier;
    this.memberships = paramUser.getMemberships();
    this.mobilePhone = paramUser.mobilePhone;
    this.parentEmail = paramUser.parentEmail;
    this.phoneNumber = paramUser.phoneNumber;
    this.preferredLanguages.addAll(paramUser.preferredLanguages);
    this.registrations = paramUser.getRegistrations();
    this.tenantId = paramUser.tenantId;
    if (paramUser.timezone != null)
      this.timezone = paramUser.timezone.toString(); 
    this.twoFactor = new IndexUserTwoFactorConfiguration();
    for (TwoFactorMethod twoFactorMethod1 : paramUser.twoFactor.methods) {
      TwoFactorMethod twoFactorMethod2 = new TwoFactorMethod(twoFactorMethod1);
      twoFactorMethod2.secure();
      twoFactorMethod2.authenticator = null;
      this.twoFactor.methods.add(twoFactorMethod2);
    } 
    if (paramUser.uniqueUsername != null)
      this.username = paramUser.uniqueUsername.toLowerCase(); 
    this.verified = paramUser.verified;
    this.verifiedInstant = paramUser.verifiedInstant;
    this.passwordChangeRequired = paramUser.passwordChangeRequired;
  }
  
  public static class IndexUserTwoFactorConfiguration extends UserTwoFactorConfiguration {
    public List<String> recoveryCodes() {
      return null;
    }
  }
}
