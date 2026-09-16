package io.fusionauth.api.domain.json.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface MaskString {
  public static final String DEFAULT = "*****";
  
  int showLast() default 0;
  
  String value() default "*****";
}
