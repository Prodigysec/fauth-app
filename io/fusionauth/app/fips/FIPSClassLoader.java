package io.fusionauth.app.fips;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FIPSClassLoader extends URLClassLoader {
  public FIPSClassLoader(URL[] paramArrayOfURL, ClassLoader paramClassLoader) {
    super(paramArrayOfURL, paramClassLoader);
  }
  
  public static FIPSClassLoader makeFIPSClassLoader(Path paramPath, ClassLoader paramClassLoader) {
    if (Files.notExists(paramPath, new java.nio.file.LinkOption[0]))
      return null; 
    if (!Files.isDirectory(paramPath, new java.nio.file.LinkOption[0]))
      throw new IllegalArgumentException("Invalid FIPS library directory [" + String.valueOf(paramPath) + "]. It is a regular file rather than a directory"); 
    ArrayList arrayList = new ArrayList();
    try {
      Stream<Path> stream = Files.list(paramPath);
      try {
        stream.filter(Files::isReadable).forEach(paramPath -> {
              if (Files.isRegularFile(paramPath, new java.nio.file.LinkOption[0]) && paramPath.toAbsolutePath().toString().endsWith(".jar"))
                paramList.add(toURL(paramPath)); 
            });
        FIPSClassLoader fIPSClassLoader = new FIPSClassLoader((URL[])arrayList.toArray(paramInt -> new URL[paramInt]), paramClassLoader);
        if (stream != null)
          stream.close(); 
        return fIPSClassLoader;
      } catch (Throwable throwable) {
        if (stream != null)
          try {
            stream.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (IOException iOException) {
      throw new IllegalArgumentException(iOException);
    } 
  }
  
  private static URL toURL(Path paramPath) {
    try {
      return paramPath.toRealPath(new java.nio.file.LinkOption[0]).toUri().toURL();
    } catch (IOException iOException) {
      throw new RuntimeException(iOException);
    } 
  }
}
