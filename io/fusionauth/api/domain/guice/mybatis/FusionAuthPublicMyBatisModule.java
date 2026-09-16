package io.fusionauth.api.domain.guice.mybatis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.inversoft.mybatis.JSONColumnService;
import io.fusionauth.api.domain.guice.DatabaseObjectMapperProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.aopalliance.intercept.MethodInterceptor;
import org.mybatis.guice.transactional.Transactional;

public class FusionAuthPublicMyBatisModule extends AbstractModule {
  protected void configure() {
    bind(ObjectMapper.class).annotatedWith((Annotation)Names.named("DatabaseObjectMapper")).toProvider(DatabaseObjectMapperProvider.class).asEagerSingleton();
    requestStaticInjection(new Class[] { JSONColumnService.class });
    FusionAuthTransactionalMethodInterceptor fusionAuthTransactionalMethodInterceptor = new FusionAuthTransactionalMethodInterceptor();
    requestInjection(fusionAuthTransactionalMethodInterceptor);
    bindInterceptor(Matchers.any(), Matchers.not(Method::isSynthetic).and(Matchers.not(paramMethod -> (paramMethod.getDeclaringClass() == Object.class)))
        .and(Matchers.annotatedWith(Transactional.class)), new MethodInterceptor[] { fusionAuthTransactionalMethodInterceptor });
    bindInterceptor(Matchers.annotatedWith(Transactional.class), 
        Matchers.not(Method::isSynthetic).and(Matchers.not(paramMethod -> (paramMethod.getDeclaringClass() == Object.class)))
        .and(Matchers.not(Matchers.annotatedWith(Transactional.class))), new MethodInterceptor[] { fusionAuthTransactionalMethodInterceptor });
  }
}
