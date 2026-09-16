package io.fusionauth.api.domain.api.service;

public class MutableLambdaArgument extends LambdaArgument {
  public MutableLambdaArgument(Object paramObject) {
    super(paramObject, true);
  }
}
