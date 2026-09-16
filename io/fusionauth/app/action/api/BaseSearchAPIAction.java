package io.fusionauth.app.action.api;

import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.error.Errors;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.search.BaseSearchCriteria;
import java.util.Locale;
import java.util.Set;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.annotation.PostValidationMethod;

public abstract class BaseSearchAPIAction<T extends BaseSearchCriteria> extends BaseTenantAPIAction {
  protected BaseSearchAPIAction(AuthenticationKeyCache paramAuthenticationKeyCache, FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
  }
  
  public String get() {
    return search();
  }
  
  public int getNumberOfResults() {
    return ((BaseSearchCriteria)criteria()).numberOfResults;
  }
  
  public void setNumberOfResults(int paramInt) {
    ((BaseSearchCriteria)criteria()).numberOfResults = paramInt;
  }
  
  public String getOrderBy() {
    return ((BaseSearchCriteria)criteria()).orderBy;
  }
  
  public void setOrderBy(String paramString) {
    ((BaseSearchCriteria)criteria()).orderBy = paramString;
  }
  
  public int getStartRow() {
    return ((BaseSearchCriteria)criteria()).startRow;
  }
  
  public void setStartRow(int paramInt) {
    ((BaseSearchCriteria)criteria()).startRow = paramInt;
  }
  
  public String post() {
    return search();
  }
  
  @PostValidationMethod
  public void prepare() {
    criteria().secure().prepare();
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validate() {
    this.frontEndSupport.transfer(validateCriteria(criteria()));
    this.frontEndSupport.transfer(validateOrderBy(criteria()));
  }
  
  protected abstract T criteria();
  
  protected boolean resolveDefaultTenant() {
    return false;
  }
  
  protected abstract String search();
  
  protected Errors validateCriteria(T paramT) {
    return new Errors();
  }
  
  protected Errors validateOrderBy(T paramT) {
    Errors errors = new Errors();
    Set<String> set = paramT.supportedOrderByColumns();
    if (set.isEmpty())
      return errors; 
    if (((BaseSearchCriteria)paramT).orderBy != null && ((BaseSearchCriteria)paramT).orderBy.length() > 0) {
      String[] arrayOfString = ((BaseSearchCriteria)paramT).orderBy.split(",");
      for (String str1 : arrayOfString) {
        String str2 = str1.trim();
        String str3 = str2.toLowerCase(Locale.ROOT);
        if (str3.endsWith("asc")) {
          str2 = str2.substring(0, str2.length() - 3).trim();
        } else if (str3.endsWith("desc")) {
          str2 = str2.substring(0, str2.length() - 4).trim();
        } 
        if (!set.contains(str2)) {
          String str = this.frontEndSupport.isGET() ? "orderBy" : "search.orderBy";
          errors.addFieldError(str, "[invalid]" + str, null, new Object[] { str2, String.join(", ", (Iterable)set) });
        } 
      } 
    } 
    return errors;
  }
}
