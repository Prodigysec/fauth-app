package io.fusionauth.api.domain.guice.mybatis;

import com.google.inject.Inject;
import com.google.inject.Provider;
import javax.sql.DataSource;

public class DelegateDatasourceProvider implements Provider<DataSource> {
  private final DataSource dataSource;
  
  @Inject
  public DelegateDatasourceProvider(DataSource paramDataSource) {
    this.dataSource = paramDataSource;
  }
  
  public DataSource get() {
    return this.dataSource;
  }
}
