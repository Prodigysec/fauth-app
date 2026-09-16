package io.fusionauth.api.util;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;

public class BinderTools {
  public static <U extends com.google.inject.Provider<T>, T> void bindToEagerSingletonProvider(Binder paramBinder, Class<U> paramClass, Class<T> paramClass1) {
    paramBinder.bind(paramClass).asEagerSingleton();
    paramBinder.bind(paramClass1).toProvider(paramClass);
  }
  
  public static <U extends com.google.inject.Provider<T>, T> void bindToEagerSingletonProvider(Binder paramBinder, Class<U> paramClass, TypeLiteral<T> paramTypeLiteral) {
    paramBinder.bind(paramClass).asEagerSingleton();
    paramBinder.bind(paramTypeLiteral).toProvider(paramClass);
  }
}
