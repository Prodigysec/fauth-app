package io.fusionauth.api.domain.guice.mybatis;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.ibatis.session.SqlSessionFactory;

public class DelegateSqlSessionFactoryProvider implements Provider<SqlSessionFactory> {
  private final SqlSessionFactory sqlSessionFactory;
  
  @Inject
  public DelegateSqlSessionFactoryProvider(SqlSessionFactory paramSqlSessionFactory) {
    this.sqlSessionFactory = paramSqlSessionFactory;
  }
  
  public SqlSessionFactory get() {
    return this.sqlSessionFactory;
  }
}
