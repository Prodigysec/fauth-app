package io.fusionauth.api.util;

import com.inversoft.util.SecurityTools;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.User;
import io.fusionauth.twofactor.TwoFactor;
import java.util.ArrayList;
import java.util.List;

public class TwoFactorTools {
  public static String generateUniqueTwoFactorMethodId(User paramUser) {
    String str;
    do {
      str = SecurityTools.secureRandomAlphaNumeric(4);
    } while (paramUser.twoFactor.getMethodById(str) != null);
    return str;
  }
  
  public static List<String> getPhoneMessageTypesForTenant(Tenant paramTenant) {
    ArrayList<String> arrayList = new ArrayList();
    if (paramTenant.multiFactorConfiguration.sms.enabled)
      arrayList.add("SMS"); 
    if (paramTenant.multiFactorConfiguration.voice.enabled)
      arrayList.add("Voice"); 
    return List.copyOf(arrayList);
  }
  
  public static TwoFactorMethod newAuthenticatorTwoFactor(String paramString) {
    return (new TwoFactorMethod("authenticator")).with(paramTwoFactorMethod -> paramTwoFactorMethod.secret = (paramString != null) ? paramString : TwoFactor.generateBase64EncodedSecret());
  }
  
  public static TwoFactorMethod newEmailTwoFactor(String paramString) {
    return (new TwoFactorMethod("email")).with(paramTwoFactorMethod -> paramTwoFactorMethod.email = paramString);
  }
  
  public static TwoFactorMethod newSMSTwoFactor(String paramString) {
    return (new TwoFactorMethod("sms")).with(paramTwoFactorMethod -> paramTwoFactorMethod.mobilePhone = paramString);
  }
}
