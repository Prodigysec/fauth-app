package io.fusionauth.app.domain;

public class Pagination {
  private final int results;
  
  private final int resultsPerPage;
  
  private final long startRow;
  
  private final long total;
  
  public int currentPage;
  
  public long firstResult;
  
  public long lastResult;
  
  public int numberOfPages;
  
  public Pagination(int paramInt1, int paramInt2, int paramInt3, long paramLong) {
    this.startRow = paramInt1;
    this.resultsPerPage = paramInt2;
    this.results = paramInt3;
    this.total = paramLong;
    initialize();
  }
  
  private void initialize() {
    this.numberOfPages = (int)this.total / this.resultsPerPage;
    int i = (int)this.total % this.resultsPerPage;
    if (i != 0)
      this.numberOfPages++; 
    if (this.numberOfPages <= 0)
      this.numberOfPages = 1; 
    this.currentPage = (int)this.startRow / this.resultsPerPage + 1;
    if (this.results > 0) {
      this.firstResult = ((this.currentPage - 1) * this.resultsPerPage + 1);
      this.lastResult = this.firstResult + this.results - 1L;
    } else {
      this.firstResult = 0L;
      this.lastResult = 0L;
    } 
  }
}
