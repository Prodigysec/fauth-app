package io.fusionauth.api.domain;

import java.util.List;
import java.util.UUID;

public class SearchEngineResult {
  public final List<UUID> ids;
  
  public final boolean totalEqualToActual;
  
  public final long totalNumberOfResults;
  
  public String nextSearchToken;
  
  public SearchEngineResult(List<UUID> paramList, boolean paramBoolean, long paramLong, String paramString) {
    this.ids = paramList;
    this.totalEqualToActual = paramBoolean;
    this.totalNumberOfResults = paramLong;
    this.nextSearchToken = paramString;
  }
}
