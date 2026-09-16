package io.fusionauth.api.domain.api.service;

public class ImmutableLambdaArgument extends LambdaArgument {
  public final boolean context;
  
  public ImmutableLambdaArgument(Object paramObject) {
    this(paramObject, false);
  }
  
  public ImmutableLambdaArgument(Object paramObject, boolean paramBoolean) {
    super(paramObject, false);
    this.context = paramBoolean;
  }
}
