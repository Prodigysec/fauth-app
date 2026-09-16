package io.fusionauth.api.util;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class CIDRBlock {
  private final byte[] address;
  
  private final BigInteger mask;
  
  public CIDRBlock(String paramString) {
    try {
      String[] arrayOfString = paramString.split("/");
      InetAddress inetAddress = InetAddress.getByName(arrayOfString[0]);
      if (arrayOfString.length == 1)
        arrayOfString = new String[] { paramString, (inetAddress instanceof java.net.Inet4Address) ? "32" : "128" }; 
      int i = Integer.parseInt(arrayOfString[1]);
      byte b = (inetAddress instanceof java.net.Inet4Address) ? 32 : 128;
      if (b == 32 && i > b)
        i = 32 - 128 - i; 
      this.address = inetAddress.getAddress();
      this
        
        .mask = BigInteger.ONE.shiftLeft(i).subtract(BigInteger.ONE).shiftLeft(b - i);
    } catch (UnknownHostException unknownHostException) {
      throw new RuntimeException(unknownHostException);
    } 
  }
  
  public boolean contains(String paramString) {
    try {
      return (new BigInteger(InetAddress.getByName(paramString).getAddress()))
        .xor(new BigInteger(this.address))
        .and(this.mask)
        .equals(BigInteger.ZERO);
    } catch (UnknownHostException unknownHostException) {
      return false;
    } 
  }
}
