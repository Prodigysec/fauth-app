package io.fusionauth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.domain.util.Normalizer;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class User extends SecureIdentity implements Buildable<User>, Tenantable, JSONColumnable {
  @JSONColumn
  public final List<Locale> preferredLanguages = new ArrayList<>();
  
  private final List<GroupMember> memberships = new ArrayList<>();
  
  private final List<UserRegistration> registrations = new ArrayList<>();
  
  public boolean active;
  
  public LocalDate birthDate;
  
  public UUID cleanSpeakId;
  
  public Map<String, Object> data = new LinkedHashMap<>();
  
  public String email;
  
  public ZonedDateTime expiry;
  
  public String firstName;
  
  public String fullName;
  
  public URI imageUrl;
  
  public ZonedDateTime insertInstant;
  
  public String lastName;
  
  public String legacyIdentifier;
  
  public ZonedDateTime lastUpdateInstant;
  
  public String middleName;
  
  public String mobilePhone;
  
  public String parentEmail;
  
  public String phoneNumber;
  
  public UUID tenantId;
  
  public ZoneId timezone;
  
  @JSONColumn
  public UserTwoFactorConfiguration twoFactor = new UserTwoFactorConfiguration();
  
  public User(User paramUser) {
    this.active = paramUser.active;
    this.connectorId = paramUser.connectorId;
    this.breachedPasswordLastCheckedInstant = paramUser.breachedPasswordLastCheckedInstant;
    this.breachedPasswordStatus = paramUser.breachedPasswordStatus;
    this.birthDate = paramUser.birthDate;
    this.cleanSpeakId = paramUser.cleanSpeakId;
    this.email = paramUser.email;
    this.encryptionScheme = paramUser.encryptionScheme;
    this.expiry = paramUser.expiry;
    this.factor = paramUser.factor;
    this.firstName = paramUser.firstName;
    this.fullName = paramUser.fullName;
    this.id = paramUser.id;
    this.imageUrl = paramUser.imageUrl;
    this.insertInstant = paramUser.insertInstant;
    this.lastLoginInstant = paramUser.lastLoginInstant;
    this.lastUpdateInstant = paramUser.lastUpdateInstant;
    this.lastName = paramUser.lastName;
    this.legacyIdentifier = paramUser.legacyIdentifier;
    this.identities.addAll((Collection<? extends UserIdentity>)paramUser.identities.stream().map(UserIdentity::new).collect(Collectors.toCollection(ArrayList::new)));
    this.memberships.addAll((Collection<? extends GroupMember>)paramUser.memberships.stream().map(GroupMember::new).collect(Collectors.toCollection(ArrayList::new)));
    this.middleName = paramUser.middleName;
    this.mobilePhone = paramUser.mobilePhone;
    this.parentEmail = paramUser.parentEmail;
    this.password = paramUser.password;
    this.passwordChangeReason = paramUser.passwordChangeReason;
    this.passwordChangeRequired = paramUser.passwordChangeRequired;
    this.passwordLastUpdateInstant = paramUser.passwordLastUpdateInstant;
    this.phoneNumber = paramUser.phoneNumber;
    this.preferredLanguages.addAll(paramUser.preferredLanguages);
    this.registrations.addAll((Collection<? extends UserRegistration>)paramUser.registrations.stream().map(UserRegistration::new).collect(Collectors.toCollection(ArrayList::new)));
    this.salt = paramUser.salt;
    this.tenantId = paramUser.tenantId;
    this.timezone = paramUser.timezone;
    this.twoFactor = new UserTwoFactorConfiguration(paramUser.twoFactor);
    this.uniqueUsername = paramUser.uniqueUsername;
    this.username = paramUser.username;
    this.usernameStatus = paramUser.usernameStatus;
    this.verified = paramUser.verified;
    this.verifiedInstant = paramUser.verifiedInstant;
    if (paramUser.data != null)
      this.data.putAll(paramUser.data); 
  }
  
  public void addMemberships(GroupMember paramGroupMember) {
    this.memberships.removeIf(paramGroupMember2 -> paramGroupMember2.groupId.equals(paramGroupMember1.groupId));
    this.memberships.add(paramGroupMember);
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof User))
      return false; 
    if (!super.equals(paramObject))
      return false; 
    User user = (User)paramObject;
    sort();
    user.sort();
    return (this.active == user.active && 
      Objects.equals(this.preferredLanguages, user.preferredLanguages) && 
      Objects.equals(this.memberships, user.memberships) && 
      Objects.equals(this.registrations, user.registrations) && 
      Objects.equals(this.birthDate, user.birthDate) && 
      Objects.equals(this.cleanSpeakId, user.cleanSpeakId) && 
      Objects.equals(this.data, user.data) && 
      Objects.equals(this.email, user.email) && 
      Objects.equals(this.expiry, user.expiry) && 
      Objects.equals(this.firstName, user.firstName) && 
      Objects.equals(this.fullName, user.fullName) && 
      Objects.equals(this.imageUrl, user.imageUrl) && 
      Objects.equals(this.insertInstant, user.insertInstant) && 
      Objects.equals(this.lastName, user.lastName) && 
      Objects.equals(this.legacyIdentifier, user.legacyIdentifier) && 
      Objects.equals(this.lastUpdateInstant, user.lastUpdateInstant) && 
      Objects.equals(this.middleName, user.middleName) && 
      Objects.equals(this.mobilePhone, user.mobilePhone) && 
      Objects.equals(this.parentEmail, user.parentEmail) && 
      Objects.equals(this.phoneNumber, user.phoneNumber) && 
      Objects.equals(this.twoFactor, user.twoFactor) && 
      Objects.equals(this.tenantId, user.tenantId) && 
      Objects.equals(this.timezone, user.timezone));
  }
  
  @JsonIgnore
  public int getAge() {
    if (this.birthDate == null)
      return -1; 
    return (int)this.birthDate.until(LocalDate.now(), ChronoUnit.YEARS);
  }
  
  @JsonIgnore
  public String getLogin() {
    if (this.email != null)
      return this.email; 
    if (this.uniqueUsername != null)
      return this.uniqueUsername; 
    return this.phoneNumber;
  }
  
  public List<GroupMember> getMemberships() {
    return this.memberships;
  }
  
  @JsonIgnore
  public String getName() {
    if (this.fullName != null)
      return this.fullName; 
    if (this.firstName != null)
      return this.firstName + this.firstName; 
    return null;
  }
  
  public UserRegistration getRegistrationForApplication(UUID paramUUID) {
    return getRegistrations().stream()
      .filter(paramUserRegistration -> paramUserRegistration.applicationId.equals(paramUUID))
      .findFirst()
      .orElse(null);
  }
  
  public List<UserRegistration> getRegistrations() {
    return this.registrations;
  }
  
  public Set<String> getRoleNamesForApplication(UUID paramUUID) {
    UserRegistration userRegistration = getRegistrationForApplication(paramUUID);
    return (userRegistration != null) ? userRegistration.roles : null;
  }
  
  public UUID getTenantId() {
    return this.tenantId;
  }
  
  public boolean hasIdentityType(IdentityType paramIdentityType) {
    return this.identities.stream()
      .anyMatch(paramUserIdentity -> paramUserIdentity.type.is(paramIdentityType));
  }
  
  public boolean hasUserData() {
    if (!this.data.isEmpty())
      return true; 
    for (UserRegistration userRegistration : this.registrations) {
      if (userRegistration.hasRegistrationData())
        return true; 
    } 
    return false;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          Integer.valueOf(super.hashCode()), this.preferredLanguages, this.memberships, this.registrations, Boolean.valueOf(this.active), this.birthDate, this.cleanSpeakId, this.data, this.email, this.expiry, 
          this.firstName, this.fullName, this.imageUrl, this.insertInstant, this.lastName, this.legacyIdentifier, this.lastUpdateInstant, this.middleName, this.mobilePhone, this.parentEmail, 
          this.phoneNumber, this.tenantId, this.timezone, this.twoFactor });
  }
  
  public String lookupEmail() {
    if (this.email != null)
      return this.email; 
    if (this.data.containsKey("email"))
      return this.data.get("email").toString(); 
    return null;
  }
  
  public Locale lookupPreferredLanguage(UUID paramUUID) {
    for (UserRegistration userRegistration : this.registrations) {
      if (userRegistration.applicationId.equals(paramUUID) && 
        userRegistration.preferredLanguages.size() > 0)
        return userRegistration.preferredLanguages.get(0); 
    } 
    if (this.preferredLanguages.size() > 0)
      return this.preferredLanguages.get(0); 
    return null;
  }
  
  public void normalize() {
    Normalizer.removeEmpty(this.data);
    this.email = Normalizer.toLowerCase(Normalizer.trimToNull(this.email));
    this.identities.forEach(UserIdentity::normalize);
    this.encryptionScheme = Normalizer.trim(this.encryptionScheme);
    this.firstName = Normalizer.trim(this.firstName);
    this.fullName = Normalizer.trim(this.fullName);
    this.lastName = Normalizer.trim(this.lastName);
    this.legacyIdentifier = Normalizer.trimToNull(this.legacyIdentifier);
    this.middleName = Normalizer.trim(this.middleName);
    this.mobilePhone = Normalizer.trim(this.mobilePhone);
    this.parentEmail = Normalizer.toLowerCase(Normalizer.trim(this.parentEmail));
    this.phoneNumber = Normalizer.trimToNull(this.phoneNumber);
    Normalizer.removeEmpty(this.preferredLanguages);
    Normalizer.deDuplicate(this.preferredLanguages);
    this.preferredLanguages.removeIf(paramLocale -> paramLocale.toString().equals(""));
    this.username = Normalizer.trimToNull(this.username);
    this.memberships.removeIf(paramGroupMember -> (paramGroupMember.groupId == null));
    getRegistrations().forEach(UserRegistration::normalize);
  }
  
  public void removeIdentitiesOfType(IdentityType paramIdentityType) {
    this.identities.removeIf(paramUserIdentity -> paramUserIdentity.type.is(paramIdentityType));
  }
  
  public void removeMembershipById(UUID paramUUID) {
    this.memberships.removeIf(paramGroupMember -> paramGroupMember.groupId.equals(paramUUID));
  }
  
  public void replaceIdentities(List<UserIdentity> paramList) {
    Set set = (Set)paramList.stream().map(paramUserIdentity -> paramUserIdentity.type).collect(Collectors.toSet());
    this.identities.removeIf(paramUserIdentity -> paramSet.contains(paramUserIdentity.type));
    this.identities.addAll(paramList);
  }
  
  public UserIdentity resolveFirstIdentity() {
    return Optional.<UserIdentity>ofNullable(resolveLegacyIdentity())
      .orElse(this.identities.stream()
        .findFirst()
        .orElse(null));
  }
  
  public List<UserIdentity> resolveIdentitiesOfType(IdentityType paramIdentityType) {
    return (List<UserIdentity>)this.identities.stream().filter(paramUserIdentity -> paramUserIdentity.type.is(paramIdentityType))
      .collect(Collectors.toList());
  }
  
  public UserIdentity resolveLegacyIdentity() {
    return resolvePrimaryIdentity(new IdentityType[] { IdentityType.email, IdentityType.username });
  }
  
  public UserIdentity resolvePrimaryIdentity(IdentityType... paramVarArgs) {
    for (IdentityType identityType : paramVarArgs) {
      UserIdentity userIdentity = resolvePrimaryIdentity(identityType);
      if (userIdentity != null)
        return userIdentity; 
    } 
    return null;
  }
  
  @JsonIgnore
  public UserIdentity resolvePrimaryIdentity(IdentityType paramIdentityType) {
    return this.identities.stream()
      .filter(paramUserIdentity -> paramUserIdentity.primary)
      .filter(paramUserIdentity -> paramUserIdentity.type.is(paramIdentityType))
      .findFirst()
      .orElse(null);
  }
  
  public User secure() {
    this.encryptionScheme = null;
    this.factor = null;
    this.password = null;
    this.salt = null;
    this.twoFactor.secure();
    return this;
  }
  
  public User sort() {
    this.identities.sort(Comparator.comparing(paramUserIdentity -> paramUserIdentity.insertInstant, Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(paramUserIdentity -> paramUserIdentity.type, Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(paramUserIdentity -> paramUserIdentity.value, Comparator.nullsLast(Comparator.naturalOrder())));
    this.registrations.sort(Comparator.comparing(paramUserRegistration -> paramUserRegistration.applicationId));
    return this;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  public boolean twoFactorEnabled() {
    return (this.twoFactor.methods.size() > 0);
  }
  
  @JacksonConstructor
  public User() {}
}
