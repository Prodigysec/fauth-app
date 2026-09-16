package io.fusionauth.api.domain.json.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface MaskMapValue {
  public static final String DEFAULT = "*****";
  
  public static final boolean caseSensitive = false;
  
  boolean maskAll() default false;
  
  String replacement() default "*****";
  
  int showLast() default 0;
  
  String value() default "";
  
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD})
  public static @interface List {
    MaskMapValue[] value();
  }
}
