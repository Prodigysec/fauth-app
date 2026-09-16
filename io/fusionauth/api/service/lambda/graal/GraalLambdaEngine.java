package io.fusionauth.api.service.lambda.graal;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.fusionauth.api.domain.CompiledLambda;
import io.fusionauth.api.domain.api.service.ImmutableLambdaArgument;
import io.fusionauth.api.domain.api.service.LambdaArgument;
import io.fusionauth.api.service.lambda.LambdaEngine;
import io.fusionauth.api.service.lambda.LambdaScriptException;
import io.fusionauth.api.service.lambda.graal.domain.GraalContextObject;
import io.fusionauth.api.service.lambda.graal.domain.GraalServicesObject;
import io.fusionauth.api.service.lambda.graal.domain.Headers;
import io.fusionauth.api.service.lambda.graal.domain.LambdaConsole;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.api.service.system.KeyReaderService;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.util.ArrayList;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.Value;

public class GraalLambdaEngine implements LambdaEngine {
  private final Engine engine;
  
  private final KeyReaderService keyReader;
  
  private final MetricRegistry metricRegistry;
  
  private final ObjectMapper objectMapper;
  
  private final ReactorStatusService reactorStatusService;
  
  @Inject
  public GraalLambdaEngine(@Named("LambdaObjectMapper") ObjectMapper paramObjectMapper, ReactorStatusService paramReactorStatusService, MetricRegistry paramMetricRegistry, Engine paramEngine, KeyReaderService paramKeyReaderService) {
    this.metricRegistry = paramMetricRegistry;
    this.objectMapper = paramObjectMapper;
    this.reactorStatusService = paramReactorStatusService;
    this.engine = paramEngine;
    this.keyReader = paramKeyReaderService;
  }
  
  public CompiledLambda.InvocationResult invoke(CompiledLambda paramCompiledLambda, LambdaArgument... paramVarArgs) {
    CompiledLambda.LockableExecutionContext lockableExecutionContext = paramCompiledLambda.getContext(this::buildContext);
    try {
      CompiledLambda.InvocationResult invocationResult1 = executeLambda(lockableExecutionContext, paramCompiledLambda, paramVarArgs);
      if (!paramCompiledLambda.contexts.contains(lockableExecutionContext))
        lockableExecutionContext.context.close(); 
      CompiledLambda.InvocationResult invocationResult2 = invocationResult1;
      if (lockableExecutionContext != null)
        lockableExecutionContext.close(); 
      return invocationResult2;
    } catch (Throwable throwable) {
      if (lockableExecutionContext != null)
        try {
          lockableExecutionContext.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  private LambdaConsole addInvocationMembers(Value paramValue, CompiledLambda paramCompiledLambda) {
    LambdaConsole lambdaConsole = new LambdaConsole(paramCompiledLambda.debug);
    boolean bool = ReactorStatusValidator.isLicensedFor(this.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.advancedLambdas);
    paramValue.putMember("console", lambdaConsole);
    paramValue.putMember("fetch", new Fetch(lambdaConsole, paramCompiledLambda.id, paramCompiledLambda.name, bool, this.metricRegistry));
    paramValue.putMember("FusionAuth", new FusionAuthFunctions());
    paramValue.putMember("Headers", new Headers());
    return lambdaConsole;
  }
  
  private CompiledLambda.LockableExecutionContext buildContext(CompiledLambda paramCompiledLambda) {
    Context context = Context.newBuilder(new String[0]).engine(this.engine).allowAllAccess(false).allowPolyglotAccess(PolyglotAccess.NONE).allowHostAccess(HostAccess.NONE).allowExperimentalOptions(true).option("js.console", "false").build();
    Value value1 = context.eval("js", "JSON.parse");
    Value value2 = context.eval("js", "JSON.stringify");
    addInvocationMembers(context.getBindings("js"), paramCompiledLambda);
    try {
      context.eval(paramCompiledLambda.source);
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    } 
    CompiledLambda.ContextValues contextValues = new CompiledLambda.ContextValues(value1, value2);
    return new CompiledLambda.LockableExecutionContext(context, contextValues);
  }
  
  private CompiledLambda.InvocationResult executeLambda(CompiledLambda.LockableExecutionContext paramLockableExecutionContext, CompiledLambda paramCompiledLambda, LambdaArgument... paramVarArgs) {
    Value value1 = paramLockableExecutionContext.context.getBindings("js");
    LambdaConsole lambdaConsole = addInvocationMembers(value1, paramCompiledLambda);
    Value value2 = paramLockableExecutionContext.values.parse();
    Value value3 = paramLockableExecutionContext.values.stringify();
    CompiledLambda.InvocationResult invocationResult = new CompiledLambda.InvocationResult();
    try {
      String str = paramCompiledLambda.type.getFunctionName();
      if (!value1.hasMember(str))
        throw new NoSuchMethodException("No such function " + str); 
      ArrayList<Value> arrayList = new ArrayList();
      boolean bool = false;
      for (LambdaArgument lambdaArgument : paramVarArgs) {
        Value value = (lambdaArgument.object != null) ? value2.execute(new Object[] { this.objectMapper.writeValueAsString(lambdaArgument.object) }) : null;
        if (value != null && lambdaArgument instanceof ImmutableLambdaArgument) {
          ImmutableLambdaArgument immutableLambdaArgument = (ImmutableLambdaArgument)lambdaArgument;
          if (immutableLambdaArgument.context) {
            bool = true;
            value.putMember("services", new GraalServicesObject(this.keyReader));
          } 
        } 
        arrayList.add(value);
      } 
      if (!bool)
        arrayList.add(new GraalContextObject(this.keyReader)); 
      value1.getMember(str).executeVoid(arrayList.toArray());
      for (byte b = 0; b < paramVarArgs.length; b++) {
        LambdaArgument lambdaArgument = paramVarArgs[b];
        Value value = arrayList.get(b);
        if (lambdaArgument.mutable && value != null) {
          String str1 = value3.execute(new Object[] { value }).asString();
          this.objectMapper.readerForUpdating(lambdaArgument.object).readValue(str1);
        } 
      } 
    } catch (NoSuchMethodException noSuchMethodException) {
      invocationResult.exception = noSuchMethodException;
    } catch (Exception exception) {
      invocationResult.exception = new LambdaScriptException(exception);
    } finally {
      invocationResult.info = lambdaConsole.cutInfoLog();
      invocationResult.error = lambdaConsole.cutErrorLog();
      invocationResult.debug = lambdaConsole.cutDebugLog();
    } 
    return invocationResult;
  }
}
