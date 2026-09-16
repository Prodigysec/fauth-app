package io.fusionauth.api.security.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import io.fusionauth.api.service.connector.Connector;
import io.fusionauth.api.service.connector.ConnectorValidator;
import io.fusionauth.api.service.connector.FusionAuthConnector;
import io.fusionauth.api.service.connector.GenericConnector;
import io.fusionauth.api.service.connector.GenericConnectorValidator;
import io.fusionauth.api.service.connector.LDAPConnector;
import io.fusionauth.api.service.connector.LDAPConnectorValidator;
import io.fusionauth.domain.connector.ConnectorType;

public class ConnectorModule extends AbstractModule {
  protected void configure() {
    MapBinder mapBinder1 = MapBinder.newMapBinder(binder(), ConnectorType.class, Connector.class);
    mapBinder1.addBinding(ConnectorType.FusionAuth).to(FusionAuthConnector.class);
    mapBinder1.addBinding(ConnectorType.Generic).to(GenericConnector.class);
    mapBinder1.addBinding(ConnectorType.LDAP).to(LDAPConnector.class);
    MapBinder mapBinder2 = MapBinder.newMapBinder(binder(), ConnectorType.class, ConnectorValidator.class);
    mapBinder2.addBinding(ConnectorType.Generic).to(GenericConnectorValidator.class);
    mapBinder2.addBinding(ConnectorType.LDAP).to(LDAPConnectorValidator.class);
  }
}
