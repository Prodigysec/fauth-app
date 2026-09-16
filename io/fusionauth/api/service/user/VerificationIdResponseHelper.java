package io.fusionauth.api.service.user;

import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.api.UserResponse;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class VerificationIdResponseHelper {
  public static List<UserResponse.VerificationId> getSortedVerificationIds(Map<UserIdentity, ExternalIdentifier> paramMap, BiConsumer<ExternalIdentifier, UserIdentity> paramBiConsumer) {
    List<UserResponse.VerificationId> list = paramMap.entrySet().stream().sorted(Comparator.comparing(paramEntry -> ((UserIdentity)paramEntry.getKey()).insertInstant).thenComparing(paramEntry -> ((UserIdentity)paramEntry.getKey()).type).thenComparing(paramEntry -> ((UserIdentity)paramEntry.getKey()).value)).map(paramEntry -> {
          UserIdentity userIdentity = (UserIdentity)paramEntry.getKey();
          ExternalIdentifier externalIdentifier = (ExternalIdentifier)paramEntry.getValue();
          if (paramBiConsumer != null)
            paramBiConsumer.accept(externalIdentifier, userIdentity); 
          return (new UserResponse.VerificationId()).with(()).with(()).with(()).with(());
        }).toList();
    if (list.isEmpty())
      return null; 
    return list;
  }
}
