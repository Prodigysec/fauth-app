package io.fusionauth.app.action.api;

import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.SearchContinuationToken;
import io.fusionauth.api.domain.SearchEngineType;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.search.BaseElasticSearchCriteria;
import io.fusionauth.domain.search.SortField;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;

public abstract class BaseSearchTenantAPIAction extends BaseTenantAPIAction {
  public boolean accurateTotal;
  
  public List<UUID> ids = new ArrayList<>();
  
  public String nextResults;
  
  public int numberOfResults = 25;
  
  public String query;
  
  public String queryString;
  
  public List<SortField> sortFields;
  
  public int startRow;
  
  protected SearchContinuationToken continuationToken;
  
  protected List<String> lastSort;
  
  protected String pointInTimeId;
  
  protected BaseSearchTenantAPIAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
  }
  
  public String get() {
    return search();
  }
  
  public String post() {
    BaseElasticSearchCriteria baseElasticSearchCriteria = criteria();
    this.ids = baseElasticSearchCriteria.ids;
    this.accurateTotal = baseElasticSearchCriteria.accurateTotal;
    this.query = baseElasticSearchCriteria.query;
    this.queryString = baseElasticSearchCriteria.queryString;
    this.numberOfResults = baseElasticSearchCriteria.numberOfResults;
    this.startRow = baseElasticSearchCriteria.startRow;
    this.sortFields = baseElasticSearchCriteria.sortFields;
    this.nextResults = baseElasticSearchCriteria.nextResults;
    return search();
  }
  
  @PostParameterMethod
  public void prepare() {
    BaseElasticSearchCriteria baseElasticSearchCriteria = criteria();
    if (baseElasticSearchCriteria != null)
      baseElasticSearchCriteria.secure()
        .prepare(); 
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validate() {
    BaseElasticSearchCriteria baseElasticSearchCriteria = criteria();
    if (baseElasticSearchCriteria == null || (baseElasticSearchCriteria.ids.isEmpty() && baseElasticSearchCriteria.query == null && baseElasticSearchCriteria.queryString == null && baseElasticSearchCriteria.nextResults == null)) {
      this.frontEndSupport.addGeneralError("[invalid]", new Object[0]);
      return;
    } 
    if (baseElasticSearchCriteria.nextResults != null) {
      this.continuationToken = validateContinuationToken(baseElasticSearchCriteria.query, baseElasticSearchCriteria.queryString, baseElasticSearchCriteria.startRow, baseElasticSearchCriteria.sortFields, baseElasticSearchCriteria.nextResults);
      if (this.continuationToken != null) {
        baseElasticSearchCriteria.query = this.continuationToken.q;
        baseElasticSearchCriteria.queryString = this.continuationToken.qs;
        baseElasticSearchCriteria.sortFields = this.continuationToken.sf;
        this.lastSort = this.continuationToken.ls;
        this.pointInTimeId = this.continuationToken.pit;
        Errors errors = validateSearchQuery(getOptionalTenantId(), baseElasticSearchCriteria.query, baseElasticSearchCriteria.queryString, baseElasticSearchCriteria.sortFields, baseElasticSearchCriteria.startRow, baseElasticSearchCriteria.numberOfResults);
        if (!errors.empty())
          this.frontEndSupport.addFieldError("nextResults", "[invalid]nextResults", new Object[0]); 
      } 
    } else if (baseElasticSearchCriteria.query != null || baseElasticSearchCriteria.queryString != null) {
      this.frontEndSupport.transfer(validateSearchQuery(getOptionalTenantId(), baseElasticSearchCriteria.query, baseElasticSearchCriteria.queryString, baseElasticSearchCriteria.sortFields, baseElasticSearchCriteria.startRow, baseElasticSearchCriteria.numberOfResults));
    } 
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    if (!this.ids.isEmpty())
      return; 
    if (this.nextResults != null) {
      this.continuationToken = validateContinuationToken(this.query, this.queryString, this.startRow, this.sortFields, this.nextResults);
      if (this.continuationToken != null) {
        this.query = this.continuationToken.q;
        this.queryString = this.continuationToken.qs;
        this.sortFields = this.continuationToken.sf;
        this.lastSort = this.continuationToken.ls;
        this.pointInTimeId = this.continuationToken.pit;
        Errors errors = validateSearchQuery(getOptionalTenantId(), this.query, this.queryString, this.sortFields, this.startRow, this.numberOfResults);
        if (!errors.empty())
          this.frontEndSupport.addFieldError("nextResults", "[invalid]nextResults", new Object[0]); 
      } 
      return;
    } 
    if (this.query == null && this.queryString == null) {
      this.frontEndSupport.addGeneralError("[invalid]", new Object[0]);
      return;
    } 
    this.frontEndSupport.transfer(validateSearchQuery(getOptionalTenantId(), this.query, this.queryString, this.sortFields, this.startRow, this.numberOfResults));
  }
  
  protected abstract BaseElasticSearchCriteria criteria();
  
  protected abstract String search();
  
  protected abstract Errors validateSearchQuery(UUID paramUUID, String paramString1, String paramString2, List<SortField> paramList, int paramInt1, int paramInt2);
  
  private SearchContinuationToken decodeToken(String paramString) {
    try {
      return (SearchContinuationToken)this.frontEndSupport.objectMapper.readValue(Base64.getUrlDecoder().decode(paramString), SearchContinuationToken.class);
    } catch (IOException iOException) {
      return null;
    } 
  }
  
  private SearchContinuationToken validateContinuationToken(String paramString1, String paramString2, int paramInt, List<SortField> paramList, String paramString3) {
    Errors errors = (new Validator()).blank(paramString1, "query", new Object[0]).blank(paramString2, "queryString", new Object[0]).ensureWithCode((paramInt == 0), "startRow", "[notMissing]startRow", new Object[0]).empty(paramList, "sortFields", new Object[0]).done();
    if (!errors.empty()) {
      this.frontEndSupport.transfer(errors);
      return null;
    } 
    if (this.frontEndSupport.configuration.searchEngineType() == SearchEngineType.database) {
      this.frontEndSupport.addFieldError("nextResults", "[unsupported]nextResults", new Object[0]);
      return null;
    } 
    this.continuationToken = decodeToken(paramString3);
    if (this.continuationToken == null) {
      this.frontEndSupport.addFieldError("nextResults", "[invalid]nextResults", new Object[0]);
      return null;
    } 
    if (this.continuationToken.sf == null || this.continuationToken.sf.isEmpty()) {
      this.frontEndSupport.addFieldError("nextResults", "[invalid]nextResults", new Object[0]);
      return null;
    } 
    return this.continuationToken;
  }
}
