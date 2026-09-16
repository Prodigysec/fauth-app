package io.fusionauth.app.primeframework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.primeframework.mvc.action.result.annotation.ResultAnnotation;
import org.primeframework.mvc.action.result.annotation.ResultContainerAnnotation;

@ResultAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ThemedForward {
  String cacheControl() default "no-cache";
  
  String code() default "success";
  
  String contentType() default "text/html; charset=UTF-8";
  
  boolean disableCacheControl() default false;
  
  String page() default "";
  
  int status() default 200;
  
  String statusStr() default "";
  
  @ResultContainerAnnotation
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE})
  public static @interface List {
    ThemedForward[] value();
  }
}
