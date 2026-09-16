package io.fusionauth.domain.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class DefaultTools {
  public static <T> void addIfEmpty(List<T> paramList, T paramT) {
    if (paramList.isEmpty())
      paramList.add(paramT); 
  }
  
  public static <T> T defaultIfNull(T paramT1, T paramT2) {
    return Optional.<T>ofNullable(paramT1).orElse(paramT2);
  }
  
  public static <T> T defaultIfNull(T paramT, Supplier<T> paramSupplier) {
    return Optional.<T>ofNullable(paramT).orElse(paramSupplier.get());
  }
  
  public static int defaultIfZero(int paramInt1, int paramInt2) {
    return (paramInt1 != 0) ? paramInt1 : paramInt2;
  }
  
  public static long defaultIfZero(long paramLong1, long paramLong2) {
    return (paramLong1 != 0L) ? paramLong1 : paramLong2;
  }
}
