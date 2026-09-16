package io.fusionauth.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.usagestats.shared.domain.CurrentStats;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

public class SavedCurrentStats extends CurrentStats {
  public ZonedDateTime lastCheckedInstant;
  
  public SavedCurrentStats() {}
  
  public SavedCurrentStats(CurrentStats paramCurrentStats, ZonedDateTime paramZonedDateTime) {
    this.stats = paramCurrentStats.stats;
    this.lastModifiedInstant = paramCurrentStats.lastModifiedInstant;
    this.lastCheckedInstant = paramZonedDateTime;
  }
  
  public SavedCurrentStats(List<String> paramList, ZonedDateTime paramZonedDateTime1, ZonedDateTime paramZonedDateTime2) {
    this.stats = paramList;
    this.lastModifiedInstant = paramZonedDateTime2;
    this.lastCheckedInstant = paramZonedDateTime1;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    if (!super.equals(paramObject))
      return false; 
    SavedCurrentStats savedCurrentStats = (SavedCurrentStats)paramObject;
    return Objects.equals(this.lastCheckedInstant, savedCurrentStats.lastCheckedInstant);
  }
  
  @JsonIgnore
  public String getStatsAsString() {
    return this.stats.isEmpty() ? null : String.join(",", this.stats);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.lastCheckedInstant });
  }
}
