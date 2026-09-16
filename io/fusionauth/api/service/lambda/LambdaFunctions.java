package io.fusionauth.api.service.lambda;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

public class LambdaFunctions {
  public static String encodedGuidToString(String paramString) {
    byte[] arrayOfByte = Base64.getDecoder().decode(paramString);
    long l1 = ByteBuffer.allocate(8).put(3, arrayOfByte[0]).put(2, arrayOfByte[1]).put(1, arrayOfByte[2]).put(0, arrayOfByte[3]).put(5, arrayOfByte[4]).put(4, arrayOfByte[5]).put(7, arrayOfByte[6]).put(6, arrayOfByte[7]).getLong();
    long l2 = ByteBuffer.wrap(arrayOfByte, 8, 8).getLong();
    return (new UUID(l1, l2)).toString();
  }
}
