package io.fusionauth.api.service.reactor;

public final class ActivateResult extends Record {
  private final boolean success;
  
  private final ActivateThread thread;
  
  public ActivateResult(boolean paramBoolean, ActivateThread paramActivateThread) {
    this.success = paramBoolean;
    this.thread = paramActivateThread;
  }
  
  public final String toString() {
    // Byte code:
    //   0: aload_0
    //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/reactor/ActivateResult;)Ljava/lang/String;
    //   6: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #11	-> 0
  }
  
  public final int hashCode() {
    // Byte code:
    //   0: aload_0
    //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/reactor/ActivateResult;)I
    //   6: ireturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #11	-> 0
  }
  
  public final boolean equals(Object paramObject) {
    // Byte code:
    //   0: aload_0
    //   1: aload_1
    //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/reactor/ActivateResult;Ljava/lang/Object;)Z
    //   7: ireturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #11	-> 0
  }
  
  public boolean success() {
    return this.success;
  }
  
  public ActivateThread thread() {
    return this.thread;
  }
}
