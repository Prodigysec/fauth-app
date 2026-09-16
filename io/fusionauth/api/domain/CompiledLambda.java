package io.fusionauth.api.domain;

import com.inversoft.cache.CloseableReentrantReadWriteLock;
import io.fusionauth.domain.Lambda;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

public class CompiledLambda extends Lambda implements Closeable {
  public static final int MAX_CONTEXT_POOL_SIZE = 100;
  
  public final List<LockableExecutionContext> contexts = new ArrayList<>();
  
  public final Source source;
  
  private final CloseableReentrantReadWriteLock lock = new CloseableReentrantReadWriteLock();
  
  public CompiledLambda(Lambda paramLambda) {
    super(paramLambda);
    try {
      this.source = Source.newBuilder("js", paramLambda.body, paramLambda.type.getFunctionName()).build();
    } catch (IOException iOException) {
      throw new RuntimeException(iOException);
    } 
  }
  
  public void close() {
    ArrayList<LockableExecutionContext> arrayList;
    CloseableReentrantReadWriteLock.CloseableLock closeableLock = this.lock.openWrite();
    try {
      arrayList = new ArrayList<>(this.contexts);
      this.contexts.clear();
      if (closeableLock != null)
        closeableLock.close(); 
    } catch (Throwable throwable) {
      if (closeableLock != null)
        try {
          closeableLock.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
    Thread.startVirtualThread(() -> paramList.forEach(this::safeClose));
  }
  
  public LockableExecutionContext getContext(Function<CompiledLambda, LockableExecutionContext> paramFunction) {
    Optional<LockableExecutionContext> optional;
    CloseableReentrantReadWriteLock.CloseableLock closeableLock = this.lock.openRead();
    try {
      optional = this.contexts.stream().filter(LockableExecutionContext::tryLock).findFirst();
      if (closeableLock != null)
        closeableLock.close(); 
    } catch (Throwable throwable) {
      if (closeableLock != null)
        try {
          closeableLock.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
    return optional.orElseGet(() -> {
          LockableExecutionContext lockableExecutionContext = getNewContextAndLockIt(paramFunction);
          CloseableReentrantReadWriteLock.CloseableLock closeableLock = this.lock.openWrite();
          try {
            if (this.contexts.size() < 100)
              this.contexts.add(lockableExecutionContext); 
            if (closeableLock != null)
              closeableLock.close(); 
          } catch (Throwable throwable) {
            if (closeableLock != null)
              try {
                closeableLock.close();
              } catch (Throwable throwable1) {
                throwable.addSuppressed(throwable1);
              }  
            throw throwable;
          } 
          return lockableExecutionContext;
        });
  }
  
  private LockableExecutionContext getNewContextAndLockIt(Function<CompiledLambda, LockableExecutionContext> paramFunction) {
    LockableExecutionContext lockableExecutionContext = paramFunction.apply(this);
    lockableExecutionContext.lock();
    return lockableExecutionContext;
  }
  
  private void safeClose(LockableExecutionContext paramLockableExecutionContext) {
    try {
      paramLockableExecutionContext.lock();
      paramLockableExecutionContext.unlock();
      paramLockableExecutionContext.context.close();
    } catch (Throwable throwable) {}
  }
  
  public static final class ContextValues extends Record {
    private final Value parse;
    
    private final Value stringify;
    
    public ContextValues(Value param1Value1, Value param1Value2) {
      this.parse = param1Value1;
      this.stringify = param1Value2;
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/domain/CompiledLambda$ContextValues;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #117	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/domain/CompiledLambda$ContextValues;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #117	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/domain/CompiledLambda$ContextValues;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #117	-> 0
    }
    
    public Value parse() {
      return this.parse;
    }
    
    public Value stringify() {
      return this.stringify;
    }
  }
  
  public static class InvocationResult {
    public String debug;
    
    public String error;
    
    public Exception exception;
    
    public String info;
  }
  
  public static class LockableExecutionContext implements AutoCloseable {
    public final Context context;
    
    public final CompiledLambda.ContextValues values;
    
    private final ReentrantLock lock = new ReentrantLock();
    
    public LockableExecutionContext(Context param1Context, CompiledLambda.ContextValues param1ContextValues) {
      this.context = param1Context;
      this.values = param1ContextValues;
    }
    
    public void close() {
      unlock();
    }
    
    public void lock() {
      this.lock.lock();
    }
    
    public boolean tryLock() {
      return this.lock.tryLock();
    }
    
    public void unlock() {
      this.lock.unlock();
    }
  }
}
