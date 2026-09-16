package io.fusionauth.app.domain;

public final class FormStepInfo extends Record {
  private final String description;
  
  private final String resendMessage;
  
  public FormStepInfo(String paramString1, String paramString2) {
    this.description = paramString1;
    this.resendMessage = paramString2;
  }
  
  public final String toString() {
    // Byte code:
    //   0: aload_0
    //   1: <illegal opcode> toString : (Lio/fusionauth/app/domain/FormStepInfo;)Ljava/lang/String;
    //   6: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #13	-> 0
  }
  
  public final int hashCode() {
    // Byte code:
    //   0: aload_0
    //   1: <illegal opcode> hashCode : (Lio/fusionauth/app/domain/FormStepInfo;)I
    //   6: ireturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #13	-> 0
  }
  
  public final boolean equals(Object paramObject) {
    // Byte code:
    //   0: aload_0
    //   1: aload_1
    //   2: <illegal opcode> equals : (Lio/fusionauth/app/domain/FormStepInfo;Ljava/lang/Object;)Z
    //   7: ireturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #13	-> 0
  }
  
  public String description() {
    return this.description;
  }
  
  public String resendMessage() {
    return this.resendMessage;
  }
}
