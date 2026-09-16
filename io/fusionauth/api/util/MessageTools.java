package io.fusionauth.api.util;

import com.inversoft.util.SecurityTools;
import io.fusionauth.domain.ContentStatus;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.UUID;

public class MessageTools {
  public static User buildMockUser(String paramString, UUID paramUUID1, UUID paramUUID2) {
    return (new User()).with(paramUser -> paramUser.id = paramUUID)
      .with(paramUser -> paramUser.email = String.format("{{%s.email}}", new Object[] { paramString })).with(paramUser -> paramUser.username = String.format("{{%s.username}}", new Object[] { paramString })).with(paramUser -> paramUser.phoneNumber = "+15555555555")
      .with(paramUser -> paramUser.birthDate = LocalDate.ofYearDay(1977, 123))
      .with(paramUser -> paramUser.fullName = String.format("{{%s.fullName}}", new Object[] { paramString })).with(paramUser -> paramUser.firstName = String.format("{{%s.firstName}}", new Object[] { paramString })).with(paramUser -> paramUser.middleName = String.format("{{%s.middleName}}", new Object[] { paramString })).with(paramUser -> paramUser.lastName = String.format("{{%s.lastName}}", new Object[] { paramString })).with(paramUser -> paramUser.expiry = ZonedDateTime.now(ZoneOffset.UTC).plusDays(90L))
      .with(paramUser -> paramUser.active = true)
      .with(paramUser -> paramUser.timezone = ZoneId.of("America/Denver"))
      .with(paramUser -> paramUser.cleanSpeakId = UUID.randomUUID())
      .with(paramUser -> paramUser.lastLoginInstant = ZonedDateTime.now(ZoneOffset.UTC))
      .with(paramUser -> paramUser.data = new MockUserData(String.format("%s.data", new Object[] { paramString }))).with(paramUser -> paramUser.usernameStatus = ContentStatus.ACTIVE)
      .with(paramUser -> paramUser.tenantId = paramUUID)
      .with(paramUser -> paramUser.twoFactor.methods.add(TwoFactorTools.newAuthenticatorTwoFactor(String.format("{{%s.twoFactor.methods[0].secret}}", new Object[] { paramString })))).with(paramUser -> paramUser.twoFactor.methods.add(TwoFactorTools.newEmailTwoFactor("user@example.com").with(())))
      .with(paramUser -> paramUser.twoFactor.methods.add(TwoFactorTools.newSMSTwoFactor("555-555-5555").with(())))
      .with(paramUser -> paramUser.getRegistrations().add((new UserRegistration(new MockUserData(String.format("%s.registrations[0].data", new Object[] { paramString })))).with(()).with(()).with(()).with(()).with(()).with(()).with(()).with(())));
  }
  
  protected static class MockUserData extends LinkedHashMap<String, Object> {
    public String name;
    
    public MockUserData(String param1String) {
      this.name = param1String;
    }
    
    public Object get(Object param1Object) {
      return new MockUserData(this.name + "." + this.name);
    }
  }
}
