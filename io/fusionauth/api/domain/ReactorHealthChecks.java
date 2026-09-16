package io.fusionauth.api.domain;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ReactorHealthChecks implements Buildable<ReactorHealthChecks> {
  public static final int version = 4;
  
  @JsonAnySetter
  @JsonAnyGetter
  private final Map<String, Object> otherFeatures = new LinkedHashMap<>();
  
  public ReactorFeatureStatus breachedPasswordDetection = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus ipGeoLocation = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus ipReputation = ReactorFeatureStatus.UNKNOWN;
  
  public ReactorFeatureStatus userAgentReputation = ReactorFeatureStatus.UNKNOWN;
  
  @JsonProperty("currentVersion")
  private Integer currentVersion;
  
  public ReactorHealthChecks() {}
  
  public ReactorHealthChecks(ReactorHealthChecks paramReactorHealthChecks) {
    this.breachedPasswordDetection = paramReactorHealthChecks.breachedPasswordDetection;
    this.currentVersion = paramReactorHealthChecks.currentVersion;
    this.ipGeoLocation = paramReactorHealthChecks.ipGeoLocation;
    this.ipReputation = paramReactorHealthChecks.ipReputation;
    this.userAgentReputation = paramReactorHealthChecks.userAgentReputation;
    this.otherFeatures.putAll(paramReactorHealthChecks.otherFeatures);
  }
  
  public boolean equals(Object paramObject) {
    ReactorHealthChecks reactorHealthChecks;
    if (paramObject instanceof ReactorHealthChecks) {
      reactorHealthChecks = (ReactorHealthChecks)paramObject;
    } else {
      return false;
    } 
    return (this.breachedPasswordDetection == reactorHealthChecks.breachedPasswordDetection && this.ipGeoLocation == reactorHealthChecks.ipGeoLocation);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.breachedPasswordDetection, this.ipGeoLocation });
  }
  
  @JsonIgnore
  public boolean isOutOfDate() {
    return (this.currentVersion == null || this.currentVersion.intValue() != 4);
  }
  
  public String toString() {
    return ToString.toString(this);
  }
  
  public void updateWithHealth(ReactorStatus paramReactorStatus) {
    paramReactorStatus.breachedPasswordDetection = this.breachedPasswordDetection;
    paramReactorStatus.ipGeoLocation = this.ipGeoLocation;
    paramReactorStatus.ipReputation = this.ipReputation;
    paramReactorStatus.userAgentReputation = this.userAgentReputation;
  }
}
