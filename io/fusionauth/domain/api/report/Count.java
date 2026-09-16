package io.fusionauth.domain.api.report;

public class Count {
  public int count;
  
  public int interval;
  
  public Count() {}
  
  public Count(int paramInt1, int paramInt2) {
    this.count = paramInt1;
    this.interval = paramInt2;
  }
}
