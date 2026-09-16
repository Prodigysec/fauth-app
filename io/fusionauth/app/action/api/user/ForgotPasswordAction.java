package io.fusionauth.app.action.api.user;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.user.IdentityHelper;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.api.user.ForgotPasswordRequest;
import io.fusionauth.domain.api.user.ForgotPasswordResponse;
import java.util.Collections;
import java.util.Objects;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.security.annotation.AuthorizeMethod;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.annotation.PostValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api", "authorize-method"})
@Status(code = "missing-delivery-address", status = 422)
public class ForgotPasswordAction extends BaseTenantAPIAction {
  @JSONRequest
  public final ForgotPasswordRequest request = new ForgotPasswordRequest();
  
  private final UserReaderService userReader;
  
  private final UserService userService;
  
  @JSONResponse
  public ForgotPasswordResponse response;
  
  private boolean apiKeyAuthenticated = true;
  
  private UserService.ValidationResult result;
  
  private User user;
  
  private UserIdentity userIdentity;
  
  @Inject
  public ForgotPasswordAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.userReader = paramUserReaderService;
    this.userService = paramUserService;
  }
  
  @AuthorizeMethod
  public boolean authorize() {
    this.apiKeyAuthenticated = false;
    return true;
  }
  
  public String post() {
    // Byte code:
    //   0: aload_0
    //   1: getfield apiKeyAuthenticated : Z
    //   4: ifne -> 26
    //   7: aload_0
    //   8: getfield request : Lio/fusionauth/domain/api/user/ForgotPasswordRequest;
    //   11: iconst_1
    //   12: invokestatic valueOf : (Z)Ljava/lang/Boolean;
    //   15: putfield sendForgotPasswordMessage : Ljava/lang/Boolean;
    //   18: aload_0
    //   19: getfield request : Lio/fusionauth/domain/api/user/ForgotPasswordRequest;
    //   22: aconst_null
    //   23: putfield changePasswordId : Ljava/lang/String;
    //   26: aload_0
    //   27: getfield userIdentity : Lio/fusionauth/domain/UserIdentity;
    //   30: ifnull -> 70
    //   33: aload_0
    //   34: getfield userIdentity : Lio/fusionauth/domain/UserIdentity;
    //   37: getfield type : Lio/fusionauth/domain/IdentityType;
    //   40: getstatic io/fusionauth/domain/IdentityType.phoneNumber : Lio/fusionauth/domain/IdentityType;
    //   43: invokevirtual is : (Lio/fusionauth/domain/IdentityType;)Z
    //   46: ifeq -> 70
    //   49: aload_0
    //   50: invokevirtual getTenant : ()Lio/fusionauth/domain/Tenant;
    //   53: getfield phoneConfiguration : Lio/fusionauth/domain/TenantPhoneConfiguration;
    //   56: getfield forgotPasswordTemplateId : Ljava/util/UUID;
    //   59: ifnull -> 66
    //   62: iconst_1
    //   63: goto -> 88
    //   66: iconst_0
    //   67: goto -> 88
    //   70: aload_0
    //   71: invokevirtual getTenant : ()Lio/fusionauth/domain/Tenant;
    //   74: getfield emailConfiguration : Lio/fusionauth/domain/EmailConfiguration;
    //   77: getfield forgotPasswordEmailTemplateId : Ljava/util/UUID;
    //   80: ifnull -> 87
    //   83: iconst_1
    //   84: goto -> 88
    //   87: iconst_0
    //   88: istore_1
    //   89: aload_0
    //   90: getfield request : Lio/fusionauth/domain/api/user/ForgotPasswordRequest;
    //   93: getfield sendForgotPasswordMessage : Ljava/lang/Boolean;
    //   96: invokevirtual booleanValue : ()Z
    //   99: ifeq -> 109
    //   102: iload_1
    //   103: ifne -> 109
    //   106: ldc 'disabled'
    //   108: areturn
    //   109: aload_0
    //   110: getfield userService : Lio/fusionauth/api/service/user/UserService;
    //   113: aload_0
    //   114: invokevirtual getTenant : ()Lio/fusionauth/domain/Tenant;
    //   117: aload_0
    //   118: getfield result : Lio/fusionauth/api/service/user/UserService$ValidationResult;
    //   121: getfield application : Lio/fusionauth/domain/Application;
    //   124: aload_0
    //   125: getfield user : Lio/fusionauth/domain/User;
    //   128: aload_0
    //   129: getfield userIdentity : Lio/fusionauth/domain/UserIdentity;
    //   132: aload_0
    //   133: getfield request : Lio/fusionauth/domain/api/user/ForgotPasswordRequest;
    //   136: getfield loginId : Ljava/lang/String;
    //   139: aload_0
    //   140: getfield request : Lio/fusionauth/domain/api/user/ForgotPasswordRequest;
    //   143: getfield changePasswordId : Ljava/lang/String;
    //   146: aload_0
    //   147: getfield request : Lio/fusionauth/domain/api/user/ForgotPasswordRequest;
    //   150: getfield sendForgotPasswordMessage : Ljava/lang/Boolean;
    //   153: invokevirtual booleanValue : ()Z
    //   156: aload_0
    //   157: getfield request : Lio/fusionauth/domain/api/user/ForgotPasswordRequest;
    //   160: getfield state : Ljava/util/Map;
    //   163: aload_0
    //   164: getfield request : Lio/fusionauth/domain/api/user/ForgotPasswordRequest;
    //   167: getfield eventInfo : Lio/fusionauth/domain/EventInfo;
    //   170: invokeinterface forgotPassword : (Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Lio/fusionauth/domain/User;Lio/fusionauth/domain/UserIdentity;Ljava/lang/String;Ljava/lang/String;ZLjava/util/Map;Lio/fusionauth/domain/EventInfo;)Lio/fusionauth/api/service/user/UserService$ForgotPasswordResult;
    //   175: astore_2
    //   176: aload_0
    //   177: new io/fusionauth/domain/api/user/ForgotPasswordResponse
    //   180: dup
    //   181: aload_2
    //   182: getfield changePasswordId : Ljava/lang/String;
    //   185: invokespecial <init> : (Ljava/lang/String;)V
    //   188: putfield response : Lio/fusionauth/domain/api/user/ForgotPasswordResponse;
    //   191: aload_0
    //   192: getfield user : Lio/fusionauth/domain/User;
    //   195: ifnull -> 205
    //   198: aload_0
    //   199: getfield userIdentity : Lio/fusionauth/domain/UserIdentity;
    //   202: ifnonnull -> 220
    //   205: aload_0
    //   206: getfield apiKeyAuthenticated : Z
    //   209: ifeq -> 217
    //   212: ldc 'missing'
    //   214: goto -> 219
    //   217: ldc 'success'
    //   219: areturn
    //   220: aload_2
    //   221: getfield askedToSendButNoDeliveryAvailable : Z
    //   224: ifeq -> 237
    //   227: aload_0
    //   228: getfield apiKeyAuthenticated : Z
    //   231: ifeq -> 237
    //   234: ldc 'missing-delivery-address'
    //   236: areturn
    //   237: aload_0
    //   238: getfield apiKeyAuthenticated : Z
    //   241: ifeq -> 249
    //   244: ldc 'render'
    //   246: goto -> 251
    //   249: ldc 'success'
    //   251: areturn
    //   252: astore_2
    //   253: aload_0
    //   254: getfield apiKeyAuthenticated : Z
    //   257: ifeq -> 262
    //   260: aload_2
    //   261: athrow
    //   262: aload_2
    //   263: dup
    //   264: invokestatic requireNonNull : (Ljava/lang/Object;)Ljava/lang/Object;
    //   267: pop
    //   268: astore_3
    //   269: iconst_0
    //   270: istore #4
    //   272: aload_3
    //   273: iload #4
    //   275: <illegal opcode> typeSwitch : (Lorg/primeframework/mvc/ErrorException;I)I
    //   280: lookupswitch default -> 336, 0 -> 308, 1 -> 322
    //   308: aload_3
    //   309: checkcast io/fusionauth/api/service/messenger/MessageTemplateException
    //   312: astore #5
    //   314: aload #5
    //   316: invokestatic logEvent : (Lio/fusionauth/api/service/messenger/MessageTemplateException;)V
    //   319: goto -> 356
    //   322: aload_3
    //   323: checkcast io/fusionauth/api/service/messenger/MessengerException
    //   326: astore #6
    //   328: aload #6
    //   330: invokestatic logEvent : (Lio/fusionauth/api/service/messenger/MessengerException;)V
    //   333: goto -> 356
    //   336: new java/lang/IllegalStateException
    //   339: dup
    //   340: aload_2
    //   341: invokevirtual getClass : ()Ljava/lang/Class;
    //   344: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   347: <illegal opcode> makeConcatWithConstants : (Ljava/lang/String;)Ljava/lang/String;
    //   352: invokespecial <init> : (Ljava/lang/String;)V
    //   355: athrow
    //   356: ldc 'success'
    //   358: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #85	-> 0
    //   #86	-> 7
    //   #87	-> 18
    //   #90	-> 26
    //   #91	-> 49
    //   #92	-> 70
    //   #93	-> 89
    //   #94	-> 106
    //   #99	-> 109
    //   #100	-> 153
    //   #99	-> 170
    //   #101	-> 176
    //   #105	-> 191
    //   #106	-> 205
    //   #109	-> 220
    //   #110	-> 234
    //   #115	-> 237
    //   #116	-> 252
    //   #117	-> 253
    //   #119	-> 260
    //   #121	-> 262
    //   #122	-> 308
    //   #123	-> 322
    //   #124	-> 336
    //   #126	-> 356
    // Exception table:
    //   from	to	target	type
    //   109	219	252	io/fusionauth/api/service/messenger/MessengerException
    //   109	219	252	io/fusionauth/api/service/messenger/MessageTemplateException
    //   220	236	252	io/fusionauth/api/service/messenger/MessengerException
    //   220	236	252	io/fusionauth/api/service/messenger/MessageTemplateException
    //   237	251	252	io/fusionauth/api/service/messenger/MessengerException
    //   237	251	252	io/fusionauth/api/service/messenger/MessageTemplateException
  }
  
  @PostValidationMethod
  public void postValidate() {
    if (this.result != null && this.result.errors.empty()) {
      this.user = this.userReader.retrieveByLoginId((getTenant()).id, this.request.loginId, this.result.identityTypes, Collections.emptySet());
      if (this.user != null) {
        this.user.secure().sort();
        this.userIdentity = IdentityHelper.resolveIdentity(this.user, this.request.loginId, this.result.identityTypes);
      } 
    } 
  }
  
  @ValidationMethod
  public void validate() {
    if (this.request.loginId == null) {
      this.frontEndSupport.addFieldError("loginId", "[blank]loginId", new Object[0]);
      return;
    } 
    this.request.sendForgotPasswordMessage = (Boolean)Objects.requireNonNullElse(this.request.sendForgotPasswordMessage, Boolean.valueOf(this.request.sendForgotPasswordEmail));
    this.result = this.userService.validateForgotPassword(getOptionalTenant(), this.request.applicationId, this.request.loginId, this.request.loginIdTypes);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
