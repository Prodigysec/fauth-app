package io.fusionauth.app.action.admin.system.loginRecord;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.admin.BaseSearchAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.DisplayableRawLogin;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.LoginRecordSearchRequest;
import io.fusionauth.domain.api.LoginRecordSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.LoginRecordSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.Comparator;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "system_manager"})
public class IndexAction extends BaseSearchAction<DisplayableRawLogin, LoginRecordSearchCriteria> {
  public List<Application> applications;
  
  @FTLVariable
  public String q;
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  @FormPrepareMethod
  public void prepareForm() {
    this.applications = ((ApplicationResponse)this.delegate.execute(FusionAuthClient::retrieveApplications)).applications;
    this.applications.sort(Comparator.comparing(paramApplication -> paramApplication.name));
    if (this.tenants.size() > 1)
      this.applications.forEach(paramApplication -> {
            if (paramApplication.tenantId != null) {
              Tenant tenant = this.tenants.get(paramApplication.tenantId);
              paramApplication.name = "%s (%s)".formatted(new Object[] { paramApplication.name, tenant.name });
            } else {
              paramApplication.name = "%s".formatted(new Object[] { paramApplication.name });
            } 
          }); 
  }
  
  protected LoginRecordSearchCriteria defaultSearchCriteria() {
    return new LoginRecordSearchCriteria();
  }
  
  protected SearchResults<DisplayableRawLogin> search() {
    LoginRecordSearchResponse loginRecordSearchResponse = superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.searchLoginRecords(new LoginRecordSearchRequest(this.s)));
    return new SearchResults<>(loginRecordSearchResponse.logins, loginRecordSearchResponse.total);
  }
}
