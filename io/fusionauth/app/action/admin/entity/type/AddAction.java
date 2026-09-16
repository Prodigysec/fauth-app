package io.fusionauth.app.action.admin.entity.type;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.SCIMServerPermissions;
import io.fusionauth.app.domain.AIAgentPermissions;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.EntityType;
import io.fusionauth.domain.EntityTypePermission;
import io.fusionauth.domain.api.EntityTypeRequest;
import io.fusionauth.domain.api.EntityTypeResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

@Action(value = "{template}", requiresAuthentication = true, constraints = {"admin", "entity_manager"})
@List({@Redirect(code = "not-licensed", uri = "/admin/entity/type/"), @Redirect(code = "success", uri = "/admin/entity/type/")})
public class AddAction extends BaseFormAction {
  public String template;
  
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    // Byte code:
    //   0: aload_0
    //   1: aload_0
    //   2: getfield template : Ljava/lang/String;
    //   5: astore_1
    //   6: iconst_0
    //   7: istore_2
    //   8: aload_1
    //   9: iload_2
    //   10: <illegal opcode> typeSwitch : (Ljava/lang/String;I)I
    //   15: tableswitch default -> 129, -1 -> 129, 0 -> 44, 1 -> 76, 2 -> 97
    //   44: new io/fusionauth/domain/EntityType
    //   47: dup
    //   48: invokespecial <init> : ()V
    //   51: <illegal opcode> accept : ()Ljava/util/function/Consumer;
    //   56: invokevirtual with : (Ljava/util/function/Consumer;)Ljava/lang/Object;
    //   59: checkcast io/fusionauth/domain/EntityType
    //   62: <illegal opcode> accept : ()Ljava/util/function/Consumer;
    //   67: invokevirtual with : (Ljava/util/function/Consumer;)Ljava/lang/Object;
    //   70: checkcast io/fusionauth/domain/EntityType
    //   73: goto -> 136
    //   76: new io/fusionauth/domain/EntityType
    //   79: dup
    //   80: invokespecial <init> : ()V
    //   83: <illegal opcode> accept : ()Ljava/util/function/Consumer;
    //   88: invokevirtual with : (Ljava/util/function/Consumer;)Ljava/lang/Object;
    //   91: checkcast io/fusionauth/domain/EntityType
    //   94: goto -> 136
    //   97: new io/fusionauth/domain/EntityType
    //   100: dup
    //   101: invokespecial <init> : ()V
    //   104: <illegal opcode> accept : ()Ljava/util/function/Consumer;
    //   109: invokevirtual with : (Ljava/util/function/Consumer;)Ljava/lang/Object;
    //   112: checkcast io/fusionauth/domain/EntityType
    //   115: <illegal opcode> accept : ()Ljava/util/function/Consumer;
    //   120: invokevirtual with : (Ljava/util/function/Consumer;)Ljava/lang/Object;
    //   123: checkcast io/fusionauth/domain/EntityType
    //   126: goto -> 136
    //   129: new io/fusionauth/domain/EntityType
    //   132: dup
    //   133: invokespecial <init> : ()V
    //   136: putfield entityType : Lio/fusionauth/domain/EntityType;
    //   139: ldc 'input'
    //   141: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #35	-> 0
    //   #36	-> 44
    //   #37	-> 67
    //   #38	-> 76
    //   #39	-> 97
    //   #40	-> 120
    //   #41	-> 129
    //   #42	-> 136
    //   #44	-> 139
  }
  
  public String post() {
    EntityType entityType = ((EntityTypeResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.createEntityType(this.entityTypeId, new EntityTypeRequest(this.entityType)))).entityType;
    writeAuditLog("Created Entity Type with Id [" + String.valueOf(entityType.id) + "] and name [" + entityType.name + "]");
    return "success";
  }
  
  @PostParameterMethod
  public void postParameter() {
    this.entityType.permissions.removeIf(paramEntityTypePermission -> (paramEntityTypePermission == null || paramEntityTypePermission.name == null || paramEntityTypePermission.name.trim().isEmpty()));
  }
}
