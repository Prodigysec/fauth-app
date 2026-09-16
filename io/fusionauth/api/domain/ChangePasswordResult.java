package io.fusionauth.api.domain;

import java.util.Map;

public class ChangePasswordResult {
  public String oneTimePassword;
  
  public Map<String, Object> state;
  
  public boolean success;
}
