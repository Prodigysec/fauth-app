package io.fusionauth.api.util;

import java.util.Random;

public class UsernameTools {
  public static Random random = new Random();
  
  public static String generateRandomSuffix(int paramInt) {
    StringBuilder stringBuilder = new StringBuilder(paramInt);
    for (byte b = 0; b < paramInt; b++) {
      int i = random.nextInt(11);
      if (i != 10)
        stringBuilder.append(i); 
    } 
    return stringBuilder.toString();
  }
}
