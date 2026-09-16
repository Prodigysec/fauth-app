package io.fusionauth.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PublicDoc {
  String description() default "this is a test description";
  
  String extraDoc() default "";
  
  String name() default "";
  
  String since() default "";
}
