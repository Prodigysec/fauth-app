package io.fusionauth.app.action.admin.entity.type;

import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.EntityType;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.api.KeyResponse;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;

public abstract class BaseFormAction extends BaseAction {
  @FTLVariable
  public Key accessTokenKey;
  
  @FTLVariable
  public List<Key> accessTokenVerificationKeys = List.of();
  
  public String confirm;
  
  public EntityType entityType = new EntityType();
  
  public UUID entityTypeId;
  
  protected BaseFormAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  @FormPrepareMethod
  public void prepare() {
    if (this.entityType.jwtConfiguration.accessTokenKeyId != null)
      fetchKey(this.entityType.jwtConfiguration.accessTokenKeyId).ifPresent(paramKey -> this.accessTokenKey = paramKey); 
    this



      
      .accessTokenVerificationKeys = this.entityType.jwtConfiguration.accessTokenVerificationKeyIds.stream().filter(Objects::nonNull).map(this::fetchKey).flatMap(Optional::stream).toList();
    if (this.entityType.jwtConfiguration.timeToLiveInSeconds == 0)
      this.entityType.jwtConfiguration.timeToLiveInSeconds = 60; 
  }
  
  private Optional<Key> fetchKey(UUID paramUUID) {
    ClientResponse<KeyResponse, Errors> clientResponse = this.superClient.retrieveKey(paramUUID);
    if (clientResponse.wasSuccessful())
      return Optional.ofNullable(((KeyResponse)clientResponse.successResponse).key); 
    return Optional.empty();
  }
}
