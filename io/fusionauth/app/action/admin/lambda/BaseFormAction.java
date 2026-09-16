package io.fusionauth.app.action.admin.lambda;

import com.inversoft.error.Error;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Lambda;
import io.fusionauth.domain.LambdaType;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public abstract class BaseFormAction extends BaseAction {
  public static final LambdaType[] types = LambdaType.values();
  
  public static final Map<String, List<LambdaType>> typeGroups = new LinkedHashMap<>();
  
  private final Pattern ContainsFetch = Pattern.compile("^.*[=\\s]fetch\\s*\\(.*$", 8);
  
  public Lambda lambda = new Lambda();
  
  public UUID lambdaId;
  
  protected LambdaType existingType;
  
  protected BaseFormAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
    paramFrontEndSupport.fieldMapperFunction = (paramString -> paramString);
    paramFrontEndSupport.errorMapperFunction = (paramError -> {
        if (paramError.code.equals("[functionMissing]lambda.body")) {
          String str = (this.existingType != null) ? this.existingType.getFunctionName() : this.lambda.type.getFunctionName();
          paramError.values = new Object[] { str };
        } 
        return paramError;
      });
  }
  
  protected void warnOnFetchUsage() {
    if (ReactorStatusValidator.isNotLicensedFor(this.reactorStatus, paramReactorStatus -> paramReactorStatus.advancedLambdas) && 
      this.lambda.body != null && 
      this.ContainsFetch.matcher(this.lambda.body).find())
      this.frontEndSupport.addGeneralInfo("[notLicensed]AdvancedLambdasFeature", new Object[0]); 
  }
  
  static {
    Arrays.sort(types, Comparator.comparing(Enum::name));
    typeGroups.put("lambda.type.group.common", List.of(LambdaType.LoginValidation, LambdaType.MFARequirement, LambdaType.SelfServiceRegistrationValidation));
    typeGroups.put("lambda.type.group.populate", List.of(LambdaType.ClientCredentialsJWTPopulate, LambdaType.JWTPopulate, LambdaType.SAMLv2Populate, LambdaType.UserInfoPopulate));
    typeGroups.put("lambda.type.group.reconcile", List.of((Object[])new LambdaType[] { 
            LambdaType.AppleReconcile, LambdaType.EpicGamesReconcile, LambdaType.ExternalJWTReconcile, LambdaType.FacebookReconcile, LambdaType.GoogleReconcile, LambdaType.HYPRReconcile, LambdaType.LDAPConnectorReconcile, LambdaType.LinkedInReconcile, LambdaType.NintendoReconcile, LambdaType.OpenIDReconcile, 
            LambdaType.SAMLv2Reconcile, LambdaType.SonyPSNReconcile, LambdaType.SteamReconcile, LambdaType.TwitchReconcile, LambdaType.TwitterReconcile, LambdaType.XboxReconcile }));
    typeGroups.put("lambda.type.group.scim", List.of(LambdaType.SCIMServerGroupRequestConverter, LambdaType.SCIMServerGroupResponseConverter, LambdaType.SCIMServerUserRequestConverter, LambdaType.SCIMServerUserResponseConverter));
  }
}
