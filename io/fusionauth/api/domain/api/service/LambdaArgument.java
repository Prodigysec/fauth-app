package io.fusionauth.api.domain.api.service;

public abstract class LambdaArgument {
  public final boolean mutable;
  
  public final Object object;
  
  public LambdaArgument(Object paramObject, boolean paramBoolean) {
    this.object = paramObject;
    this.mutable = paramBoolean;
  }
}
