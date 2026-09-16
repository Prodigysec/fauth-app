package io.fusionauth.app.action.admin.entity.grant;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.EntityGrant;
import io.fusionauth.domain.api.EntityGrantRequest;
import io.fusionauth.domain.api.EntityGrantResponse;
import io.fusionauth.domain.api.EntityResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "entity_manager"})
@List({@Redirect(code = "not-licensed", uri = "${successURI}"), @Redirect(code = "success", uri = "${successURI}")})
public class UpsertAction extends BaseAction {
  public Entity entity;
  
  public UUID entityId;
  
  public EntityGrant grant;
  
  public String q;
  
  public String successURI;
  
  @Inject
  public UpsertAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    if (this.entity != null && this.grant != null && (this.grant.userId != null || this.grant.recipientEntityId != null)) {
      EntityGrant entityGrant = retrieveExisting();
      if (entityGrant != null)
        this.grant = entityGrant; 
    } 
    return "input";
  }
  
  public String post() {
    if (this.entityId == null)
      return "input"; 
    EntityGrant entityGrant = retrieveExisting();
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.upsertEntityGrant(this.entityId, new EntityGrantRequest(this.grant)));
    if (entityGrant == null) {
      auditLogCreate();
    } else {
      this.grant.entity = this.entity;
      auditLogUpdate(entityGrant, this.grant);
    } 
    return "success";
  }
  
  @PostParameterMethod
  public void setup() {
    if (this.entityId != null)
      this.entity = ((EntityResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveEntity(this.entityId))).entity; 
    if (this.grant != null && this.grant.userId != null) {
      this.successURI = "/admin/user/manage/${grant.userId}?tenantId=${tenantId}";
    } else if (this.grant != null && this.grant.recipientEntityId != null) {
      this.successURI = "/admin/entity/manage/${grant.recipientEntityId}?tenantId=${tenantId}";
    } else {
      this.successURI = "/admin/";
    } 
  }
  
  private void auditLogCreate() {
    StringBuilder stringBuilder = (new StringBuilder()).append("Created an Entity grant for entity with Id [").append(this.entity.id).append("] and to ");
    if (this.grant.userId != null) {
      stringBuilder.append(" user with Id [")
        .append(this.grant.userId)
        .append("]");
    } else {
      stringBuilder.append(" entity with Id [")
        .append(this.grant.recipientEntityId)
        .append("]");
    } 
    stringBuilder.append(" with permissions ")
      .append(this.grant.permissions);
    writeAuditLog(stringBuilder.toString());
  }
  
  private void auditLogUpdate(EntityGrant paramEntityGrant1, EntityGrant paramEntityGrant2) {
    StringBuilder stringBuilder = (new StringBuilder()).append("Updated an Entity grant for entity with Id [").append(this.entity.id).append("] and to ");
    if (this.grant.userId != null) {
      stringBuilder.append(" user with Id [")
        .append(this.grant.userId)
        .append("]");
    } else {
      stringBuilder.append(" entity with Id [")
        .append(this.grant.recipientEntityId)
        .append("]");
    } 
    stringBuilder.append(" with permissions ")
      .append(this.grant.permissions);
    writeAuditLogForUpdate(stringBuilder.toString(), paramEntityGrant1, paramEntityGrant2);
  }
  
  private EntityGrant retrieveExisting() {
    ClientResponse<EntityGrantResponse, Errors> clientResponse = this.client.retrieveEntityGrant(this.entityId, this.grant.recipientEntityId, this.grant.userId);
    EntityGrant entityGrant = null;
    if (clientResponse.wasSuccessful()) {
      entityGrant = ((EntityGrantResponse)clientResponse.successResponse).grant;
    } else if (clientResponse.status != 404) {
      this.frontEndSupport.frontEndErrorHandling(clientResponse);
    } 
    return entityGrant;
  }
}
