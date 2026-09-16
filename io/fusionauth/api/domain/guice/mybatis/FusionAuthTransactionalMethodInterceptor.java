package io.fusionauth.api.domain.guice.mybatis;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.mybatis.guice.transactional.Transactional;

public class FusionAuthTransactionalMethodInterceptor implements MethodInterceptor {
  private static final Class<?>[] CAUSE_TYPES = new Class[] { Throwable.class };
  
  private static final Class<?>[] MESSAGE_CAUSE_TYPES = new Class[] { String.class, Throwable.class };
  
  private final Log log = LogFactory.getLog(getClass());
  
  private final Map<String, SqlSessionManager> sqlSessionManagers = new HashMap<>(3);
  
  private static <E extends Throwable> Constructor<E> getMatchingConstructor(Class<E> paramClass, Class<?>[] paramArrayOfClass) {
    Class<E> clazz = paramClass;
    while (Object.class != clazz) {
      for (Constructor<E> constructor : clazz.getConstructors()) {
        if (Arrays.equals((Object[])paramArrayOfClass, (Object[])constructor.getParameterTypes()))
          return constructor; 
      } 
      clazz = (Class)clazz.getSuperclass();
    } 
    return null;
  }
  
  public Object invoke(MethodInvocation paramMethodInvocation) throws Throwable {
    Method method = paramMethodInvocation.getMethod();
    Transactional transactional = method.<Transactional>getAnnotation(Transactional.class);
    UseDataSource useDataSource = method.<UseDataSource>getAnnotation(UseDataSource.class);
    SqlSessionManager sqlSessionManager = (useDataSource == null) ? this.sqlSessionManagers.get(DataSourceName.primary.name()) : this.sqlSessionManagers.get(useDataSource.value());
    if (transactional == null)
      transactional = method.getDeclaringClass().<Transactional>getAnnotation(Transactional.class); 
    String str = null;
    if (this.log.isDebugEnabled())
      str = String.format("[Intercepted method: %s]", new Object[] { method.toGenericString() }); 
    boolean bool1 = sqlSessionManager.isManagedSessionStarted();
    if (bool1) {
      if (this.log.isDebugEnabled())
        this.log.debug(String.format("%s - SqlSession already set for thread: %s", new Object[] { str, Long.valueOf(Thread.currentThread().getId()) })); 
    } else {
      if (this.log.isDebugEnabled())
        this.log.debug(
            String.format("%s - SqlSession not set for thread: %s, creating a new one", new Object[] { str, Long.valueOf(Thread.currentThread().getId()) })); 
      sqlSessionManager.startManagedSession(transactional.executorType(), transactional
          .isolation().getTransactionIsolationLevel());
    } 
    Object object = null;
    boolean bool2 = transactional.rollbackOnly();
    try {
      object = paramMethodInvocation.proceed();
    } catch (Throwable throwable) {
      bool2 = true;
      throw convertThrowableIfNeeded(paramMethodInvocation, transactional, throwable);
    } finally {
      if (!bool1) {
        try {
          if (bool2) {
            if (this.log.isDebugEnabled())
              this.log.debug(str + " - SqlSession of thread: " + str + " rolling back"); 
            sqlSessionManager.rollback(true);
          } else {
            if (this.log.isDebugEnabled())
              this.log.debug(str + " - SqlSession of thread: " + str + " committing"); 
            sqlSessionManager.commit(transactional.force());
          } 
        } finally {
          if (this.log.isDebugEnabled())
            this.log.debug(String.format("%s - SqlSession of thread: %s terminated its life-cycle, closing it", new Object[] { str, 
                    Long.valueOf(Thread.currentThread().getId()) })); 
          sqlSessionManager.close();
        } 
      } else if (this.log.isDebugEnabled()) {
        this.log.debug(String.format("%s - SqlSession of thread: %s is inherited, skipped close operation", new Object[] { str, 
                Long.valueOf(Thread.currentThread().getId()) }));
      } 
    } 
    return object;
  }
  
  @Inject
  public void setBackgroundSqlSessionManager(@Named("background") SqlSessionManager paramSqlSessionManager) {
    this.sqlSessionManagers.put(DataSourceName.background.name(), paramSqlSessionManager);
  }
  
  @Inject
  public void setPrimarySqlSessionManager(@Named("primary") SqlSessionManager paramSqlSessionManager) {
    this.sqlSessionManagers.put(DataSourceName.primary.name(), paramSqlSessionManager);
  }
  
  @Inject
  public void setSecondarySqlSessionManager(@Named("secondary") SqlSessionManager paramSqlSessionManager) {
    this.sqlSessionManagers.put(DataSourceName.secondary.name(), paramSqlSessionManager);
  }
  
  private Throwable convertThrowableIfNeeded(MethodInvocation paramMethodInvocation, Transactional paramTransactional, Throwable paramThrowable) {
    Object[] arrayOfObject;
    Class<?>[] arrayOfClass;
    Method method = paramMethodInvocation.getMethod();
    for (Class<?> clazz : method.getExceptionTypes()) {
      if (clazz.isAssignableFrom(paramThrowable.getClass()))
        return paramThrowable; 
    } 
    if (paramTransactional.rethrowExceptionsAs().isAssignableFrom(paramThrowable.getClass()))
      return paramThrowable; 
    if (paramTransactional.exceptionMessage().length() != 0) {
      String str = String.format(paramTransactional.exceptionMessage(), paramMethodInvocation.getArguments());
      arrayOfObject = new Object[] { str, paramThrowable };
      arrayOfClass = MESSAGE_CAUSE_TYPES;
    } else {
      arrayOfObject = new Object[] { paramThrowable };
      arrayOfClass = CAUSE_TYPES;
    } 
    Constructor<Throwable> constructor = getMatchingConstructor(paramTransactional.rethrowExceptionsAs(), arrayOfClass);
    Throwable throwable = null;
    if (constructor != null) {
      try {
        throwable = constructor.newInstance(arrayOfObject);
      } catch (Exception exception) {
        String str = String.format("Impossible to re-throw '%s', it needs the constructor with %s argument(s).", new Object[] { paramTransactional
              .rethrowExceptionsAs().getName(), Arrays.toString((Object[])arrayOfClass) });
        this.log.error(str, exception);
        throwable = new RuntimeException(str, exception);
      } 
    } else {
      String str = String.format("Impossible to re-throw '%s', it needs the constructor with %s or %s argument(s).", new Object[] { paramTransactional
            .rethrowExceptionsAs().getName(), Arrays.toString((Object[])CAUSE_TYPES), 
            Arrays.toString((Object[])MESSAGE_CAUSE_TYPES) });
      this.log.error(str);
      throwable = new RuntimeException(str);
    } 
    return throwable;
  }
}
