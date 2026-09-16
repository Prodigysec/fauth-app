package io.fusionauth.api.service.samlv2;

import io.fusionauth.domain.Application;
import io.fusionauth.samlv2.service.SAMLv2Service;
import java.security.PublicKey;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class FusionAuthRedirectBindingSignatureHelper implements SAMLv2Service.RedirectBindingSignatureHelper {
  private final Function<String, Application> applicationResolver;
  
  private final String issuer;
  
  private final Function<UUID, PublicKey> keyResolver;
  
  private final Function<Application.SAMLv2Configuration, List<UUID>> verificationKeyIdsFunction;
  
  private final Function<Application.SAMLv2Configuration, Boolean> verifySignatureFunction;
  
  private Application application;
  
  public FusionAuthRedirectBindingSignatureHelper(Function<String, Application> paramFunction, String paramString, Function<UUID, PublicKey> paramFunction1, Function<Application.SAMLv2Configuration, List<UUID>> paramFunction2, Function<Application.SAMLv2Configuration, Boolean> paramFunction3) {
    this.applicationResolver = paramFunction;
    this.keyResolver = paramFunction1;
    this.issuer = paramString;
    this.verificationKeyIdsFunction = paramFunction2;
    this.verifySignatureFunction = paramFunction3;
  }
  
  public PublicKey publicKey() {
    List list = this.verificationKeyIdsFunction.apply((resolveApplication()).samlv2Configuration);
    return (list == null || list.isEmpty()) ? null : this.keyResolver.apply((UUID)list.getFirst());
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
