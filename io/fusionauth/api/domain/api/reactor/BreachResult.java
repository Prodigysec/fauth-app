package io.fusionauth.api.domain.api.reactor;

import io.fusionauth.domain.BreachedPasswordStatus;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.PasswordBreachDetection;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class BreachResult implements Buildable<BreachResult> {
  public ZonedDateTime breachDataLastModified;
  
  public List<String> loginIds = new ArrayList<>();
  
  public BreachedPasswordStatus match;
  
  public List<String> sources = new ArrayList<>();
  
  public BreachResult(BreachedPasswordStatus paramBreachedPasswordStatus) {
    this.match = paramBreachedPasswordStatus;
  }
  
  public BreachResult() {
    this.match = BreachedPasswordStatus.None;
  }
  
  public boolean isBreached(PasswordBreachDetection paramPasswordBreachDetection) {
    switch (paramPasswordBreachDetection.matchMode) {
      default:
        throw new MatchException(null, null);
      case High:
        return 
          (this.match == BreachedPasswordStatus.CommonPassword || this.match == BreachedPasswordStatus.PasswordOnly || this.match == BreachedPasswordStatus.SubAddressMatch || this.match == BreachedPasswordStatus.ExactMatch);
      case Medium:
        return (this.match == BreachedPasswordStatus.CommonPassword || this.match == BreachedPasswordStatus.SubAddressMatch || this.match == BreachedPasswordStatus.ExactMatch);
      case Low:
        break;
    } 
    return (this.match == BreachedPasswordStatus.CommonPassword || this.match == BreachedPasswordStatus.ExactMatch);
  }
}
