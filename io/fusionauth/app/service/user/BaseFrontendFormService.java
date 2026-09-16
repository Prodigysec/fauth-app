package io.fusionauth.app.service.user;

import io.fusionauth.domain.form.FormControl;
import io.fusionauth.domain.form.FormDataType;
import io.fusionauth.domain.form.FormField;
import java.lang.reflect.Array;
import java.util.List;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

public abstract class BaseFrontendFormService {
  protected <T> boolean handleConversion(ExpressionEvaluator paramExpressionEvaluator, String paramString, FormField paramFormField, T paramT, Object paramObject) {
    // Byte code:
    //   0: aload_3
    //   1: getfield type : Lio/fusionauth/domain/form/FormDataType;
    //   4: getstatic io/fusionauth/domain/form/FormDataType.bool : Lio/fusionauth/domain/form/FormDataType;
    //   7: if_acmpeq -> 30
    //   10: aload_3
    //   11: getfield type : Lio/fusionauth/domain/form/FormDataType;
    //   14: getstatic io/fusionauth/domain/form/FormDataType.date : Lio/fusionauth/domain/form/FormDataType;
    //   17: if_acmpeq -> 30
    //   20: aload_3
    //   21: getfield type : Lio/fusionauth/domain/form/FormDataType;
    //   24: getstatic io/fusionauth/domain/form/FormDataType.number : Lio/fusionauth/domain/form/FormDataType;
    //   27: if_acmpne -> 142
    //   30: getstatic io/fusionauth/app/service/user/BaseFrontendFormService$1.$SwitchMap$io$fusionauth$domain$form$FormDataType : [I
    //   33: aload_3
    //   34: getfield type : Lio/fusionauth/domain/form/FormDataType;
    //   37: invokevirtual ordinal : ()I
    //   40: iaload
    //   41: tableswitch default -> 92, 1 -> 68, 2 -> 76, 3 -> 84
    //   68: aload #5
    //   70: invokestatic convertToBooleans : (Ljava/lang/Object;)Ljava/lang/Object;
    //   73: goto -> 112
    //   76: aload #5
    //   78: invokestatic convertToDates : (Ljava/lang/Object;)Ljava/lang/Object;
    //   81: goto -> 112
    //   84: aload #5
    //   86: invokestatic convertToNumbers : (Ljava/lang/Object;)Ljava/lang/Object;
    //   89: goto -> 112
    //   92: new java/lang/IllegalStateException
    //   95: dup
    //   96: aload_3
    //   97: getfield type : Lio/fusionauth/domain/form/FormDataType;
    //   100: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   103: <illegal opcode> makeConcatWithConstants : (Ljava/lang/String;)Ljava/lang/String;
    //   108: invokespecial <init> : (Ljava/lang/String;)V
    //   111: athrow
    //   112: astore #6
    //   114: aload #6
    //   116: ifnonnull -> 121
    //   119: iconst_0
    //   120: ireturn
    //   121: aload_0
    //   122: aload_3
    //   123: aload #6
    //   125: invokevirtual handleCheckboxLists : (Lio/fusionauth/domain/form/FormField;Ljava/lang/Object;)[Ljava/lang/Object;
    //   128: astore #7
    //   130: aload #7
    //   132: ifnull -> 139
    //   135: aload #7
    //   137: astore #6
    //   139: goto -> 151
    //   142: aload_0
    //   143: aload_3
    //   144: aload #5
    //   146: invokevirtual handleCheckboxLists : (Lio/fusionauth/domain/form/FormField;Ljava/lang/Object;)[Ljava/lang/Object;
    //   149: astore #6
    //   151: aload #6
    //   153: ifnull -> 167
    //   156: aload_1
    //   157: aload_2
    //   158: aload #4
    //   160: aload #6
    //   162: invokeinterface setValue : (Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V
    //   167: iconst_1
    //   168: ireturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #26	-> 0
    //   #27	-> 30
    //   #28	-> 68
    //   #29	-> 76
    //   #30	-> 84
    //   #31	-> 92
    //   #27	-> 112
    //   #34	-> 114
    //   #36	-> 119
    //   #39	-> 121
    //   #40	-> 130
    //   #41	-> 135
    //   #43	-> 139
    //   #45	-> 142
    //   #49	-> 151
    //   #50	-> 156
    //   #53	-> 167
  }
  
  protected <T> T merge(ExpressionEvaluator paramExpressionEvaluator, T paramT1, T paramT2, List<FormField> paramList) {
    boolean bool = paramT1 instanceof io.fusionauth.domain.User;
    for (FormField formField : paramList) {
      if ((bool && formField.key.startsWith("registration.")) || (!bool && formField.key.startsWith("user.")) || formField.key.startsWith("consents["))
        continue; 
      int i = (paramT1 instanceof io.fusionauth.domain.User) ? "user.".length() : "registration.".length();
      String str = formField.key.substring(i);
      Object object = paramExpressionEvaluator.getValue(str, paramT1);
      if (object == null) {
        Object object1 = paramExpressionEvaluator.getValue(str, paramT2);
        if (object1 == null)
          continue; 
      } 
      paramExpressionEvaluator.setValue(str, paramT2, object);
    } 
    return paramT2;
  }
  
  protected boolean validOptions(FormField paramFormField, Object paramObject) {
    if (paramObject instanceof String)
      return paramFormField.options.contains(paramObject); 
    if (paramObject instanceof String[])
      for (String str : (String[])paramObject) {
        if (!paramFormField.options.contains(str))
          return false; 
      }  
    return true;
  }
  
  private Object[] handleCheckboxLists(FormField paramFormField, Object paramObject) {
    if (paramFormField.control == FormControl.checkbox && paramFormField.type != FormDataType.bool && 
      !paramObject.getClass().isArray() && !(paramObject instanceof java.util.Collection)) {
      Object[] arrayOfObject = (Object[])Array.newInstance(Object.class, 1);
      arrayOfObject[0] = paramObject;
      return arrayOfObject;
    } 
    return null;
  }
}
