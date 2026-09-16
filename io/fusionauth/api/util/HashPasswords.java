package io.fusionauth.api.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.fusionauth.api.security.BCryptPasswordEncryptor;
import io.fusionauth.api.security.PBKDF2HMACSHA256PasswordEncryptor;
import io.fusionauth.api.security.PBKDF2HMACSHA256WithKeyLength512PasswordEncryptor;
import io.fusionauth.api.security.PBKDF2HMACSHA512PasswordEncryptor;
import io.fusionauth.api.security.PortablePHPMD5PasswordEncryptor;
import io.fusionauth.api.security.PortablePHPSHA512PasswordEncryptor;
import io.fusionauth.api.security.SaltedHMACSHA256PasswordEncryptor;
import io.fusionauth.api.security.SaltedMD5PasswordEncryptor;
import io.fusionauth.api.security.SaltedSHA256PasswordEncryptor;
import io.fusionauth.plugin.spi.security.PasswordEncryptor;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class HashPasswords {
  private static final ObjectMapper OBJECT_MAPPER = (new ObjectMapper()).setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
    .configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true);
  
  private static final Function<String, ThreadFactory> ThreadFactory = paramString -> ();
  
  public static void main(String... paramVarArgs) {
    HashPasswords hashPasswords = new HashPasswords();
    if (paramVarArgs.length > 0 && paramVarArgs[0].equals("dev-setup")) {
      hashPasswords.devSetup(paramVarArgs);
      return;
    } 
    hashPasswords.doIt(paramVarArgs);
  }
  
  private void devSetup(String... paramVarArgs) {
    System.out.println("Initialize an input file for testing.");
    PlainTextRequest plainTextRequest = new PlainTextRequest();
    int i = Integer.parseInt(paramVarArgs[1]);
    for (byte b = 0; b < i; b++) {
      PlainTextPassword plainTextPassword = new PlainTextPassword();
      plainTextPassword.id = UUID.randomUUID().toString();
      plainTextPassword.password = "password" + b;
      plainTextRequest.passwords.add(plainTextPassword);
    } 
    String str = (paramVarArgs.length == 3) ? paramVarArgs[2] : "/tmp/input.json";
    try {
      OBJECT_MAPPER.writeValue(new File(str), plainTextRequest);
    } catch (IOException iOException) {
      throw new RuntimeException(iOException);
    } 
  }
  
  private void doIt(String... paramVarArgs) {
    PlainTextRequest plainTextRequest;
    if (paramVarArgs.length < 2) {
      printUsage();
      return;
    } 
    File file1 = new File(paramVarArgs[0]);
    File file2 = new File(paramVarArgs[1]);
    try {
      plainTextRequest = (PlainTextRequest)OBJECT_MAPPER.readValue(file1, PlainTextRequest.class);
    } catch (Exception exception) {
      log("Failed to read the input file, verify it is formatted correctly.");
      throw new RuntimeException(exception);
    } 
    log("Hash Passwords");
    log("  Input : " + String.valueOf(file1));
    log("  Output : " + String.valueOf(file2));
    byte b = (paramVarArgs.length <= 2) ? 5 : Integer.parseInt(paramVarArgs[2]);
    log("  Thread Count : " + b);
    String str = (paramVarArgs.length <= 3) ? "salted-pbkdf2-hmac-sha256" : paramVarArgs[3];
    PasswordEncryptor passwordEncryptor = lookup(str);
    int i = (paramVarArgs.length <= 4) ? passwordEncryptor.defaultFactor() : Integer.parseInt(paramVarArgs[4]);
    plainTextRequest.passwords.removeIf(Objects::isNull);
    int j = plainTextRequest.passwords.size();
    log("  Encryption Scheme : " + str);
    log("  Encryption Factor : " + i);
    log("  Imported Plaintext Passwords : " + j);
    Instant instant = Instant.now();
    HashedResponse hashedResponse = new HashedResponse();
    AtomicInteger atomicInteger = new AtomicInteger(0);
    ArrayList<Runnable> arrayList = new ArrayList();
    ExecutorService executorService = Executors.newFixedThreadPool(b, ThreadFactory.apply("HashPasswords Worker"));
    int k = plainTextRequest.passwords.size() / b;
    int m;
    for (m = 0; m < j; ) {
      int n = Math.min(m + k, j);
      List<PlainTextPassword> list = plainTextRequest.passwords.subList(m, n);
      arrayList.add(() -> paramList.forEach(()));
      m += k;
    } 
    Objects.requireNonNull(executorService);
    arrayList.forEach(executorService::submit);
    executorService.shutdown();
    try {
      while (!executorService.awaitTermination(5L, TimeUnit.SECONDS)) {
        m = atomicInteger.get();
        if (m > 0)
          log("   - hashing password " + m + " of " + j); 
      } 
    } catch (InterruptedException interruptedException) {
      throw new RuntimeException(interruptedException);
    } 
    log("Completed - hashed " + atomicInteger.get() + " of " + j);
    log("Total Passwords Hashed : " + hashedResponse.users.size());
    Duration duration = Duration.between(instant, Instant.now());
    log("Completed in ~" + duration.toMinutes() + " minutes. (" + duration.toMillis() + " milliseconds).");
    log(" ~ " + duration.toMillis() / plainTextRequest.passwords.size() + " milliseconds per password.");
    writeOutput(file2, hashedResponse);
  }
  
  private HashedPassword hash(PlainTextPassword paramPlainTextPassword, PasswordEncryptor paramPasswordEncryptor, String paramString, int paramInt, AtomicInteger paramAtomicInteger) {
    try {
      HashedPassword hashedPassword = new HashedPassword();
      hashedPassword.id = paramPlainTextPassword.id;
      hashedPassword.encryptionScheme = paramString;
      hashedPassword.factor = paramInt;
      hashedPassword.salt = paramPasswordEncryptor.generateSalt();
      hashedPassword.password = paramPasswordEncryptor.encrypt(paramPlainTextPassword.password, hashedPassword.salt, paramInt);
      hashedPassword.passwordLastUpdateInstant = ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli();
      paramAtomicInteger.incrementAndGet();
      return hashedPassword;
    } catch (Exception exception) {
      log("\n -- Failed to hash password with Id: " + paramPlainTextPassword.id + ". Exception\n" + exception.getMessage());
      System.exit(1);
      return null;
    } 
  }
  
  private void log(String paramString) {
    System.out.println("[" + ZonedDateTime.now().toLocalDateTime().toString() + "] " + paramString);
  }
  
  private PasswordEncryptor lookup(String paramString) {
    switch (paramString) {
      case "salted-pbkdf2-hmac-sha256":
        return new PBKDF2HMACSHA256PasswordEncryptor();
      case "salted-pbkdf2-hmac-sha256-512":
        return new PBKDF2HMACSHA256WithKeyLength512PasswordEncryptor();
      case "salted-pbkdf2-hmac-sha512-512":
        return new PBKDF2HMACSHA512PasswordEncryptor();
      case "bcrypt":
        return new BCryptPasswordEncryptor();
      case "salted-sha256":
        return new SaltedSHA256PasswordEncryptor();
      case "salted-md5":
        return new SaltedMD5PasswordEncryptor();
      case "salted-hmac-sha256":
        return new SaltedHMACSHA256PasswordEncryptor();
      case "phpass-md5":
        return new PortablePHPMD5PasswordEncryptor();
      case "phpass-sha512":
        return new PortablePHPSHA512PasswordEncryptor();
    } 
    log("Invalid encryption scheme [" + paramString + "]");
    System.exit(1);
    return null;
  }
  
  private void printUsage() {
    System.out.println("Usage: HashPasswords <input_file> <output_file> [threads] [scheme] [factor]");
    System.out.println("  Parameters are ordered. To use an optional parameter, all previous parameters must be specified.\n");
    System.out.println("  <input_file> (Required)\n\tThe path to the input file.\n");
    System.out.println("  <output_file> (Required)\n\tThe path used to write the output.\n");
    System.out.println("  [threads] (Optional)\n\tIf threads is omitted not provided, the default will be used. [5]");
    System.out.println();
    System.out.println("  [scheme] (Optional)\n\tIf scheme is omitted the default scheme will be used. [salted-pbkdf2-hmac-sha256]\n\tPossible values:");
    System.out.println("\t\tsalted-md5");
    System.out.println("\t\tsalted-sha256");
    System.out.println("\t\tsalted-hmac-sha256");
    System.out.println("\t\tsalted-pbkdf2-hmac-sha256");
    System.out.println("\t\tsalted-pbkdf2-hmac-sha256-512");
    System.out.println("\t\tsalted-pbkdf2-hmac-sha512-512");
    System.out.println("\t\tphpass-md5");
    System.out.println("\t\tphpass-sha512");
    System.out.println("\t\tbcrypt");
    System.out.println();
    System.out.println("  [factor] (Optional)\n\tIf factor is omitted the default factor defined by the encryptor will be used.");
    System.out.println("\nExample Linux Usage: \n");
    System.out.println("  $ cd /usr/local/fusionauth/");
    System.out.println("  $ ./java/current/bin/java -cp \"./lib/*\" io.fusionauth.api.util.HashPasswords /tmp/input.json /tmp/output.json 5 salted-pbkdf2-hmac-sha256 24000\n");
    System.out.println("\nExample Windows Usage: \n");
    System.out.println("  > cd \\fusionauth\\");
    System.out.println("  > java\\current\\bin\\java -cp \"lib\\*\" io.fusionauth.api.util.HashPasswords \\input.json \\output.json 5 salted-pbkdf2-hmac-sha256 24000\n");
  }
  
  private void writeOutput(File paramFile, HashedResponse paramHashedResponse) {
    try {
      OBJECT_MAPPER.writeValue(paramFile, paramHashedResponse);
    } catch (IOException iOException) {
      log("Failed to write output file.");
      throw new RuntimeException(iOException);
    } 
  }
  
  private static class HashedPassword {
    public String encryptionScheme;
    
    public int factor;
    
    public String id;
    
    public String password;
    
    public long passwordLastUpdateInstant;
    
    public String salt;
  }
  
  private static class HashedResponse {
    public Queue<HashPasswords.HashedPassword> users = new ConcurrentLinkedQueue<>();
  }
  
  private static class PlainTextPassword {
    public String id;
    
    public String password;
  }
  
  private static class PlainTextRequest {
    public List<HashPasswords.PlainTextPassword> passwords = new ArrayList<>();
  }
}
