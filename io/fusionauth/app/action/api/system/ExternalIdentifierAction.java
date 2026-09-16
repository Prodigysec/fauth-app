package io.fusionauth.app.action.api.system;

import com.google.inject.Inject;
import com.inversoft.authentication.api.domain.LocalKey;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.api.ExternalIdentifierResponse;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.primeframework.UndocumentedAPI;
import io.fusionauth.app.service.FrontEndSupport;
import java.util.ArrayList;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@UndocumentedAPI
@Action(value = "{id}", requiresAuthentication = true, scheme = {"api"})
public class ExternalIdentifierAction extends BaseTenantAPIAction {
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private final boolean internalLocalRequest;
  
  public String id;
  
  @JSONResponse
  public ExternalIdentifierResponse response;
  
  public String[] type;
  
  private ExternalIdentifierReaderService.ValidationResult result;
  
  @Inject
  public ExternalIdentifierAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ExternalIdentifierReaderService paramExternalIdentifierReaderService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    String str = paramFrontEndSupport.request.getHeader("Authorization");
    this.internalLocalRequest = (str != null && str.equals(LocalKey.KEY));
  }
  
  public String get() {
    if (this.result.id == null)
      return "missing"; 
    this
      
      .response = this.internalLocalRequest ? new ExternalIdentifierResponse(this.result.id) : new ExternalIdentifierResponse(this.result.id.secure());
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    ArrayList<ExternalIdentifier.ExternalIdType> arrayList = new ArrayList();
    if (this.type != null)
      for (String str : this.type)
        arrayList.add(ExternalIdentifier.ExternalIdType.safeValueOf(str));  
    if (arrayList.isEmpty()) {
      this.result = new ExternalIdentifierReaderService.ValidationResult();
      return;
    } 
    this.result = this.externalIdentifierReader.validate(getOptionalTenant(), this.id, arrayList.<ExternalIdentifier.ExternalIdType>toArray(new ExternalIdentifier.ExternalIdType[0]));
  }
}
