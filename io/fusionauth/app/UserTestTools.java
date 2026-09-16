package io.fusionauth.app;

import com.inversoft.util.MapBuilder;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ContentStatus;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class UserTestTools {
  public static final UUID AudreyId = UUID.randomUUID();
  
  public static final UUID ClarkId = UUID.randomUUID();
  
  public static final UUID EllenId = UUID.randomUUID();
  
  public static final UUID HanSoloId = UUID.fromString("b125a070-71c4-11e5-a837-0800200c9a69");
  
  public static final UUID RustyId = UUID.randomUUID();
  
  public static UserRegistration baseRegistration() {
    return (new UserRegistration()).with(paramUserRegistration -> paramUserRegistration.id = UUID.randomUUID())
      .with(paramUserRegistration -> paramUserRegistration.lastLoginInstant = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1L))
      .with(paramUserRegistration -> paramUserRegistration.usernameStatus = ContentStatus.ACTIVE);
  }
  
  public static User baseUser() {
    return (new User()).with(paramUser -> paramUser.password = "abc")
      .with(paramUser -> paramUser.salt = "abc")
      .with(paramUser -> paramUser.encryptionScheme = "salted-sha256")
      .with(paramUser -> paramUser.active = true)
      .with(paramUser -> paramUser.timezone = ZoneId.of("America/Denver"))
      .with(paramUser -> paramUser.verified = true)
      .with(paramUser -> paramUser.insertInstant = ZonedDateTime.now(ZoneOffset.UTC))
      .with(paramUser -> paramUser.usernameStatus = ContentStatus.ACTIVE);
  }
  
  public static List<User> makeSpecialUsers(Application paramApplication, String paramString) {
    String str1 = paramString + "images/test-profile-images/han-solo.jpg";
    String str2 = paramString + "images/test-profile-images/ellen.jpg";
    String str3 = paramString + "images/test-profile-images/rusty.jpg";
    String str4 = paramString + "images/test-profile-images/audrey.jpg";
    String str5 = paramString + "images/test-profile-images/clark.jpg";
    User user1 = baseUser().with(paramUser -> paramUser.id = HanSoloId).with(paramUser -> paramUser.email = "han@example.com").with(paramUser -> paramUser.username = "hanSolo42").with(paramUser -> paramUser.birthDate = LocalDate.of(1965, 2, 27)).with(paramUser -> paramUser.factor = Integer.valueOf(1)).with(paramUser -> paramUser.passwordLastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC)).with(paramUser -> paramUser.fullName = "Han Solo").with(paramUser -> paramUser.firstName = "Han").with(paramUser -> paramUser.lastName = "Solo").with(paramUser -> paramUser.timezone = ZoneId.of("America/Denver")).with(paramUser -> paramUser.imageUrl = URI.create(paramString)).with(paramUser -> paramUser.preferredLanguages.addAll(Arrays.asList(new Locale[] { Locale.ENGLISH, Locale.FRENCH }))).with(paramUser -> paramUser.getRegistrations().add(baseRegistration().with(()).with(()).with(()))).with(paramUser -> paramUser.data.putAll((new MapBuilder()).put("favoriteShip", "Millennium Falcon").put("lifeTimeValue", Integer.valueOf(125)).map()));
    User user2 = baseUser().with(paramUser -> paramUser.id = EllenId).with(paramUser -> paramUser.email = "ellen@griswold.com").with(paramUser -> paramUser.username = "ellen-griswold").with(paramUser -> paramUser.birthDate = LocalDate.of(1979, 1, 10)).with(paramUser -> paramUser.factor = Integer.valueOf(1)).with(paramUser -> paramUser.passwordLastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC)).with(paramUser -> paramUser.fullName = "Ellen Griswold").with(paramUser -> paramUser.firstName = "Ellen").with(paramUser -> paramUser.lastName = "Griswold").with(paramUser -> paramUser.imageUrl = URI.create(paramString)).with(paramUser -> paramUser.preferredLanguages.addAll(Arrays.asList(new Locale[] { Locale.ENGLISH, Locale.FRENCH }))).with(paramUser -> paramUser.getRegistrations().add(baseRegistration().with(()).with(()).with(()))).with(paramUser -> paramUser.data.putAll((new MapBuilder()).put("city", "Chicago").put("state", "IL").put("lifeTimeValue", Integer.valueOf(50)).map()));
    User user3 = baseUser().with(paramUser -> paramUser.id = RustyId).with(paramUser -> paramUser.email = "rusty@griswold.com").with(paramUser -> paramUser.username = "rustyG").with(paramUser -> paramUser.birthDate = LocalDate.of(2006, 1, 10)).with(paramUser -> paramUser.factor = Integer.valueOf(1)).with(paramUser -> paramUser.passwordLastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC)).with(paramUser -> paramUser.fullName = "Rusty Griswold").with(paramUser -> paramUser.firstName = "Rusty").with(paramUser -> paramUser.lastName = "Griswold").with(paramUser -> paramUser.imageUrl = URI.create(paramString)).with(paramUser -> paramUser.preferredLanguages.addAll(Arrays.asList(new Locale[] { Locale.ENGLISH, Locale.FRENCH }))).with(paramUser -> paramUser.getRegistrations().add(baseRegistration().with(()).with(()).with(()))).with(paramUser -> paramUser.data.putAll((new MapBuilder()).put("city", "Chicago").put("state", "IL").put("lifeTimeValue", Integer.valueOf(5)).map()));
    User user4 = baseUser().with(paramUser -> paramUser.id = AudreyId).with(paramUser -> paramUser.email = "audrey@griswold.com").with(paramUser -> paramUser.username = "audrey").with(paramUser -> paramUser.birthDate = LocalDate.of(2009, 1, 10)).with(paramUser -> paramUser.factor = Integer.valueOf(1)).with(paramUser -> paramUser.passwordLastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC)).with(paramUser -> paramUser.fullName = "Audrey Griswold").with(paramUser -> paramUser.firstName = "Audrey").with(paramUser -> paramUser.lastName = "Griswold").with(paramUser -> paramUser.imageUrl = URI.create(paramString)).with(paramUser -> paramUser.preferredLanguages.addAll(Arrays.asList(new Locale[] { Locale.ENGLISH, Locale.FRENCH }))).with(paramUser -> paramUser.getRegistrations().add(baseRegistration().with(()).with(()).with(()))).with(paramUser -> paramUser.data.putAll((new MapBuilder()).put("city", "Chicago").put("state", "IL").map()));
    User user5 = baseUser().with(paramUser -> paramUser.id = ClarkId).with(paramUser -> paramUser.email = "clark@griswold.com").with(paramUser -> paramUser.username = "clark").with(paramUser -> paramUser.birthDate = LocalDate.of(1976, 1, 10)).with(paramUser -> paramUser.factor = Integer.valueOf(1)).with(paramUser -> paramUser.passwordLastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC)).with(paramUser -> paramUser.fullName = "Clark Griswold").with(paramUser -> paramUser.firstName = "Clark").with(paramUser -> paramUser.lastName = "Griswold").with(paramUser -> paramUser.imageUrl = URI.create(paramString)).with(paramUser -> paramUser.preferredLanguages.addAll(Arrays.asList(new Locale[] { Locale.ENGLISH, Locale.FRENCH }))).with(paramUser -> paramUser.getRegistrations().add(baseRegistration().with(()).with(()).with(()))).with(paramUser -> paramUser.data.putAll((new MapBuilder()).put("city", "Chicago").put("state", "IL").map()));
    return Arrays.asList(new User[] { user1, user2, user3, user4, user5 });
  }
}
