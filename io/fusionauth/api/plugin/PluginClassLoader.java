package io.fusionauth.api.plugin;

import io.fusionauth.api.util.FileTools;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class PluginClassLoader extends URLClassLoader {
  public static final String PLUGIN_DIRECTORY = "fusionauth.plugin.directory";
  
  public PluginClassLoader(URL[] paramArrayOfURL, ClassLoader paramClassLoader) {
    super(paramArrayOfURL, paramClassLoader);
  }
  
  public static List<PluginClassLoader> makePluginClassLoader(ClassLoader paramClassLoader) {
    String str = System.getProperty("fusionauth.plugin.directory");
    if (str == null)
      throw new IllegalArgumentException("You must specify the location of the FusionAuth plugin directory using the System property passed to the JVM like this:\n\n  java -Dfusionauth.plugin.home=/usr/local/fusionauth/plugins"); 
    Path path = Paths.get(str, new String[0]);
    if (Files.notExists(path, new java.nio.file.LinkOption[0]))
      return Collections.emptyList(); 
    if (!Files.isDirectory(path, new java.nio.file.LinkOption[0]))
      throw new IllegalArgumentException("Invalid plugin directory [" + String.valueOf(path) + "]. It is a regular file rather than a directory"); 
    ArrayList arrayList = new ArrayList();
    try {
      Stream<Path> stream = Files.list(path);
      try {
        stream.filter(Files::isReadable).forEach(paramPath -> {
              if (Files.isRegularFile(paramPath, new java.nio.file.LinkOption[0])) {
                if (paramPath.toAbsolutePath().toString().endsWith(".jar"))
                  paramList.add(List.of(paramPath)); 
              } else if (Files.isDirectory(paramPath, new java.nio.file.LinkOption[0])) {
                try {
                  paramList.add(FileTools.findFilesByPattern(paramPath, ".*\\.jar$", 42));
                } catch (IOException iOException) {}
              } 
            });
        ArrayList<PluginClassLoader> arrayList1 = new ArrayList();
        for (List list : arrayList)
          arrayList1.add(new PluginClassLoader((URL[])list.stream().map(PluginClassLoader::toURL).toArray(paramInt -> new URL[paramInt]), paramClassLoader)); 
        ArrayList<PluginClassLoader> arrayList2 = arrayList1;
        if (stream != null)
          stream.close(); 
        return arrayList2;
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
