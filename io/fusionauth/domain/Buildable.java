package io.fusionauth.domain;

import java.util.function.Consumer;

public interface Buildable<T> {
  default T with(Consumer<T> paramConsumer) {
    paramConsumer.accept((T)this);
    return (T)this;
  }
}
