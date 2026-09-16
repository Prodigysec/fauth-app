package io.fusionauth.api.util;

import com.inversoft.error.Errors;
import freemarker.core.ParseException;
import freemarker.template.TemplateException;
import io.fusionauth.api.domain.message.BaseMessageResult;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.event.UserLoginNewDeviceEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;

public class PhoneMessageTools {
  public static final Map<String, String> FIELD_NAME_MAPPING = new HashMap<>();
  
  public static final Map<String, Object> MOCK_PARAMETERS = new HashMap<>();
  
  public static String formatCodeForVoice(@Nonnull String paramString) {
    StringBuilder stringBuilder = new StringBuilder();
    char[] arrayOfChar = paramString.toCharArray();
    for (byte b = 0; b < arrayOfChar.length; b++) {
      if (b > 0)
        stringBuilder.append(' '); 
      stringBuilder.append(arrayOfChar[b]);
    } 
    return stringBuilder.toString();
  }
  
  public static String loadTemplate(Class<?> paramClass, String paramString) {
    return URITools.loadResourceByURI(paramClass, paramString);
  }
  
  public static Errors translateErrors(BaseMessageResult paramBaseMessageResult) {
    Errors errors = new Errors();
    if (paramBaseMessageResult.renderErrors.isEmpty() && paramBaseMessageResult.parseErrors.isEmpty())
      return errors; 
    paramBaseMessageResult.parseErrors.forEach((paramString, paramParseException) -> paramErrors.addFieldError(FIELD_NAME_MAPPING.get(paramString), "[invalidTemplate]" + (String)FIELD_NAME_MAPPING.get(paramString), null, new Object[] { paramParseException.getMessage() }));
    paramBaseMessageResult.renderErrors.forEach((paramString, paramTemplateException) -> paramErrors.addFieldError(FIELD_NAME_MAPPING.get(paramString), "[invalidTemplate]" + (String)FIELD_NAME_MAPPING.get(paramString), null, new Object[] { paramTemplateException.getMessage() }));
    return errors;
  }
  
  static {
    UUID uUID = UUID.randomUUID();
    User user = MessageTools.buildMockUser("user", UUID.fromString("00000000-0000-0000-0000-000000000001"), uUID);
    Application application = (new Application()).with(paramApplication -> paramApplication.id = UUID.randomUUID()).with(paramApplication -> paramApplication.name = "My Application").with(paramApplication -> paramApplication.tenantId = paramUUID).with(paramApplication -> paramApplication.oauthConfiguration.clientId = UUID.randomUUID().toString());
    Tenant tenant = (new Tenant()).with(paramTenant -> paramTenant.id = paramUUID).with(paramTenant -> paramTenant.name = "My Tenant");
    MOCK_PARAMETERS.put("user", user.secure());
    MOCK_PARAMETERS.put("application", application.secure());
    MOCK_PARAMETERS.put("baseUrl", "https://example.com");
    MOCK_PARAMETERS.put("tenant", tenant.secure());
    String str = "123456";
    MOCK_PARAMETERS.put("code", str);
    MOCK_PARAMETERS.put("spokenCode", formatCodeForVoice(str));
    MOCK_PARAMETERS.put("userId", UUID.fromString("00000000-0000-0000-0000-000000000000"));
    MOCK_PARAMETERS.put("verificationOneTimeCode", "987654");
    MOCK_PARAMETERS.put("verificationId", "fQAhksO1iDNgTq0dixKsgVk58AixT---HIKpFXS8SKo");
    MOCK_PARAMETERS.put("changePasswordId", "rB2TUINSXpLb4zthgaMX0z4VLB1NF4e0HkgjIC6SjD8");
    MOCK_PARAMETERS.put("oneTimeCode", "234567");
    MOCK_PARAMETERS.put("event", new UserLoginNewDeviceEvent());
    MOCK_PARAMETERS.put("method", user.twoFactor.methods.get(1));
    FIELD_NAME_MAPPING.put("message", "messageTemplate.defaultTemplate");
  }
}
