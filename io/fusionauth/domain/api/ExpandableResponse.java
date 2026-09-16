package io.fusionauth.domain.api;

import java.util.ArrayList;
import java.util.List;

public abstract class ExpandableResponse {
  public List<String> expandable = new ArrayList<>(0);
}
