package io.fusionauth.app.action.admin;

import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.domain.Pagination;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.search.BaseSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.util.ArrayList;
import java.util.List;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.parameter.annotation.PreParameterMethod;
import org.primeframework.mvc.scope.annotation.BrowserActionSession;

public abstract class BaseSearchAction<T, U extends BaseSearchCriteria> extends BaseAction {
  public boolean clear;
  
  public int currentPage;
  
  public long firstResult;
  
  public long lastResult;
  
  public int numberOfPages;
  
  public List<T> results;
  
  @BrowserActionSession
  public U s;
  
  public long total;
  
  protected BaseSearchAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  @PostParameterMethod
  public void clear() {
    if (this.clear)
      this.s = defaultSearchCriteria(); 
  }
  
  public String execute() {
    SearchResults<T> searchResults = search();
    if (searchResults == null)
      return "success"; 
    this.results = searchResults.results;
    if (this.results == null)
      this.results = new ArrayList<>(); 
    this.total = searchResults.total;
    calculatePagination();
    return "success";
  }
  
  @PreParameterMethod
  public void prepare() {
    if (this.s == null || this.clear)
      this.s = defaultSearchCriteria(); 
  }
  
  protected void calculatePagination() {
    boolean bool = (this.results == null) ? false : this.results.size();
    Pagination pagination = new Pagination(((BaseSearchCriteria)this.s).startRow, ((BaseSearchCriteria)this.s).numberOfResults, bool, this.total);
    this.numberOfPages = pagination.numberOfPages;
    this.currentPage = pagination.currentPage;
    this.firstResult = pagination.firstResult;
    this.lastResult = pagination.lastResult;
  }
  
  protected abstract U defaultSearchCriteria();
  
  protected abstract SearchResults<T> search();
}
