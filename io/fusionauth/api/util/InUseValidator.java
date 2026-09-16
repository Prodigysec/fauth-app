package io.fusionauth.api.util;

import com.google.inject.Inject;
import com.inversoft.validator.Validator;
import java.util.UUID;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

public class InUseValidator {
  private final ExpressionEvaluator expressionEvaluator;
  
  @Inject
  public InUseValidator(ExpressionEvaluator paramExpressionEvaluator) {
    this.expressionEvaluator = paramExpressionEvaluator;
  }
  
  public void notInUse(boolean paramBoolean, Validator paramValidator, UUID paramUUID1, String paramString1, UUID paramUUID2, String paramString2, String paramString3, String paramString4) {
    String str = (paramString3 != null) ? String.format("%s [%s] with Id [%s] assigned to [%s]", new Object[] { paramString2, paramString3, paramUUID2, paramString4 }) : String.format("%s with Id [%s] assigned to [%s]", new Object[] { paramString2, paramUUID2, paramString4 });
    paramValidator.notInUse(paramBoolean, paramString1, new Object[] { paramUUID1, str });
  }
  
  public void validateOptional(Validator paramValidator, UUID paramUUID, String paramString1, Object paramObject, String paramString2) {
    String str1 = paramString2.substring(paramString2.indexOf(".") + 1);
    UUID uUID1 = (UUID)this.expressionEvaluator.getValue(str1, paramObject);
    UUID uUID2 = (UUID)this.expressionEvaluator.getValue("id", paramObject);
    String str2 = (String)this.expressionEvaluator.getValue("name", paramObject);
    String str3 = paramObject.getClass().getSimpleName();
    if (str3.startsWith("_"))
      str3 = str3.substring(1); 
    if (uUID1 != null) {
      String str = (str2 != null) ? String.format("%s [%s] with Id [%s] assigned to [%s]", new Object[] { str3, str2, uUID2, paramString2 }) : String.format("%s with Id [%s] assigned to [%s]", new Object[] { str3, uUID2, paramString2 });
      paramValidator.notInUse(!uUID1.equals(paramUUID), paramString1, new Object[] { paramUUID, str });
    } 
  }
}
