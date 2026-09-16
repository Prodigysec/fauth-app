package io.fusionauth.app.action.legacy.oauth2;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.service.jwt.FusionAuthJWTDecoder;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.util.ClaimTools;
import io.fusionauth.app.service.legacy.LegacyTokenSigner;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.oauth2.AccessToken;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import io.fusionauth.jwt.OpenIDConnect;
import io.fusionauth.jwt.domain.JWT;
import java.net.URI;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action
public class TokenAction extends BaseLegacyAdapterAction {
  private final HTTPResponse httpResponse;
  
  private final LegacyTokenSigner legacyTokenSigner;
  
  public String access_token;
  
  public String client_id;
  
  public String client_secret;
  
  public String code;
  
  public String code_verifier;
  
  public String device_code;
  
  public String grant_type;
  
  public URI redirect_uri;
  
  public String refresh_token;
  
  @JSONResponse
  public Object response;
  
  public String scope;
  
  @Inject
  public TokenAction(FusionAuthConfiguration paramFusionAuthConfiguration, FusionAuthClientProvider paramFusionAuthClientProvider, HTTPRequest paramHTTPRequest, ReactorStatusService paramReactorStatusService, HTTPResponse paramHTTPResponse, LegacyTokenSigner paramLegacyTokenSigner) {
    super(paramFusionAuthConfiguration, paramFusionAuthClientProvider, paramHTTPRequest, paramReactorStatusService);
    this.httpResponse = paramHTTPResponse;
    this.legacyTokenSigner = paramLegacyTokenSigner;
  }
  
  public String post() {
    // Byte code:
    //   0: aload_0
    //   1: invokevirtual isDisabled : ()Z
    //   4: ifeq -> 10
    //   7: ldc 'not-found'
    //   9: areturn
    //   10: aload_0
    //   11: invokevirtual isNotLicensed : ()Z
    //   14: ifeq -> 28
    //   17: aload_0
    //   18: aload_0
    //   19: invokevirtual buildNotLicensedError : ()Lio/fusionauth/domain/oauth2/OAuthError;
    //   22: putfield response : Ljava/lang/Object;
    //   25: ldc 'input'
    //   27: areturn
    //   28: aload_0
    //   29: getfield httpRequest : Lio/fusionauth/http/server/HTTPRequest;
    //   32: ldc 'Authorization'
    //   34: invokevirtual getHeader : (Ljava/lang/String;)Ljava/lang/String;
    //   37: astore_1
    //   38: aload_1
    //   39: ifnull -> 179
    //   42: aload_1
    //   43: aload_0
    //   44: getfield client_id : Ljava/lang/String;
    //   47: invokestatic parseCredentials : (Ljava/lang/String;Ljava/lang/String;)Lio/fusionauth/api/service/oauth2/OAuthService$CredentialResult;
    //   50: astore_2
    //   51: aload_2
    //   52: dup
    //   53: invokestatic requireNonNull : (Ljava/lang/Object;)Ljava/lang/Object;
    //   56: pop
    //   57: astore_3
    //   58: iconst_0
    //   59: istore #4
    //   61: aload_3
    //   62: iload #4
    //   64: <illegal opcode> typeSwitch : (Lio/fusionauth/api/service/oauth2/OAuthService$CredentialResult;I)I
    //   69: lookupswitch default -> 96, 0 -> 106, 1 -> 133
    //   96: new java/lang/MatchException
    //   99: dup
    //   100: aconst_null
    //   101: aconst_null
    //   102: invokespecial <init> : (Ljava/lang/String;Ljava/lang/Throwable;)V
    //   105: athrow
    //   106: aload_3
    //   107: checkcast io/fusionauth/api/service/oauth2/OAuthService$CredentialResult$Success
    //   110: astore #5
    //   112: aload_0
    //   113: aload #5
    //   115: invokevirtual client_id : ()Ljava/lang/String;
    //   118: putfield client_id : Ljava/lang/String;
    //   121: aload_0
    //   122: aload #5
    //   124: invokevirtual client_secret : ()Ljava/lang/String;
    //   127: putfield client_secret : Ljava/lang/String;
    //   130: goto -> 179
    //   133: aload_3
    //   134: checkcast io/fusionauth/api/service/oauth2/OAuthService$CredentialResult$Failure
    //   137: astore #6
    //   139: aload_0
    //   140: aload #6
    //   142: invokevirtual error : ()Lio/fusionauth/domain/oauth2/OAuthError;
    //   145: putfield response : Ljava/lang/Object;
    //   148: aload #6
    //   150: invokevirtual error : ()Lio/fusionauth/domain/oauth2/OAuthError;
    //   153: getfield error : Lio/fusionauth/domain/oauth2/OAuthError$OAuthErrorType;
    //   156: getstatic io/fusionauth/domain/oauth2/OAuthError$OAuthErrorType.invalid_client : Lio/fusionauth/domain/oauth2/OAuthError$OAuthErrorType;
    //   159: if_acmpne -> 176
    //   162: aload_0
    //   163: getfield httpResponse : Lio/fusionauth/http/server/HTTPResponse;
    //   166: ldc 'WWW-Authenticate'
    //   168: ldc 'Basic'
    //   170: invokevirtual setHeader : (Ljava/lang/String;Ljava/lang/String;)V
    //   173: ldc 'invalid-client'
    //   175: areturn
    //   176: ldc 'input'
    //   178: areturn
    //   179: aload_0
    //   180: getfield fusionAuthClientProvider : Lio/fusionauth/api/domain/guice/FusionAuthClientProvider;
    //   183: aload_0
    //   184: getfield tenantId : Ljava/util/UUID;
    //   187: invokevirtual get : (Ljava/util/UUID;)Lio/fusionauth/client/FusionAuthClient;
    //   190: astore_2
    //   191: ldc 'authorization_code'
    //   193: aload_0
    //   194: getfield grant_type : Ljava/lang/String;
    //   197: invokevirtual equals : (Ljava/lang/Object;)Z
    //   200: ifeq -> 290
    //   203: aload_0
    //   204: getfield code_verifier : Ljava/lang/String;
    //   207: ifnull -> 252
    //   210: aload_2
    //   211: aload_0
    //   212: getfield code : Ljava/lang/String;
    //   215: aload_0
    //   216: getfield client_id : Ljava/lang/String;
    //   219: aload_0
    //   220: getfield client_secret : Ljava/lang/String;
    //   223: aload_0
    //   224: getfield redirect_uri : Ljava/net/URI;
    //   227: ifnull -> 240
    //   230: aload_0
    //   231: getfield redirect_uri : Ljava/net/URI;
    //   234: invokevirtual toString : ()Ljava/lang/String;
    //   237: goto -> 241
    //   240: aconst_null
    //   241: aload_0
    //   242: getfield code_verifier : Ljava/lang/String;
    //   245: invokevirtual exchangeOAuthCodeForAccessTokenUsingPKCE : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcom/inversoft/rest/ClientResponse;
    //   248: astore_3
    //   249: goto -> 399
    //   252: aload_2
    //   253: aload_0
    //   254: getfield code : Ljava/lang/String;
    //   257: aload_0
    //   258: getfield client_id : Ljava/lang/String;
    //   261: aload_0
    //   262: getfield client_secret : Ljava/lang/String;
    //   265: aload_0
    //   266: getfield redirect_uri : Ljava/net/URI;
    //   269: ifnull -> 282
    //   272: aload_0
    //   273: getfield redirect_uri : Ljava/net/URI;
    //   276: invokevirtual toString : ()Ljava/lang/String;
    //   279: goto -> 283
    //   282: aconst_null
    //   283: invokevirtual exchangeOAuthCodeForAccessToken : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcom/inversoft/rest/ClientResponse;
    //   286: astore_3
    //   287: goto -> 399
    //   290: ldc 'refresh_token'
    //   292: aload_0
    //   293: getfield grant_type : Ljava/lang/String;
    //   296: invokevirtual equals : (Ljava/lang/Object;)Z
    //   299: ifeq -> 327
    //   302: aload_2
    //   303: aload_0
    //   304: getfield refresh_token : Ljava/lang/String;
    //   307: aload_0
    //   308: getfield client_id : Ljava/lang/String;
    //   311: aload_0
    //   312: getfield client_secret : Ljava/lang/String;
    //   315: aload_0
    //   316: getfield scope : Ljava/lang/String;
    //   319: aconst_null
    //   320: invokevirtual exchangeRefreshTokenForAccessToken : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcom/inversoft/rest/ClientResponse;
    //   323: astore_3
    //   324: goto -> 399
    //   327: ldc 'client_credentials'
    //   329: aload_0
    //   330: getfield grant_type : Ljava/lang/String;
    //   333: invokevirtual equals : (Ljava/lang/Object;)Z
    //   336: ifeq -> 359
    //   339: aload_2
    //   340: aload_0
    //   341: getfield client_id : Ljava/lang/String;
    //   344: aload_0
    //   345: getfield client_secret : Ljava/lang/String;
    //   348: aload_0
    //   349: getfield scope : Ljava/lang/String;
    //   352: invokevirtual clientCredentialsGrant : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcom/inversoft/rest/ClientResponse;
    //   355: astore_3
    //   356: goto -> 399
    //   359: new io/fusionauth/domain/oauth2/OAuthError
    //   362: dup
    //   363: invokespecial <init> : ()V
    //   366: astore #4
    //   368: aload #4
    //   370: getstatic io/fusionauth/domain/oauth2/OAuthError$OAuthErrorType.unsupported_grant_type : Lio/fusionauth/domain/oauth2/OAuthError$OAuthErrorType;
    //   373: putfield error : Lio/fusionauth/domain/oauth2/OAuthError$OAuthErrorType;
    //   376: aload #4
    //   378: aload_0
    //   379: getfield grant_type : Ljava/lang/String;
    //   382: <illegal opcode> makeConcatWithConstants : (Ljava/lang/String;)Ljava/lang/String;
    //   387: putfield description : Ljava/lang/String;
    //   390: aload_0
    //   391: aload #4
    //   393: putfield response : Ljava/lang/Object;
    //   396: ldc 'input'
    //   398: areturn
    //   399: aload_3
    //   400: invokevirtual wasSuccessful : ()Z
    //   403: ifeq -> 431
    //   406: aload_3
    //   407: getfield successResponse : Ljava/lang/Object;
    //   410: checkcast io/fusionauth/domain/oauth2/AccessToken
    //   413: astore #4
    //   415: aload_0
    //   416: aload_2
    //   417: aload #4
    //   419: invokevirtual applySubClaimSwap : (Lio/fusionauth/client/FusionAuthClient;Lio/fusionauth/domain/oauth2/AccessToken;)V
    //   422: aload_0
    //   423: aload #4
    //   425: putfield response : Ljava/lang/Object;
    //   428: ldc 'render'
    //   430: areturn
    //   431: aload_3
    //   432: getfield status : I
    //   435: sipush #400
    //   438: if_icmpne -> 452
    //   441: aload_0
    //   442: aload_3
    //   443: getfield errorResponse : Ljava/lang/Object;
    //   446: putfield response : Ljava/lang/Object;
    //   449: ldc 'input'
    //   451: areturn
    //   452: aload_3
    //   453: getfield status : I
    //   456: sipush #401
    //   459: if_icmpne -> 488
    //   462: aload_0
    //   463: aload_3
    //   464: getfield errorResponse : Ljava/lang/Object;
    //   467: putfield response : Ljava/lang/Object;
    //   470: aload_1
    //   471: ifnull -> 485
    //   474: aload_0
    //   475: getfield httpResponse : Lio/fusionauth/http/server/HTTPResponse;
    //   478: ldc 'WWW-Authenticate'
    //   480: ldc 'Basic'
    //   482: invokevirtual addHeader : (Ljava/lang/String;Ljava/lang/String;)V
    //   485: ldc 'invalid-client'
    //   487: areturn
    //   488: aload_0
    //   489: aload_3
    //   490: getfield errorResponse : Ljava/lang/Object;
    //   493: ifnull -> 503
    //   496: aload_3
    //   497: getfield errorResponse : Ljava/lang/Object;
    //   500: goto -> 510
    //   503: new io/fusionauth/domain/oauth2/OAuthError
    //   506: dup
    //   507: invokespecial <init> : ()V
    //   510: putfield response : Ljava/lang/Object;
    //   513: ldc 'error'
    //   515: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #86	-> 0
    //   #87	-> 7
    //   #90	-> 10
    //   #91	-> 17
    //   #92	-> 25
    //   #97	-> 28
    //   #98	-> 38
    //   #99	-> 42
    //   #101	-> 51
    //   #102	-> 106
    //   #104	-> 112
    //   #105	-> 121
    //   #106	-> 130
    //   #107	-> 133
    //   #108	-> 139
    //   #109	-> 148
    //   #111	-> 162
    //   #112	-> 173
    //   #115	-> 176
    //   #120	-> 179
    //   #123	-> 191
    //   #124	-> 203
    //   #125	-> 210
    //   #126	-> 223
    //   #125	-> 245
    //   #128	-> 252
    //   #129	-> 265
    //   #128	-> 283
    //   #131	-> 290
    //   #132	-> 302
    //   #133	-> 327
    //   #134	-> 339
    //   #137	-> 359
    //   #138	-> 368
    //   #139	-> 376
    //   #140	-> 390
    //   #141	-> 396
    //   #144	-> 399
    //   #145	-> 406
    //   #146	-> 415
    //   #147	-> 422
    //   #148	-> 428
    //   #149	-> 431
    //   #150	-> 441
    //   #151	-> 449
    //   #152	-> 452
    //   #153	-> 462
    //   #154	-> 470
    //   #155	-> 474
    //   #157	-> 485
    //   #159	-> 488
    //   #160	-> 513
  }
  
  private void applySubClaimSwap(FusionAuthClient paramFusionAuthClient, AccessToken paramAccessToken) {
    if (paramAccessToken == null || paramAccessToken.token == null)
      return; 
    JWT jWT = FusionAuthJWTDecoder.unsafeDecode(paramAccessToken.token);
    if (jWT.subject == null)
      return; 
    UUID uUID1 = ClaimTools.resolveUserId(jWT);
    if (uUID1 == null)
      return; 
    UUID uUID2 = ClaimTools.resolveApplicationId(jWT);
    if (uUID2 == null)
      return; 
    ClientResponse<UserResponse, Errors> clientResponse = paramFusionAuthClient.retrieveUser(uUID1);
    if (!clientResponse.wasSuccessful() || clientResponse.successResponse == null || ((UserResponse)clientResponse.successResponse).user == null)
      return; 
    User user = ((UserResponse)clientResponse.successResponse).user;
    if (user.legacyIdentifier == null)
      return; 
    ClientResponse<ApplicationResponse, Void> clientResponse1 = paramFusionAuthClient.retrieveApplication(uUID2);
    if (!clientResponse1.wasSuccessful() || clientResponse1.successResponse == null || ((ApplicationResponse)clientResponse1.successResponse).application == null)
      return; 
    Application application = ((ApplicationResponse)clientResponse1.successResponse).application;
    String str1 = jWT.subject;
    jWT.setSubject(user.legacyIdentifier);
    jWT.addClaim("fa_uid", str1);
    String str2 = (jWT.header != null) ? (String)jWT.header.properties.get("kid") : null;
    String str3 = this.legacyTokenSigner.sign(jWT, str2, uUID2, application.oauthConfiguration.clientSecret, false);
    if (str3 != null)
      paramAccessToken.token = str3; 
    if (paramAccessToken.idToken != null) {
      JWT jWT1 = FusionAuthJWTDecoder.unsafeDecode(paramAccessToken.idToken);
      if (jWT1.subject != null) {
        jWT1.setSubject(user.legacyIdentifier);
        jWT1.addClaim("fa_uid", str1);
        jWT1.addClaim("at_hash", OpenIDConnect.at_hash(paramAccessToken.token, jWT1.header.algorithm));
        String str4 = (jWT1.header != null) ? (String)jWT1.header.properties.get("kid") : null;
        String str5 = this.legacyTokenSigner.sign(jWT1, str4, uUID2, application.oauthConfiguration.clientSecret, true);
        if (str5 != null)
          paramAccessToken.idToken = str5; 
      } 
    } 
  }
}
