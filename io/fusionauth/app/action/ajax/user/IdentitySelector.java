package io.fusionauth.app.action.ajax.user;

import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.UserIdentity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public interface IdentitySelector {
  default List<UserIdentity> prepareFormIdentities(List<UserIdentity> paramList, Predicate<UserIdentity> paramPredicate) {
    return new ArrayList<>(paramList
        .stream()
        .filter(paramPredicate)




        
        .sorted(Comparator.comparing(paramObject -> Boolean.valueOf(((UserIdentity)paramObject).primary))
          
          .reversed()
          .thenComparing(paramObject -> ((UserIdentity)paramObject).type)
          .thenComparing(paramObject -> ((UserIdentity)paramObject).value))
        
        .toList());
  }
  
  default UserIdentity resolveUserIdentity(List<UserIdentity> paramList, String paramString) {
    return paramList
      .stream()
      .filter(paramUserIdentity -> (paramUserIdentity.type.isNot(IdentityType.username) && paramUserIdentity.value.equals(paramString)))
      .findFirst()
      .orElse(null);
  }
  
  default Errors validateSelectedIdentity(String paramString, UserIdentity paramUserIdentity) {
    return (new Validator())
      .notBlank(paramString, "loginId", new Object[0])
      
      .ifLastCheckHadNoError(paramValidator -> paramValidator.validObject(paramUserIdentity, "loginId", new Object[0]))

      
      .done();
  }
}
