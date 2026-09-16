package io.fusionauth.api.json.guice;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.inversoft.json.JacksonModule;
import com.inversoft.json.ToString;
import io.fusionauth.api.domain.guice.CBORObjectMapperProvider;
import io.fusionauth.client.json.FusionAuthJacksonModule;
import java.util.Set;

public class FusionAuthJSONModule extends AbstractModule {
  protected void configure() {
    Multibinder multibinder = Multibinder.newSetBinder(binder(), Module.class);
    multibinder.addBinding().to(JacksonModule.class);
    multibinder.addBinding().to(FusionAuthJacksonModule.class);
    ToString.setJacksonModules(Set.of(new FusionAuthJacksonModule()));
    bind(CBORMapper.class).toProvider(CBORObjectMapperProvider.class).asEagerSingleton();
  }
}
