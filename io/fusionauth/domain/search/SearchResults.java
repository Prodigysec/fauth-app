package io.fusionauth.domain.search;

import java.util.List;

public class SearchResults<T> {
  public String nextResults;
  
  public List<T> results;
  
  public long total;
  
  public boolean totalEqualToActual;
  
  public SearchResults() {}
  
  public SearchResults(List<T> paramList, long paramLong) {
    this.results = paramList;
    this.total = paramLong;
  }
  
  public SearchResults(List<T> paramList, long paramLong, String paramString) {
    this.results = paramList;
    this.total = paramLong;
    this.nextResults = paramString;
  }
}
