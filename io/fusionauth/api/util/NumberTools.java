package io.fusionauth.api.util;

import java.text.DecimalFormat;

public class NumberTools {
  public static String format(long paramLong) {
    return (new DecimalFormat("#,###,###,##0")).format(paramLong);
  }
  
  public static String format(double paramDouble) {
    return (new DecimalFormat("#,###,###,##0.###")).format(paramDouble);
  }
  
  public static String format(int paramInt) {
    return (new DecimalFormat("#,###,###,##0")).format(paramInt);
  }
}
