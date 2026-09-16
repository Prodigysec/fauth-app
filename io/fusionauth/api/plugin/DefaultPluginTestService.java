package io.fusionauth.api.plugin;

public class DefaultPluginTestService implements PluginTestService {
  public static boolean called = false;
  
  public void call() {
    called = true;
  }
}
