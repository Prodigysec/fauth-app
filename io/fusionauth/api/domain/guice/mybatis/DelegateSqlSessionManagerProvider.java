package io.fusionauth.api.domain.guice.mybatis;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.ibatis.session.SqlSessionManager;
import org.mybatis.guice.session.SqlSessionManagerProvider;

public class DelegateSqlSessionManagerProvider implements Provider<SqlSessionManager> {
  private final SqlSessionManagerProvider sqlSessionManagerProvider;
  
  @Inject
  public DelegateSqlSessionManagerProvider(SqlSessionManagerProvider paramSqlSessionManagerProvider) {
    this.sqlSessionManagerProvider = paramSqlSessionManagerProvider;
  }
  
  public SqlSessionManager get() {
    return this.sqlSessionManagerProvider.get();
  }
}
