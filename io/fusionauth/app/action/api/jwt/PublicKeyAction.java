package io.fusionauth.app.action.api.jwt;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.util.StringTools;
import io.fusionauth.api.security.KeyCache;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.KeyReaderService;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.api.PublicKeyResponse;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class PublicKeyAction extends BaseTenantAPIAction {
  private final ApplicationCache applicationCache;
  
  private final ApplicationReaderService applicationReader;
  
  private final KeyCache keyCache;
  
  private final KeyReaderService keyReader;
  
  private final TenantReaderService tenantReader;
  
  public String applicationId;
  
  public String kid;
  
  @JSONResponse
  public PublicKeyResponse response;
  
  private Application application;
  
  private Key key;
  
  @Inject
  public PublicKeyAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ApplicationCache paramApplicationCache, ApplicationReaderService paramApplicationReaderService, KeyCache paramKeyCache, KeyReaderService paramKeyReaderService, TenantReaderService paramTenantReaderService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.applicationCache = paramApplicationCache;
    this.applicationReader = paramApplicationReaderService;
    this.keyCache = paramKeyCache;
    this.keyReader = paramKeyReaderService;
    this.tenantReader = paramTenantReaderService;
    this.useTenantCache = true;
  }
  
  public String get() {
    if (this.applicationId != null) {
      if (this.application == null)
        return "missing"; 
      Objects.requireNonNull(this.tenantReader);
      UUID uUID = this.application.jwtConfiguration.enabled ? this.application.jwtConfiguration.accessTokenKeyId : (this.frontEndSupport.tenantCache.get(this.application.tenantId, this.tenantReader::retrieveById)).jwtConfiguration.accessTokenKeyId;
      this



        
        .key = this.keyCache.getAll().stream().filter(Key::isPair).filter(paramKey -> paramKey.id.equals(paramUUID)).findFirst().orElse(null);
      if (this.key != null) {
        this.response = new PublicKeyResponse(this.key.publicKey);
      } else {
        this.response = new PublicKeyResponse();
      } 
    } else if (this.kid != null) {
      if (this.key == null || this.key.publicKey == null)
        return "missing"; 
      this.response = new PublicKeyResponse(this.key.publicKey);
    } else {
      this

        
        .response = new PublicKeyResponse((Map<String, String>)this.keyCache.getAll().stream().filter(Key::isPair).collect(Collectors.toMap(paramKey -> paramKey.kid, paramKey -> paramKey.publicKey)));
    } 
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validate() {
    if (this.applicationId != null) {
      Objects.requireNonNull(this.applicationReader);
      this.application = this.applicationCache.get(getOptionalTenantId(), StringTools.parseUUID(this.applicationId), this.applicationReader::retrieveById);
    } 
    if (this.kid != null)
      this.key = this.keyReader.retrieveByKid(this.kid); 
  }
}
