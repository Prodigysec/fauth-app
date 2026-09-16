package io.fusionauth.domain.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class BaseElasticSearchCriteria extends BaseSearchCriteria {
  public boolean accurateTotal;
  
  public List<UUID> ids = new ArrayList<>();
  
  public String nextResults;
  
  public String query;
  
  public String queryString;
  
  public List<SortField> sortFields = new ArrayList<>();
  
  public BaseElasticSearchCriteria prepare() {
    return this;
  }
  
  public Set<String> supportedOrderByColumns() {
    return Collections.emptySet();
  }
}
