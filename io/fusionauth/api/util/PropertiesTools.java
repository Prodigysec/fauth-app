package io.fusionauth.api.util;

import com.inversoft.util.Pair;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class PropertiesTools {
  public static Properties loadProperties(String paramString) throws IOException {
    return loadProperties(paramString, null);
  }
  
  public static Properties loadProperties(String paramString, Properties paramProperties) throws IOException {
    if (paramString == null)
      return new Properties(paramProperties); 
    StringReader stringReader = new StringReader(paramString);
    try {
      Properties properties1 = new Properties(paramProperties);
      properties1.load(stringReader);
      Properties properties2 = properties1;
      stringReader.close();
      return properties2;
    } catch (Throwable throwable) {
      try {
        stringReader.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  public static Pair<Properties, List<Pair<String, String>>> loadPropertiesAndMapKeys(String paramString) {
    try {
      StringReader stringReader = new StringReader(paramString);
      try {
        Pair<Properties, List<Pair<String, String>>> pair = load0(new LineReader(stringReader));
        stringReader.close();
        return pair;
      } catch (Throwable throwable) {
        try {
          stringReader.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
    } catch (IOException iOException) {
      throw new RuntimeException(iOException);
    } 
  }
  
  public static boolean validate(String paramString) {
    if (paramString == null)
      return true; 
    try {
      (new Properties()).load(new StringReader(paramString));
      return true;
    } catch (Exception exception) {
      return false;
    } 
  }
  
  private static Pair<Properties, List<Pair<String, String>>> load0(LineReader paramLineReader) throws IOException {
    Properties properties = new Properties();
    LinkedList<Pair> linkedList = new LinkedList();
    StringBuilder stringBuilder = new StringBuilder();
    Pair<Boolean, Integer> pair = paramLineReader.readLine();
    while (((Boolean)pair.first).booleanValue() || ((Integer)pair.second).intValue() >= 0) {
      if (((Boolean)pair.first).booleanValue() && ((Integer)pair.second).intValue() >= 0) {
        int i = ((Integer)pair.second).intValue();
        byte b = 0;
        int j = i;
        boolean bool1 = false;
        boolean bool2 = false;
        while (b < i) {
          char c = paramLineReader.lineBuf[b];
          if ((c == '=' || c == ':') && !bool2) {
            j = b + 1;
            bool1 = true;
            break;
          } 
          if ((c == ' ' || c == '\t' || c == '\f') && !bool2) {
            j = b + 1;
            break;
          } 
          if (c == '\\') {
            bool2 = !bool2 ? true : false;
          } else {
            bool2 = false;
          } 
          b++;
        } 
        while (j < i) {
          char c = paramLineReader.lineBuf[j];
          if (c != ' ' && c != '\t' && c != '\f')
            if (!bool1 && (c == '=' || c == ':')) {
              bool1 = true;
            } else {
              break;
            }  
          j++;
        } 
        String str1 = new String(paramLineReader.lineBuf, 0, i);
        String str2 = loadConvert(paramLineReader.lineBuf, 0, b, stringBuilder);
        String str3 = loadConvert(paramLineReader.lineBuf, j, i - j, stringBuilder);
        properties.put(str2, str3);
        linkedList.add(new Pair(str1, str2));
      } else {
        String str = (((Integer)pair.second).intValue() > 0) ? new String(paramLineReader.lineBuf, 0, ((Integer)pair.second).intValue()) : "";
        linkedList.add(new Pair(str, null));
        stringBuilder.setLength(0);
      } 
      pair = paramLineReader.readLine();
    } 
    return new Pair(properties, linkedList);
  }
  
  private static String loadConvert(char[] paramArrayOfchar, int paramInt1, int paramInt2, StringBuilder paramStringBuilder) {
    // Byte code:
    //   0: iload_1
    //   1: iload_2
    //   2: iadd
    //   3: istore #5
    //   5: iload_1
    //   6: istore #6
    //   8: iload_1
    //   9: iload #5
    //   11: if_icmpge -> 32
    //   14: aload_0
    //   15: iload_1
    //   16: iinc #1, 1
    //   19: caload
    //   20: istore #4
    //   22: iload #4
    //   24: bipush #92
    //   26: if_icmpne -> 8
    //   29: goto -> 32
    //   32: iload_1
    //   33: iload #5
    //   35: if_icmpne -> 50
    //   38: new java/lang/String
    //   41: dup
    //   42: aload_0
    //   43: iload #6
    //   45: iload_2
    //   46: invokespecial <init> : ([CII)V
    //   49: areturn
    //   50: aload_3
    //   51: iconst_0
    //   52: invokevirtual setLength : (I)V
    //   55: iinc #1, -1
    //   58: aload_3
    //   59: aload_0
    //   60: iload #6
    //   62: iload_1
    //   63: iload #6
    //   65: isub
    //   66: invokevirtual append : ([CII)Ljava/lang/StringBuilder;
    //   69: pop
    //   70: iload_1
    //   71: iload #5
    //   73: if_icmpge -> 527
    //   76: aload_0
    //   77: iload_1
    //   78: iinc #1, 1
    //   81: caload
    //   82: istore #4
    //   84: iload #4
    //   86: bipush #92
    //   88: if_icmpne -> 517
    //   91: aload_0
    //   92: iload_1
    //   93: iinc #1, 1
    //   96: caload
    //   97: istore #4
    //   99: iload #4
    //   101: bipush #117
    //   103: if_icmpne -> 454
    //   106: iload_1
    //   107: iload #5
    //   109: iconst_4
    //   110: isub
    //   111: if_icmple -> 124
    //   114: new java/lang/IllegalArgumentException
    //   117: dup
    //   118: ldc 'Malformed \uxxxx encoding.'
    //   120: invokespecial <init> : (Ljava/lang/String;)V
    //   123: athrow
    //   124: iconst_0
    //   125: istore #7
    //   127: iconst_0
    //   128: istore #8
    //   130: iload #8
    //   132: iconst_4
    //   133: if_icmpge -> 443
    //   136: aload_0
    //   137: iload_1
    //   138: iinc #1, 1
    //   141: caload
    //   142: istore #4
    //   144: iload #4
    //   146: tableswitch default -> 425, 48 -> 380, 49 -> 380, 50 -> 380, 51 -> 380, 52 -> 380, 53 -> 380, 54 -> 380, 55 -> 380, 56 -> 380, 57 -> 380, 58 -> 425, 59 -> 425, 60 -> 425, 61 -> 425, 62 -> 425, 63 -> 425, 64 -> 425, 65 -> 409, 66 -> 409, 67 -> 409, 68 -> 409, 69 -> 409, 70 -> 409, 71 -> 425, 72 -> 425, 73 -> 425, 74 -> 425, 75 -> 425, 76 -> 425, 77 -> 425, 78 -> 425, 79 -> 425, 80 -> 425, 81 -> 425, 82 -> 425, 83 -> 425, 84 -> 425, 85 -> 425, 86 -> 425, 87 -> 425, 88 -> 425, 89 -> 425, 90 -> 425, 91 -> 425, 92 -> 425, 93 -> 425, 94 -> 425, 95 -> 425, 96 -> 425, 97 -> 393, 98 -> 393, 99 -> 393, 100 -> 393, 101 -> 393, 102 -> 393
    //   380: iload #7
    //   382: iconst_4
    //   383: ishl
    //   384: iload #4
    //   386: iadd
    //   387: bipush #48
    //   389: isub
    //   390: goto -> 435
    //   393: iload #7
    //   395: iconst_4
    //   396: ishl
    //   397: bipush #10
    //   399: iadd
    //   400: iload #4
    //   402: iadd
    //   403: bipush #97
    //   405: isub
    //   406: goto -> 435
    //   409: iload #7
    //   411: iconst_4
    //   412: ishl
    //   413: bipush #10
    //   415: iadd
    //   416: iload #4
    //   418: iadd
    //   419: bipush #65
    //   421: isub
    //   422: goto -> 435
    //   425: new java/lang/IllegalArgumentException
    //   428: dup
    //   429: ldc 'Malformed \uxxxx encoding.'
    //   431: invokespecial <init> : (Ljava/lang/String;)V
    //   434: athrow
    //   435: istore #7
    //   437: iinc #8, 1
    //   440: goto -> 130
    //   443: aload_3
    //   444: iload #7
    //   446: i2c
    //   447: invokevirtual append : (C)Ljava/lang/StringBuilder;
    //   450: pop
    //   451: goto -> 70
    //   454: iload #4
    //   456: bipush #116
    //   458: if_icmpne -> 468
    //   461: bipush #9
    //   463: istore #4
    //   465: goto -> 507
    //   468: iload #4
    //   470: bipush #114
    //   472: if_icmpne -> 482
    //   475: bipush #13
    //   477: istore #4
    //   479: goto -> 507
    //   482: iload #4
    //   484: bipush #110
    //   486: if_icmpne -> 496
    //   489: bipush #10
    //   491: istore #4
    //   493: goto -> 507
    //   496: iload #4
    //   498: bipush #102
    //   500: if_icmpne -> 507
    //   503: bipush #12
    //   505: istore #4
    //   507: aload_3
    //   508: iload #4
    //   510: invokevirtual append : (C)Ljava/lang/StringBuilder;
    //   513: pop
    //   514: goto -> 70
    //   517: aload_3
    //   518: iload #4
    //   520: invokevirtual append : (C)Ljava/lang/StringBuilder;
    //   523: pop
    //   524: goto -> 70
    //   527: aload_3
    //   528: invokevirtual toString : ()Ljava/lang/String;
    //   531: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #192	-> 0
    //   #193	-> 5
    //   #194	-> 8
    //   #195	-> 14
    //   #196	-> 22
    //   #197	-> 29
    //   #200	-> 32
    //   #201	-> 38
    //   #205	-> 50
    //   #206	-> 55
    //   #207	-> 58
    //   #209	-> 70
    //   #210	-> 76
    //   #211	-> 84
    //   #214	-> 91
    //   #215	-> 99
    //   #217	-> 106
    //   #218	-> 114
    //   #221	-> 124
    //   #222	-> 127
    //   #223	-> 136
    //   #224	-> 144
    //   #225	-> 380
    //   #226	-> 393
    //   #227	-> 409
    //   #228	-> 425
    //   #229	-> 435
    //   #222	-> 437
    //   #231	-> 443
    //   #232	-> 451
    //   #233	-> 454
    //   #234	-> 461
    //   #235	-> 468
    //   #236	-> 475
    //   #237	-> 482
    //   #238	-> 489
    //   #239	-> 496
    //   #240	-> 503
    //   #242	-> 507
    //   #245	-> 517
    //   #248	-> 527
  }
  
  private static class LineReader {
    private final char[] inCharBuf;
    
    private final Reader reader;
    
    char[] lineBuf = new char[1024];
    
    private int inLimit = 0;
    
    private int inOff = 0;
    
    LineReader(Reader param1Reader) {
      this.reader = param1Reader;
      this.inCharBuf = new char[8192];
    }
    
    Pair<Boolean, Integer> readLine() throws IOException {
      byte b = 0;
      int i = this.inOff;
      int j = this.inLimit;
      boolean bool1 = true;
      boolean bool2 = false;
      boolean bool3 = false;
      char[] arrayOfChar1 = this.inCharBuf;
      char[] arrayOfChar2 = this.lineBuf;
      while (true) {
        if (i >= j) {
          this.inLimit = j = this.reader.read(arrayOfChar1);
          if (j <= 0) {
            if (!b)
              return new Pair(Boolean.valueOf(false), Integer.valueOf(-1)); 
            return new Pair(Boolean.valueOf(true), Integer.valueOf(bool3 ? (b - 1) : b));
          } 
          i = 0;
        } 
        char c = arrayOfChar1[i++];
        if (bool1) {
          if (c == ' ' || c == '\t' || c == '\f')
            continue; 
          if (!bool2 && (c == '\r' || c == '\n')) {
            this.inOff = i;
            return new Pair(Boolean.valueOf(true), Integer.valueOf(0));
          } 
          bool1 = false;
          bool2 = false;
        } 
        if (b == 0 && (
          c == '#' || c == '!')) {
          while (true) {
            arrayOfChar2[b++] = c;
            while (i < j) {
              c = arrayOfChar1[i++];
              if (c <= '\r' && (c == '\r' || c == '\n')) {
                this.inOff = i;
                return new Pair(Boolean.valueOf(false), Integer.valueOf(b));
              } 
              arrayOfChar2[b++] = c;
            } 
            this.inLimit = j = this.reader.read(arrayOfChar1);
            if (j <= 0)
              return new Pair(Boolean.valueOf(true), Integer.valueOf(b)); 
            i = 0;
            c = arrayOfChar1[i++];
          } 
          continue;
        } 
        if (c != '\n' && c != '\r') {
          arrayOfChar2[b++] = c;
          if (b == arrayOfChar2.length) {
            arrayOfChar2 = new char[b * 2];
            System.arraycopy(this.lineBuf, 0, arrayOfChar2, 0, b);
            this.lineBuf = arrayOfChar2;
          } 
          bool3 = (c == '\\' && !bool3) ? true : false;
          continue;
        } 
        if (b == 0) {
          bool1 = true;
          continue;
        } 
        if (i >= j) {
          this.inLimit = j = this.reader.read(arrayOfChar1);
          i = 0;
          if (j <= 0) {
            this.inOff = i;
            return new Pair(Boolean.valueOf(true), Integer.valueOf(bool3 ? (b - 1) : b));
          } 
        } 
        if (bool3) {
          b--;
          bool1 = true;
          bool2 = true;
          bool3 = false;
          if (c == '\r' && 
            arrayOfChar1[i] == '\n')
            i++; 
          continue;
        } 
        break;
      } 
      this.inOff = i;
      return new Pair(Boolean.valueOf(true), Integer.valueOf(b));
    }
  }
}
