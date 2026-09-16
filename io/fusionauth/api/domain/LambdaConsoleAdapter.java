package io.fusionauth.api.domain;

public interface LambdaConsoleAdapter {
  String cutDebugLog();
  
  String cutErrorLog();
  
  String cutInfoLog();
}
