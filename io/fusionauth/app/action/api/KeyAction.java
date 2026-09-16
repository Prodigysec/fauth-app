package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import io.fusionauth.api.service.system.KeyReaderService;
import io.fusionauth.api.service.system.KeyService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.api.KeyRequest;
import io.fusionauth.domain.api.KeyResponse;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{keyId}", requiresAuthentication = true, scheme = {"api-no-tenant"})
public class KeyAction extends BaseAPIAction {
  private final KeyReaderService keyReader;
  
  private final KeyService keyService;
  
  public UUID keyId;
  
  @JSONRequest
  public KeyRequest request = new KeyRequest();
  
  @JSONResponse
  public KeyResponse response;
  
  private KeyService.ValidationResult result;
  
  @Inject
  public KeyAction(FrontEndSupport paramFrontEndSupport, KeyReaderService paramKeyReaderService, KeyService paramKeyService) {
    super(paramFrontEndSupport);
    this.keyReader = paramKeyReaderService;
    this.keyService = paramKeyService;
  }
  
  public String delete() {
    this.keyService.delete(this.keyId);
    return "success";
  }
  
  public String get() {
    if (this.keyId == null) {
      this.response = new KeyResponse((List<Key>)this.keyReader.retrieveAll().stream().map(Key::secure).collect(Collectors.toList()));
    } else {
      Key key = this.keyReader.retrieveById(this.keyId);
      if (key == null)
        return "missing"; 
      this.response = new KeyResponse(key.secure());
    } 
    return "render";
  }
  
  public String put() {
    if (this.result.existing == null)
      return "missing"; 
    this.keyService.update(this.result.existing, this.request.key);
    this.response = new KeyResponse(this.request.key.secure());
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"PUT"})
  public void validate() {
    if (this.request == null || this.request.key == null) {
      this.frontEndSupport.addFieldError("key", "[missing]key", new Object[0]);
      return;
    } 
    this.request.key.normalize();
    this.request.key.id = this.keyId;
    this.request.key.kid = null;
    this.result = this.keyService.validate(this.request.key, false);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.keyId == null) {
      this.frontEndSupport.addFieldError("keyId", "[missing]keyId", new Object[0]);
      return;
    } 
    this.frontEndSupport.transfer(this.keyService.validateDelete(this.keyId));
  }
}
