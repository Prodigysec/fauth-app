package io.fusionauth.api.service.oauth2;

import com.inversoft.util.StringTools;
import io.fusionauth.api.domain.TokenResult;
import io.fusionauth.api.service.jwt.FusionAuthJWTDecoder;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.jwt.InvalidJWTException;
import io.fusionauth.jwt.domain.Algorithm;
import io.fusionauth.jwt.domain.JWT;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultDPoPService implements DPoPService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultDPoPService.class);
  
  @Nullable
  public OAuthError checkRefreshToken(@Nonnull RefreshToken paramRefreshToken, @Nullable String paramString) {
    Object object = paramRefreshToken.data.get("DPoPThumbprint");
    if (object != null) {
      if (paramString == null)
        return new OAuthError(OAuthError.OAuthErrorType.invalid_dpop_proof, "The request is missing a DPoP proof header."); 
      if (!paramString.equals(object))
        return new OAuthError(OAuthError.OAuthErrorType.invalid_dpop_proof, "Invalid DPoP proof header"); 
    } else if (paramString != null) {
      return new OAuthError(OAuthError.OAuthErrorType.invalid_dpop_proof, "Invalid DPoP proof header");
    } 
    return null;
  }
  
  @Nonnull
  public TokenResult extractAccessTokenFromAuthorizationHeader(HTTPRequest paramHTTPRequest) {
    List list = paramHTTPRequest.getHeaders("Authorization");
    if (list != null && list.size() >= 2)
      return TokenResult.error(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.access_token_invalid, "Multiple methods used to include access token")); 
    if (list == null || list.isEmpty())
      return TokenResult.None; 
    String str = (String)list.getFirst();
    if (StringTools.isNotBlank(str)) {
      String str1 = "bearer ";
      String str2 = "dpop ";
      if (str.toLowerCase().startsWith(str2)) {
        String str3 = str.substring(str2.length());
        DPoPService.DPoPResult dPoPResult = parseDPoPRequired(paramHTTPRequest, str3);
        return TokenResult.dpop(dPoPResult, str3);
      } 
      if (str.toLowerCase().startsWith(str1)) {
        String str3 = str.substring(str1.length());
        if (isJWTDPoPBound(str3))
          return TokenResult.error(new OAuthError(OAuthError.OAuthErrorType.invalid_token, OAuthError.OAuthErrorReason.access_token_invalid, "downgraded use of DPoP bound token")); 
        return TokenResult.bearer(str3);
      } 
    } 
    return TokenResult.None;
  }
  
  public boolean isJWTDPoPBound(String paramString) {
    JWT jWT;
    if (paramString == null)
      return false; 
    try {
      jWT = FusionAuthJWTDecoder.unsafeDecode(paramString);
    } catch (InvalidJWTException invalidJWTException) {
      return false;
    } 
    return jWT.getOtherClaims().containsKey("cnf");
  }
  
  public DPoPService.DPoPResult parseDPoP(HTTPRequest paramHTTPRequest) {
    return parseDPoP(paramHTTPRequest, null);
  }
  
  public DPoPService.DPoPResult parseDPoP(HTTPRequest paramHTTPRequest, @Nullable String paramString) {
    // Byte code:
    //   0: aload_1
    //   1: ldc 'Dpop'
    //   3: invokevirtual getHeaders : (Ljava/lang/String;)Ljava/util/List;
    //   6: astore_3
    //   7: aload_3
    //   8: ifnull -> 20
    //   11: aload_3
    //   12: invokeinterface isEmpty : ()Z
    //   17: ifeq -> 24
    //   20: getstatic io/fusionauth/api/service/oauth2/DPoPService$DPoPResult.None : Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;
    //   23: areturn
    //   24: aload_3
    //   25: invokeinterface size : ()I
    //   30: iconst_1
    //   31: if_icmple -> 40
    //   34: ldc 'Only one DPoP header allowed'
    //   36: invokestatic error : (Ljava/lang/String;)Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;
    //   39: areturn
    //   40: aload_3
    //   41: invokeinterface getFirst : ()Ljava/lang/Object;
    //   46: checkcast java/lang/String
    //   49: astore #4
    //   51: aload #4
    //   53: invokestatic decodeHeader : (Ljava/lang/String;)Lio/fusionauth/jwt/domain/Header;
    //   56: astore #5
    //   58: aload #5
    //   60: getfield type : Ljava/lang/String;
    //   63: ifnull -> 79
    //   66: aload #5
    //   68: getfield type : Ljava/lang/String;
    //   71: ldc 'dpop+jwt'
    //   73: invokevirtual equals : (Ljava/lang/Object;)Z
    //   76: ifne -> 85
    //   79: ldc 'DPoP JWT header requires a typ parameter of dpop+jwt'
    //   81: invokestatic error : (Ljava/lang/String;)Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;
    //   84: areturn
    //   85: bipush #11
    //   87: anewarray io/fusionauth/jwt/domain/Algorithm
    //   90: dup
    //   91: iconst_0
    //   92: getstatic io/fusionauth/jwt/domain/Algorithm.Ed448 : Lio/fusionauth/jwt/domain/Algorithm;
    //   95: aastore
    //   96: dup
    //   97: iconst_1
    //   98: getstatic io/fusionauth/jwt/domain/Algorithm.Ed25519 : Lio/fusionauth/jwt/domain/Algorithm;
    //   101: aastore
    //   102: dup
    //   103: iconst_2
    //   104: getstatic io/fusionauth/jwt/domain/Algorithm.ES256 : Lio/fusionauth/jwt/domain/Algorithm;
    //   107: aastore
    //   108: dup
    //   109: iconst_3
    //   110: getstatic io/fusionauth/jwt/domain/Algorithm.ES384 : Lio/fusionauth/jwt/domain/Algorithm;
    //   113: aastore
    //   114: dup
    //   115: iconst_4
    //   116: getstatic io/fusionauth/jwt/domain/Algorithm.ES512 : Lio/fusionauth/jwt/domain/Algorithm;
    //   119: aastore
    //   120: dup
    //   121: iconst_5
    //   122: getstatic io/fusionauth/jwt/domain/Algorithm.PS256 : Lio/fusionauth/jwt/domain/Algorithm;
    //   125: aastore
    //   126: dup
    //   127: bipush #6
    //   129: getstatic io/fusionauth/jwt/domain/Algorithm.PS384 : Lio/fusionauth/jwt/domain/Algorithm;
    //   132: aastore
    //   133: dup
    //   134: bipush #7
    //   136: getstatic io/fusionauth/jwt/domain/Algorithm.PS512 : Lio/fusionauth/jwt/domain/Algorithm;
    //   139: aastore
    //   140: dup
    //   141: bipush #8
    //   143: getstatic io/fusionauth/jwt/domain/Algorithm.RS256 : Lio/fusionauth/jwt/domain/Algorithm;
    //   146: aastore
    //   147: dup
    //   148: bipush #9
    //   150: getstatic io/fusionauth/jwt/domain/Algorithm.RS384 : Lio/fusionauth/jwt/domain/Algorithm;
    //   153: aastore
    //   154: dup
    //   155: bipush #10
    //   157: getstatic io/fusionauth/jwt/domain/Algorithm.RS512 : Lio/fusionauth/jwt/domain/Algorithm;
    //   160: aastore
    //   161: invokestatic of : ([Ljava/lang/Object;)Ljava/util/List;
    //   164: astore #6
    //   166: ldc 'DPoP JWT header requires an alg parameter of a supported algorithm: %s'
    //   168: iconst_1
    //   169: anewarray java/lang/Object
    //   172: dup
    //   173: iconst_0
    //   174: ldc ', '
    //   176: aload #6
    //   178: invokeinterface stream : ()Ljava/util/stream/Stream;
    //   183: <illegal opcode> apply : ()Ljava/util/function/Function;
    //   188: invokeinterface map : (Ljava/util/function/Function;)Ljava/util/stream/Stream;
    //   193: invokeinterface toList : ()Ljava/util/List;
    //   198: invokestatic join : (Ljava/lang/CharSequence;Ljava/lang/Iterable;)Ljava/lang/String;
    //   201: aastore
    //   202: invokevirtual formatted : ([Ljava/lang/Object;)Ljava/lang/String;
    //   205: astore #7
    //   207: aload #5
    //   209: getfield algorithm : Lio/fusionauth/jwt/domain/Algorithm;
    //   212: ifnull -> 230
    //   215: aload #6
    //   217: aload #5
    //   219: getfield algorithm : Lio/fusionauth/jwt/domain/Algorithm;
    //   222: invokeinterface contains : (Ljava/lang/Object;)Z
    //   227: ifne -> 236
    //   230: aload #7
    //   232: invokestatic error : (Ljava/lang/String;)Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;
    //   235: areturn
    //   236: aload #5
    //   238: invokestatic buildJSONWebKey : (Lio/fusionauth/jwt/domain/Header;)Lio/fusionauth/jwks/domain/JSONWebKey;
    //   241: astore #8
    //   243: aload #8
    //   245: ifnonnull -> 255
    //   248: ldc_w 'DPoP JWT invalid jwk header parameter'
    //   251: invokestatic error : (Ljava/lang/String;)Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;
    //   254: areturn
    //   255: aload #8
    //   257: invokestatic parse : (Lio/fusionauth/jwks/domain/JSONWebKey;)Ljava/security/PublicKey;
    //   260: astore #9
    //   262: new io/fusionauth/jwks/JSONWebKeyParser
    //   265: dup
    //   266: invokespecial <init> : ()V
    //   269: aload #8
    //   271: invokevirtual containsPrivateKeyParams : (Lio/fusionauth/jwks/domain/JSONWebKey;)Z
    //   274: ifeq -> 284
    //   277: ldc_w 'DPoP JWT invalid jwk header parameter'
    //   280: invokestatic error : (Ljava/lang/String;)Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;
    //   283: areturn
    //   284: goto -> 296
    //   287: astore #10
    //   289: ldc_w 'DPoP JWT invalid jwk header parameter'
    //   292: invokestatic error : (Ljava/lang/String;)Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;
    //   295: areturn
    //   296: getstatic io/fusionauth/api/service/oauth2/DefaultDPoPService$1.$SwitchMap$io$fusionauth$jwt$domain$Algorithm : [I
    //   299: aload #5
    //   301: getfield algorithm : Lio/fusionauth/jwt/domain/Algorithm;
    //   304: invokevirtual ordinal : ()I
    //   307: iaload
    //   308: tableswitch default -> 400, 1 -> 368, 2 -> 368, 3 -> 376, 4 -> 376, 5 -> 376, 6 -> 384, 7 -> 384, 8 -> 384, 9 -> 392, 10 -> 392, 11 -> 392
    //   368: aload #9
    //   370: invokestatic newVerifier : (Ljava/security/PublicKey;)Lio/fusionauth/jwt/ed/EdDSAVerifier;
    //   373: goto -> 410
    //   376: aload #9
    //   378: invokestatic newVerifier : (Ljava/security/PublicKey;)Lio/fusionauth/jwt/ec/ECVerifier;
    //   381: goto -> 410
    //   384: aload #9
    //   386: invokestatic newVerifier : (Ljava/security/PublicKey;)Lio/fusionauth/jwt/rsa/RSAVerifier;
    //   389: goto -> 410
    //   392: aload #9
    //   394: invokestatic newVerifier : (Ljava/security/PublicKey;)Lio/fusionauth/jwt/rsa/RSAPSSVerifier;
    //   397: goto -> 410
    //   400: new io/fusionauth/jwt/InvalidJWTException
    //   403: dup
    //   404: aload #7
    //   406: invokespecial <init> : (Ljava/lang/String;)V
    //   409: athrow
    //   410: astore #10
    //   412: aload #8
    //   414: invokestatic generateJWS_kid_S256 : (Lio/fusionauth/jwks/domain/JSONWebKey;)Ljava/lang/String;
    //   417: astore #11
    //   419: invokestatic getDecoder : ()Lio/fusionauth/jwt/JWTDecoder;
    //   422: aload #4
    //   424: iconst_1
    //   425: anewarray io/fusionauth/jwt/Verifier
    //   428: dup
    //   429: iconst_0
    //   430: aload #10
    //   432: aastore
    //   433: invokevirtual decode : (Ljava/lang/String;[Lio/fusionauth/jwt/Verifier;)Lio/fusionauth/jwt/domain/JWT;
    //   436: astore #12
    //   438: getstatic io/fusionauth/api/service/oauth2/DefaultDPoPService.logger : Lorg/slf4j/Logger;
    //   441: ldc_w 'Decoded DPoP token: {}'
    //   444: aload #12
    //   446: invokevirtual toString : ()Ljava/lang/String;
    //   449: invokeinterface debug : (Ljava/lang/String;Ljava/lang/Object;)V
    //   454: aload #12
    //   456: ldc_w 'jti'
    //   459: invokevirtual getString : (Ljava/lang/String;)Ljava/lang/String;
    //   462: astore #13
    //   464: aload #13
    //   466: ifnull -> 477
    //   469: aload #13
    //   471: invokevirtual isBlank : ()Z
    //   474: ifeq -> 484
    //   477: ldc_w 'DPoP JWT invalid jti claim'
    //   480: invokestatic error : (Ljava/lang/String;)Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;
    //   483: areturn
    //   484: aload #12
    //   486: ldc_w 'htm'
    //   489: invokevirtual getString : (Ljava/lang/String;)Ljava/lang/String;
    //   492: astore #14
    //   494: aload #14
    //   496: ifnull -> 514
    //   499: aload #14
    //   501: aload_1
    //   502: invokevirtual getMethod : ()Lio/fusionauth/http/HTTPMethod;
    //   505: invokevirtual name : ()Ljava/lang/String;
    //   508: invokevirtual equals : (Ljava/lang/Object;)Z
    //   511: ifne -> 521
    //   514: ldc_w 'DPoP JWT invalid htm claim'
    //   517: invokestatic error : (Ljava/lang/String;)Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;
    //   520: areturn
    //   521: aload #12
    //   523: ldc_w 'htu'
    //   526: invokevirtual getString : (Ljava/lang/String;)Ljava/lang/String;
    //   529: astore #15
    //   531: aload #15
    //   533: ifnull -> 557
    //   536: aload #15
    //   538: aload_1
    //   539: invokevirtual getBaseURL : ()Ljava/lang/String;
    //   542: aload_1
    //   543: invokevirtual getPath : ()Ljava/lang/String;
    //   546: <illegal opcode> makeConcatWithConstants : (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    //   551: invokestatic isSameBaseURLAndPath : (Ljava/lang/String;Ljava/lang/String;)Z
    //   554: ifne -> 564
    //   557: ldc_w 'DPoP JWT invalid htu claim'
    //   560: invokestatic error : (Ljava/lang/String;)Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;
    //   563: areturn
    //   564: bipush #10
    //   566: istore #16
    //   568: bipush #15
    //   570: istore #17
    //   572: aload #12
    //   574: getfield issuedAt : Ljava/time/ZonedDateTime;
    //   577: ifnull -> 629
    //   580: aload #12
    //   582: getfield issuedAt : Ljava/time/ZonedDateTime;
    //   585: getstatic java/time/ZoneOffset.UTC : Ljava/time/ZoneOffset;
    //   588: invokestatic now : (Ljava/time/ZoneId;)Ljava/time/ZonedDateTime;
    //   591: iload #16
    //   593: iload #17
    //   595: iadd
    //   596: i2l
    //   597: invokevirtual minusSeconds : (J)Ljava/time/ZonedDateTime;
    //   600: invokevirtual isBefore : (Ljava/time/chrono/ChronoZonedDateTime;)Z
    //   603: ifne -> 629
    //   606: aload #12
    //   608: getfield issuedAt : Ljava/time/ZonedDateTime;
    //   611: getstatic java/time/ZoneOffset.UTC : Ljava/time/ZoneOffset;
    //   614: invokestatic now : (Ljava/time/ZoneId;)Ljava/time/ZonedDateTime;
    //   617: iload #17
    //   619: i2l
    //   620: invokevirtual plusSeconds : (J)Ljava/time/ZonedDateTime;
    //   623: invokevirtual isAfter : (Ljava/time/chrono/ChronoZonedDateTime;)Z
    //   626: ifeq -> 636
    //   629: ldc_w 'DPoP JWT invalid iat claim'
    //   632: invokestatic error : (Ljava/lang/String;)Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;
    //   635: areturn
    //   636: aload_2
    //   637: ifnull -> 771
    //   640: aload #12
    //   642: ldc_w 'ath'
    //   645: invokevirtual getString : (Ljava/lang/String;)Ljava/lang/String;
    //   648: astore #18
    //   650: aload #18
    //   652: ifnull -> 667
    //   655: aload #18
    //   657: aload_2
    //   658: invokestatic sha256urlEncoding : (Ljava/lang/String;)Ljava/lang/String;
    //   661: invokevirtual equals : (Ljava/lang/Object;)Z
    //   664: ifne -> 674
    //   667: ldc_w 'DPoP JWT invalid ath claim'
    //   670: invokestatic error : (Ljava/lang/String;)Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;
    //   673: areturn
    //   674: aload_2
    //   675: invokestatic unsafeDecode : (Ljava/lang/String;)Lio/fusionauth/jwt/domain/JWT;
    //   678: astore #19
    //   680: aload #19
    //   682: invokevirtual getAllClaims : ()Ljava/util/Map;
    //   685: ldc 'cnf'
    //   687: invokeinterface get : (Ljava/lang/Object;)Ljava/lang/Object;
    //   692: astore #20
    //   694: aload #20
    //   696: instanceof java/util/HashMap
    //   699: ifeq -> 712
    //   702: aload #20
    //   704: checkcast java/util/HashMap
    //   707: astore #21
    //   709: goto -> 719
    //   712: ldc_w 'No DPoP confirmation in token'
    //   715: invokestatic error : (Ljava/lang/String;)Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;
    //   718: areturn
    //   719: aload #21
    //   721: ldc_w 'jkt'
    //   724: invokevirtual get : (Ljava/lang/Object;)Ljava/lang/Object;
    //   727: astore #22
    //   729: aload #22
    //   731: instanceof java/lang/String
    //   734: ifeq -> 747
    //   737: aload #22
    //   739: checkcast java/lang/String
    //   742: astore #23
    //   744: goto -> 754
    //   747: ldc_w 'No DPoP key thumbprint in token'
    //   750: invokestatic error : (Ljava/lang/String;)Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;
    //   753: areturn
    //   754: aload #23
    //   756: aload #11
    //   758: invokevirtual equals : (Ljava/lang/Object;)Z
    //   761: ifne -> 771
    //   764: ldc_w 'DPoP confirmation doesn't match DPoP proof'
    //   767: invokestatic error : (Ljava/lang/String;)Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;
    //   770: areturn
    //   771: aload #11
    //   773: invokestatic thumbprint : (Ljava/lang/String;)Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;
    //   776: areturn
    //   777: astore #5
    //   779: ldc_w 'DPoP JWT not signed'
    //   782: invokestatic error : (Ljava/lang/String;)Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;
    //   785: areturn
    //   786: astore #5
    //   788: ldc_w 'DPoP JWT invalid signature'
    //   791: invokestatic error : (Ljava/lang/String;)Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;
    //   794: areturn
    //   795: astore #5
    //   797: aload #5
    //   799: invokevirtual getMessage : ()Ljava/lang/String;
    //   802: invokestatic error : (Ljava/lang/String;)Lio/fusionauth/api/service/oauth2/DPoPService$DPoPResult;
    //   805: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #140	-> 0
    //   #141	-> 7
    //   #142	-> 20
    //   #143	-> 24
    //   #144	-> 34
    //   #146	-> 40
    //   #150	-> 51
    //   #153	-> 58
    //   #154	-> 79
    //   #159	-> 85
    //   #166	-> 166
    //   #167	-> 178
    //   #169	-> 207
    //   #170	-> 230
    //   #174	-> 236
    //   #175	-> 243
    //   #176	-> 248
    //   #181	-> 255
    //   #183	-> 262
    //   #184	-> 277
    //   #188	-> 284
    //   #186	-> 287
    //   #187	-> 289
    //   #190	-> 296
    //   #191	-> 368
    //   #192	-> 376
    //   #193	-> 384
    //   #194	-> 392
    //   #195	-> 400
    //   #190	-> 410
    //   #197	-> 412
    //   #200	-> 419
    //   #201	-> 438
    //   #206	-> 454
    //   #207	-> 464
    //   #208	-> 477
    //   #212	-> 484
    //   #213	-> 494
    //   #214	-> 514
    //   #218	-> 521
    //   #219	-> 531
    //   #220	-> 557
    //   #226	-> 564
    //   #227	-> 568
    //   #228	-> 572
    //   #229	-> 588
    //   #230	-> 614
    //   #231	-> 629
    //   #235	-> 636
    //   #236	-> 640
    //   #237	-> 650
    //   #238	-> 667
    //   #241	-> 674
    //   #242	-> 680
    //   #243	-> 694
    //   #244	-> 712
    //   #246	-> 719
    //   #247	-> 729
    //   #248	-> 747
    //   #250	-> 754
    //   #251	-> 764
    //   #257	-> 771
    //   #259	-> 777
    //   #261	-> 779
    //   #262	-> 786
    //   #264	-> 788
    //   #265	-> 795
    //   #266	-> 797
    // Exception table:
    //   from	to	target	type
    //   51	84	777	io/fusionauth/jwt/NoneNotAllowedException
    //   51	84	786	io/fusionauth/jwt/InvalidJWTSignatureException
    //   51	84	795	io/fusionauth/jwt/JWTException
    //   85	235	777	io/fusionauth/jwt/NoneNotAllowedException
    //   85	235	786	io/fusionauth/jwt/InvalidJWTSignatureException
    //   85	235	795	io/fusionauth/jwt/JWTException
    //   236	254	777	io/fusionauth/jwt/NoneNotAllowedException
    //   236	254	786	io/fusionauth/jwt/InvalidJWTSignatureException
    //   236	254	795	io/fusionauth/jwt/JWTException
    //   255	283	287	java/lang/Exception
    //   255	283	777	io/fusionauth/jwt/NoneNotAllowedException
    //   255	283	786	io/fusionauth/jwt/InvalidJWTSignatureException
    //   255	283	795	io/fusionauth/jwt/JWTException
    //   284	295	777	io/fusionauth/jwt/NoneNotAllowedException
    //   284	295	786	io/fusionauth/jwt/InvalidJWTSignatureException
    //   284	295	795	io/fusionauth/jwt/JWTException
    //   296	483	777	io/fusionauth/jwt/NoneNotAllowedException
    //   296	483	786	io/fusionauth/jwt/InvalidJWTSignatureException
    //   296	483	795	io/fusionauth/jwt/JWTException
    //   484	520	777	io/fusionauth/jwt/NoneNotAllowedException
    //   484	520	786	io/fusionauth/jwt/InvalidJWTSignatureException
    //   484	520	795	io/fusionauth/jwt/JWTException
    //   521	563	777	io/fusionauth/jwt/NoneNotAllowedException
    //   521	563	786	io/fusionauth/jwt/InvalidJWTSignatureException
    //   521	563	795	io/fusionauth/jwt/JWTException
    //   564	635	777	io/fusionauth/jwt/NoneNotAllowedException
    //   564	635	786	io/fusionauth/jwt/InvalidJWTSignatureException
    //   564	635	795	io/fusionauth/jwt/JWTException
    //   636	673	777	io/fusionauth/jwt/NoneNotAllowedException
    //   636	673	786	io/fusionauth/jwt/InvalidJWTSignatureException
    //   636	673	795	io/fusionauth/jwt/JWTException
    //   674	718	777	io/fusionauth/jwt/NoneNotAllowedException
    //   674	718	786	io/fusionauth/jwt/InvalidJWTSignatureException
    //   674	718	795	io/fusionauth/jwt/JWTException
    //   719	753	777	io/fusionauth/jwt/NoneNotAllowedException
    //   719	753	786	io/fusionauth/jwt/InvalidJWTSignatureException
    //   719	753	795	io/fusionauth/jwt/JWTException
    //   754	770	777	io/fusionauth/jwt/NoneNotAllowedException
    //   754	770	786	io/fusionauth/jwt/InvalidJWTSignatureException
    //   754	770	795	io/fusionauth/jwt/JWTException
    //   771	776	777	io/fusionauth/jwt/NoneNotAllowedException
    //   771	776	786	io/fusionauth/jwt/InvalidJWTSignatureException
    //   771	776	795	io/fusionauth/jwt/JWTException
  }
  
  public DPoPService.DPoPResult parseDPoPRefreshToken(HTTPRequest paramHTTPRequest, @Nonnull RefreshToken paramRefreshToken) {
    DPoPService.DPoPResult dPoPResult = parseDPoP(paramHTTPRequest, null);
    if (dPoPResult.isError())
      return dPoPResult; 
    OAuthError oAuthError = checkRefreshToken(paramRefreshToken, dPoPResult.dPoPThumbprint());
    if (oAuthError != null)
      return new DPoPService.DPoPResult(null, oAuthError); 
    return dPoPResult;
  }
  
  public DPoPService.DPoPResult parseDPoPRequired(HTTPRequest paramHTTPRequest, @Nullable String paramString) {
    DPoPService.DPoPResult dPoPResult = parseDPoP(paramHTTPRequest, paramString);
    if (dPoPResult.isError())
      return dPoPResult; 
    if (dPoPResult.dPoPThumbprint() == null)
      return DPoPService.DPoPResult.error("Missing DPoP proof header"); 
    return dPoPResult;
  }
}
