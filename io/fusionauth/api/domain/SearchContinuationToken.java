package io.fusionauth.api.domain;

import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.search.SortField;
import java.util.List;

public class SearchContinuationToken implements Buildable<SearchContinuationToken> {
  public List<String> ls;
  
  public String pit;
  
  public String q;
  
  public String qs;
  
  public List<SortField> sf;
}
