package io.fusionauth.api.domain.api.hypr;

import com.inversoft.json.ToString;
import java.util.ArrayList;

public class HYPRDeviceList extends ArrayList<HYPRDevice> {
  public String toString() {
    return ToString.toString(this);
  }
}
