package io.fusionauth.app.action.admin.theme;

import io.fusionauth.api.domain.ConfirmationRequiredReason;
import io.fusionauth.api.service.security.DefaultRecoveryCodeHasher;
import io.fusionauth.api.util.EncoderTools;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.action.admin.connector.BaseFormAction;
import io.fusionauth.app.domain.FormStepInfo;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationOAuthScope;
import io.fusionauth.domain.PasswordValidationRules;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.User;
import io.fusionauth.domain.WebAuthnCredential;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.domain.message.MessageType;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.PendingIdPLink;
import io.fusionauth.twofactor.TwoFactor;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class BaseViewAction extends BaseAction {
  @FTLVariable
  public static String secret = TwoFactor.generateBase64EncodedSecret();
  
  @FTLVariable
  public final List<String> recoveryCodes = DefaultRecoveryCodeHasher.generatePreviewCodes();
  
  @FTLVariable
  public String SAMLRequest = "fVFda8MgFP0rwT0bjTE2kSSlUAqBbg9rt3cbbRdItMu1H/v3s+3GGKN7ETmec67n3HJ6HvroaEbonK1QElMUGds63dldhV7WC5yjCLyyWvXOmgp9GEDTurSQyqXbuYN/Nu8HAz4KPhYqdBitdAo6kFYNBqRv5Wr2uJQsplIBmNGHQejGlhZYhd6830tCTqdTfEpjN+4Io5QSWpBA0tDtHn7o6f8D9qPzrnX9j4Df8U8I5Rf/kDXYN/MKFVRzXugc88kmHCIvcCGyBGsxaXORbbMNDd28fjfFLk01AAfT2Es/PkCUJZhmmKZrSmUiJMviCQ+QkEFal1f2WIMa+iOLzVkN+97E3RUtyddr+RRSNfNo4cZB+ftxkzi5Ip3G2ytVmkF1/Uzr0QAgclvRKtzDdxurzbkucq4S0SosGE0x32qGN7QQOOe5Ztkmb5nWJfkju0G/ll1/Ag==";
  
  @FTLVariable
  public boolean advancedRegistration;
  
  @FTLVariable
  public boolean allowEmailChange = true;
  
  @FTLVariable
  public boolean allowPhoneNumberChange = true;
  
  @FTLVariable
  public Application application;
  
  public UUID applicationId;
  
  @FTLVariable
  public ConfirmationRequiredReason[] availableConfirmationRequiredReasons = ConfirmationRequiredReason.values();
  
  @FTLVariable
  public List<String> availableMethods = List.of("authenticator", "email", "sms");
  
  @FTLVariable
  public Map<String, TwoFactorMethod> availableMethodsMap;
  
  @FTLVariable
  public String client_id;
  
  @FTLVariable
  public boolean collectBirthDate;
  
  @FTLVariable
  public boolean collectVerificationCode;
  
  @FTLVariable
  public String confirmationRequiredActionURI;
  
  @FTLVariable
  public ConfirmationRequiredReason confirmationRequiredReason;
  
  @FTLVariable
  public WebAuthnCredential credential;
  
  @FTLVariable
  public User currentUser;
  
  @FTLVariable
  public boolean deadEnd;
  
  @FTLVariable
  public PendingIdPLink devicePendingIdPLink;
  
  @FTLVariable
  public BaseFormAction.EditPasswordOption editPasswordOption;
  
  @FTLVariable
  public String email;
  
  @FTLVariable
  public Object fields;
  
  @FTLVariable
  public boolean formConfigured;
  
  @FTLVariable
  public boolean formField;
  
  @FTLVariable
  public FormStepInfo formStep;
  
  @FTLVariable
  public boolean hasDomainBasedIdentityProviders;
  
  @FTLVariable
  public boolean hideBirthDate;
  
  @FTLVariable
  public Map<String, List<BaseIdentityProvider<?>>> identityProviders;
  
  @FTLVariable
  public String loginId;
  
  @FTLVariable
  public RefreshToken.MetaData metaData;
  
  public String method;
  
  @FTLVariable
  public String methodId;
  
  @FTLVariable
  public String mobilePhone;
  
  @FTLVariable
  public boolean multiFactorAvailable;
  
  @FTLVariable
  public List<ApplicationOAuthScope> optionalScopes;
  
  @FTLVariable
  public boolean parentEmailRequired;
  
  @FTLVariable
  public boolean passwordSet;
  
  @FTLVariable
  public PasswordValidationRules passwordValidationRules;
  
  @FTLVariable
  public boolean passwordlessEnabled;
  
  @FTLVariable
  public PendingIdPLink pendingIdPLink;
  
  @FTLVariable
  public List<String> phoneMessageTypes;
  
  @FTLVariable
  public String phoneNumber;
  
  @FTLVariable
  public boolean pushEnabled;
  
  @FTLVariable
  public boolean readyToRegister;
  
  @FTLVariable
  public int recoverCodesAvailable;
  
  @FTLVariable
  public URI redirect_uri;
  
  @FTLVariable
  public boolean registrationEnabled;
  
  @FTLVariable
  public List<ApplicationOAuthScope> requiredScopes;
  
  @FTLVariable
  public String response_type;
  
  @FTLVariable
  public Map<String, Boolean> scopeConsents;
  
  @FTLVariable
  public String secretBase32Encoded;
  
  @FTLVariable
  public boolean showCaptcha;
  
  @FTLVariable
  public boolean showPasswordField;
  
  @FTLVariable
  public boolean showPasswordValidationRules;
  
  @FTLVariable
  public boolean showResendOrSelectMethod;
  
  @FTLVariable
  public boolean showWebAuthnReauthLink;
  
  @FTLVariable
  public int step;
  
  public String template;
  
  public String templatePath;
  
  @FTLVariable
  public Tenant tenant;
  
  public UUID themeId;
  
  @FTLVariable
  public String timezone;
  
  @FTLVariable
  public int totalSteps;
  
  @FTLVariable
  public boolean trustComputer;
  
  @FTLVariable
  public String twoFactorId;
  
  @FTLVariable
  public Set<String> unknownScopes;
  
  @FTLVariable
  public User user;
  
  @FTLVariable
  public boolean userCanReceivePush;
  
  @FTLVariable
  public int userCodeLength;
  
  @FTLVariable
  public String waitURL;
  
  @FTLVariable
  public boolean webauthnAvailable;
  
  protected BaseViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
    this.availableMethodsMap = Map.of("XY4L", (new TwoFactorMethod()).with(paramTwoFactorMethod -> paramTwoFactorMethod.method = "sms").with(paramTwoFactorMethod -> paramTwoFactorMethod.mobilePhone = "555-555-5555"), "UH7X", (new TwoFactorMethod()).with(paramTwoFactorMethod -> paramTwoFactorMethod.method = "email").with(paramTwoFactorMethod -> paramTwoFactorMethod.email = "erlich@piedpiper.com"), "FH3Q", (new TwoFactorMethod()).with(paramTwoFactorMethod -> paramTwoFactorMethod.method = "authenticator"));
    this.collectVerificationCode = true;
    this.confirmationRequiredActionURI = "#";
    this.confirmationRequiredReason = ConfirmationRequiredReason.passwordlessLogin;
    this.editPasswordOption = BaseFormAction.EditPasswordOption.update;
    this.email = "erlich@piedpiper.com";
    this.formConfigured = true;
    this.identityProviders = new HashMap<>();
    this.metaData = new RefreshToken.MetaData();
    this.method = "authenticator";
    this.methodId = "4HQB";
    this.mobilePhone = "555-555-5555";
    this.multiFactorAvailable = true;
    this.passwordSet = true;
    this.passwordValidationRules = new PasswordValidationRules();
    this.phoneMessageTypes = Arrays.<MessageType>stream(MessageType.values()).map(String::valueOf).toList();
    this.phoneNumber = "555-555-1111";
    this.recoverCodesAvailable = 10;
    this.secretBase32Encoded = EncoderTools.Base64.toBase32(secret);
    this.showCaptcha = true;
    this.showPasswordField = true;
    this.showResendOrSelectMethod = true;
    this.showWebAuthnReauthLink = true;
    this.twoFactorId = "dG9vIG1hbnkgc2VjcmV0cw";
    this.unknownScopes = new HashSet<>();
    this.userCodeLength = 6;
    this.waitURL = "";
    this.webauthnAvailable = true;
  }
}
