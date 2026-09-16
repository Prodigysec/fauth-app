package io.fusionauth.api.service.reactor;

import com.inversoft.validator.Validator;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.util.function.Consumer;
import java.util.function.Function;

public class ReactorStatusValidator {
  public static void ifNotLicensedForThen(ReactorStatus paramReactorStatus, Function<ReactorStatus, ReactorFeatureStatus> paramFunction, Consumer<String> paramConsumer) {
    if (paramReactorStatus.licensed) {
      if (paramFunction.apply(paramReactorStatus) != ReactorFeatureStatus.ACTIVE)
        paramConsumer.accept("[notLicensedFor]"); 
    } else {
      paramConsumer.accept("[notLicensed]");
    } 
  }
  
  public static boolean isLicensedFor(ReactorStatus paramReactorStatus, Function<ReactorStatus, ReactorFeatureStatus> paramFunction) {
    return (paramReactorStatus.licensed && paramFunction.apply(paramReactorStatus) == ReactorFeatureStatus.ACTIVE);
  }
  
  public static void isLicensedFor(Validator paramValidator, ReactorStatus paramReactorStatus, Function<ReactorStatus, ReactorFeatureStatus> paramFunction, String paramString) {
    paramValidator.ensure(paramReactorStatus.licensed, paramString, "[notLicensed]", new Object[0])
      .ifLastCheckHadNoError(paramValidator -> paramValidator.ensure((paramFunction.apply(paramReactorStatus) == ReactorFeatureStatus.ACTIVE), paramString, "[notLicensedFor]", new Object[0]));
  }
  
  public static boolean isNotLicensedFor(ReactorStatus paramReactorStatus, Function<ReactorStatus, ReactorFeatureStatus> paramFunction) {
    return (!paramReactorStatus.licensed || paramFunction.apply(paramReactorStatus) != ReactorFeatureStatus.ACTIVE);
  }
}
