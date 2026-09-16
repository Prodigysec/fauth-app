package io.fusionauth.api.domain.api.hypr;

import com.inversoft.json.ToString;
import java.util.ArrayList;

public class HYPRStateList extends ArrayList<HYPRRequestState> {
  public HYPRRequestState last() {
    if (size() == 0)
      return null; 
    return get(size() - 1);
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
