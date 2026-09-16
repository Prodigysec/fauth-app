package io.fusionauth.app.action.admin.system.auditLog;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.admin.BaseSearchAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.AuditLog;
import io.fusionauth.domain.api.AuditLogSearchRequest;
import io.fusionauth.domain.api.AuditLogSearchResponse;
import io.fusionauth.domain.search.AuditLogSearchCriteria;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "audit_log_viewer"})
public class IndexAction extends BaseSearchAction<AuditLog, AuditLogSearchCriteria> {
  @FTLVariable
  public String q;
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  protected AuditLogSearchCriteria defaultSearchCriteria() {
    return new AuditLogSearchCriteria();
  }
  
  protected SearchResults<AuditLog> search() {
    AuditLogSearchResponse auditLogSearchResponse = this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.searchAuditLogs(new AuditLogSearchRequest(this.s)));
    return new SearchResults<>(auditLogSearchResponse.auditLogs, auditLogSearchResponse.total);
  }
}
