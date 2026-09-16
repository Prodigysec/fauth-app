package io.fusionauth.app.action.ajax.key;

import io.fusionauth.api.security.SecurityTools;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Key;
import io.fusionauth.jwt.domain.Algorithm;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

public class BaseFormAction extends BaseAJAXAction {
  public List<Algorithm> algorithms = new ArrayList<>();
  
  public Key key = new Key();
  
  public UUID keyId;
  
  @FTLVariable
  public String t;
  
  public Key.KeyType type;
  
  protected BaseFormAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  @PostParameterMethod
  public void setupAlgorithms() {
    if (this.key.algorithm != null)
      this.type = SecurityTools.keyTypeFromAlgorithm(this.key.algorithm); 
    if (this.t != null)
      switch (this.t) {
        case "ec-pair":
        case "ec-private":
        case "certificate-ec-pair":
          this.type = Key.KeyType.EC;
          break;
        case "ed-pair":
        case "ed-private":
        case "certificate-ed-pair":
          this.type = Key.KeyType.OKP;
          break;
        case "rsa-pair":
        case "rsa-private":
        case "certificate-rsa-pair":
          this.type = Key.KeyType.RSA;
          break;
        case "hmac":
          this.type = Key.KeyType.HMAC;
          break;
        case "secret":
          this.type = Key.KeyType.Secret;
          break;
      }  
    this.key.type = this.type;
    if (this.key.type == null)
      return; 
    switch (this.type) {
      case EC:
        this.algorithms.add(Algorithm.ES256);
        this.algorithms.add(Algorithm.ES384);
        this.algorithms.add(Algorithm.ES512);
        break;
      case HMAC:
        this.algorithms.add(Algorithm.HS256);
        this.algorithms.add(Algorithm.HS384);
        this.algorithms.add(Algorithm.HS512);
        break;
      case OKP:
        this.algorithms.add(Algorithm.Ed25519);
        break;
      case RSA:
        this.algorithms.add(Algorithm.RS256);
        this.algorithms.add(Algorithm.RS384);
        this.algorithms.add(Algorithm.RS512);
        break;
    } 
  }
}
