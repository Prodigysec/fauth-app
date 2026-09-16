package io.fusionauth.api.service.samlv2;

import io.fusionauth.domain.Application;
import io.fusionauth.samlv2.service.SAMLv2Service;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import javax.xml.crypto.KeySelector;

public class FusionAuthPostBindingSignatureHelper implements SAMLv2Service.PostBindingSignatureHelper {
  private final Function<String, Application> applicationResolver;
  
  private final String issuer;
  
  private final Function<List<UUID>, KeySelector> keySelectorResolver;
  
  private final Function<Application.SAMLv2Configuration, List<UUID>> verificationKeyIdsFunction;
  
  private final Function<Application.SAMLv2Configuration, Boolean> verifySignatureFunction;
  
  private Application application;
  
  public FusionAuthPostBindingSignatureHelper(Function<String, Application> paramFunction, String paramString, Function<List<UUID>, KeySelector> paramFunction1, Function<Application.SAMLv2Configuration, List<UUID>> paramFunction2, Function<Application.SAMLv2Configuration, Boolean> paramFunction3) {
    this.applicationResolver = paramFunction;
    this.keySelectorResolver = paramFunction1;
    this.issuer = paramString;
    this.verificationKeyIdsFunction = paramFunction2;
    this.verifySignatureFunction = paramFunction3;
  }
  
  public KeySelector keySelector() {
    Application.SAMLv2Configuration sAMLv2Configuration = (resolveApplication()).samlv2Configuration;
    return this.keySelectorResolver.apply(this.verificationKeyIdsFunction.apply(sAMLv2Configuration));
  }
  
  public Application resolveApplication() {
    if (this.application == null)
      this.application = this.applicationResolver.apply(this.issuer); 
    return this.application;
  }
  
  public boolean verifySignature() {
    return (resolveApplication() != null && ((Boolean)this.verifySignatureFunction.apply((resolveApplication()).samlv2Configuration)).booleanValue());
  }
}
