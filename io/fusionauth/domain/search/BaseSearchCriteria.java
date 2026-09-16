package io.fusionauth.domain.search;

import io.fusionauth.domain.util.SQLTools;
import java.util.Set;

public abstract class BaseSearchCriteria {
  public int numberOfResults = 25;
  
  public String orderBy;
  
  public int startRow;
  
  public abstract BaseSearchCriteria prepare();
  
  public BaseSearchCriteria secure() {
    this.orderBy = SQLTools.sanitizeOrderBy(this.orderBy);
    return this;
  }
  
  public abstract Set<String> supportedOrderByColumns();
  
  protected String defaultOrderBy() {
    return null;
  }
}
