package io.fusionauth.app.action.admin.consent;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.admin.BaseSearchAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Consent;
import io.fusionauth.domain.api.ConsentResponse;
import io.fusionauth.domain.api.ConsentSearchRequest;
import io.fusionauth.domain.api.ConsentSearchResponse;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.ConsentSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "consent_manager"})
public class IndexAction extends BaseSearchAction<Consent, ConsentSearchCriteria> {
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  protected ConsentSearchCriteria defaultSearchCriteria() {
    return new ConsentSearchCriteria();
  }
  
  protected SearchResults<Consent> search() {
    UUID uUID = parseUUID(this.s.name);
    if (uUID != null) {
      ClientResponse<ConsentResponse, Void> clientResponse = this.client.retrieveConsent(uUID);
      if (clientResponse.wasSuccessful())
        return new SearchResults<>(List.of(((ConsentResponse)clientResponse.getSuccessResponse()).consent), 1L); 
      return null;
    } 
    ConsentSearchResponse consentSearchResponse = superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.searchConsents(new ConsentSearchRequest(this.s)));
    return new SearchResults<>(consentSearchResponse.consents, consentSearchResponse.total);
  }
}
