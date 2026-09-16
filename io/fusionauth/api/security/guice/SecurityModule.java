package io.fusionauth.api.security.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import io.fusionauth.api.security.BCryptPasswordEncryptor;
import io.fusionauth.api.security.DefaultPasswordEncryptorLibrary;
import io.fusionauth.api.security.PBKDF2HMACSHA256PasswordEncryptor;
import io.fusionauth.api.security.PBKDF2HMACSHA256WithKeyLength512PasswordEncryptor;
import io.fusionauth.api.security.PBKDF2HMACSHA512PasswordEncryptor;
import io.fusionauth.api.security.PasswordEncryptorLibrary;
import io.fusionauth.api.security.PortablePHPMD5PasswordEncryptor;
import io.fusionauth.api.security.PortablePHPSHA512PasswordEncryptor;
import io.fusionauth.api.security.SaltedHMACSHA256PasswordEncryptor;
import io.fusionauth.api.security.SaltedMD5PasswordEncryptor;
import io.fusionauth.api.security.SaltedSHA256PasswordEncryptor;
import io.fusionauth.plugin.spi.security.PasswordEncryptor;
import java.lang.annotation.Annotation;

public class SecurityModule extends AbstractModule {
  public static final String NULL_PASSWORD = "null-password";
  
  public static String Bcrypt = "bcrypt";
  
  protected void configure() {
    MapBinder mapBinder = MapBinder.newMapBinder(binder(), String.class, PasswordEncryptor.class);
    mapBinder.addBinding(Bcrypt).to(BCryptPasswordEncryptor.class);
    mapBinder.addBinding("salted-md5").to(SaltedMD5PasswordEncryptor.class);
    mapBinder.addBinding("salted-sha256").to(SaltedSHA256PasswordEncryptor.class);
    mapBinder.addBinding("salted-hmac-sha256").to(SaltedHMACSHA256PasswordEncryptor.class);
    mapBinder.addBinding("salted-pbkdf2-hmac-sha256").to(PBKDF2HMACSHA256PasswordEncryptor.class);
    mapBinder.addBinding("salted-pbkdf2-hmac-sha256-512").to(PBKDF2HMACSHA256WithKeyLength512PasswordEncryptor.class);
    mapBinder.addBinding("salted-pbkdf2-hmac-sha512-512").to(PBKDF2HMACSHA512PasswordEncryptor.class);
    mapBinder.addBinding("phpass-md5").to(PortablePHPMD5PasswordEncryptor.class);
    mapBinder.addBinding("phpass-sha512").to(PortablePHPSHA512PasswordEncryptor.class);
    mapBinder.addBinding("null-password").to(PBKDF2HMACSHA256PasswordEncryptor.class);
    bind(PasswordEncryptorLibrary.class).to(DefaultPasswordEncryptorLibrary.class).asEagerSingleton();
    bindConstant().annotatedWith((Annotation)Names.named("default-password-encryptor-name")).to("salted-pbkdf2-hmac-sha256");
  }
}
