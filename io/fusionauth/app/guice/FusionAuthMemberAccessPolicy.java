package io.fusionauth.app.guice;

import freemarker.ext.beans.BlacklistMemberAccessPolicy;
import freemarker.ext.beans.ClassMemberAccessPolicy;
import freemarker.ext.beans.DefaultMemberAccessPolicy;
import freemarker.ext.beans.MemberAccessPolicy;
import freemarker.ext.beans.MemberSelectorListMemberAccessPolicy;
import freemarker.ext.beans.WhitelistMemberAccessPolicy;
import freemarker.template.Version;
import freemarker.template._TemplateAPI;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FusionAuthMemberAccessPolicy implements MemberAccessPolicy {
  private static final FusionAuthMemberAccessPolicy INSTANCE = new FusionAuthMemberAccessPolicy();
  
  private final BlacklistMemberAccessPolicy blacklistMemberAccessPolicy;
  
  private final boolean toStringAlwaysExposed;
  
  private final WhitelistMemberAccessPolicy whitelistMemberAccessPolicy;
  
  private final Set<Class<?>> whitelistRuleFinalClasses;
  
  private final Set<Class<?>> whitelistRuleNonFinalClasses;
  
  private FusionAuthMemberAccessPolicy() {
    try {
      ClassLoader classLoader = DefaultMemberAccessPolicy.class.getClassLoader();
      this.whitelistRuleFinalClasses = new HashSet<>();
      this.whitelistRuleNonFinalClasses = new HashSet<>();
      HashSet<Class<?>> hashSet = new HashSet();
      ArrayList<MemberSelectorListMemberAccessPolicy.MemberSelector> arrayList1 = new ArrayList();
      for (String str : loadMemberSelectorFileLines()) {
        str = str.trim();
        if (!MemberSelectorListMemberAccessPolicy.MemberSelector.isIgnoredLine(str)) {
          MemberSelectorListMemberAccessPolicy.MemberSelector memberSelector;
          if (str.startsWith("@")) {
            Class<?> clazz;
            memberSelector = (MemberSelectorListMemberAccessPolicy.MemberSelector)str.split("\\s+");
            if (memberSelector.length != 2)
              throw new IllegalStateException("Malformed @ line: " + str); 
            String str1 = memberSelector[1];
            try {
              clazz = classLoader.loadClass(str1);
            } catch (ClassNotFoundException classNotFoundException) {
              clazz = null;
            } 
            String str2 = memberSelector[0].substring(1);
            if (str2.equals("whitelistPolicyIfAssignable")) {
              if (clazz != null) {
                Set<Class<?>> set = ((clazz.getModifiers() & 0x10) != 0) ? this.whitelistRuleFinalClasses : this.whitelistRuleNonFinalClasses;
                set.add(clazz);
              } 
              continue;
            } 
            if (str2.equals("blacklistUnlistedMembers")) {
              if (clazz != null)
                hashSet.add(clazz); 
              continue;
            } 
            throw new IllegalStateException("Unhandled rule: " + str2);
          } 
          try {
            memberSelector = MemberSelectorListMemberAccessPolicy.MemberSelector.parse(str, classLoader);
          } catch (ClassNotFoundException|NoSuchMethodException|NoSuchFieldException classNotFoundException) {
            memberSelector = null;
          } 
          if (memberSelector != null) {
            Class clazz = memberSelector.getUpperBoundType();
            if (clazz != null) {
              if (!this.whitelistRuleFinalClasses.contains(clazz) && 
                !this.whitelistRuleNonFinalClasses.contains(clazz) && 
                !hashSet.contains(clazz))
                throw new IllegalStateException("Type without rule: " + clazz.getName()); 
              arrayList1.add(memberSelector);
            } 
          } 
        } 
      } 
      this.whitelistMemberAccessPolicy = new WhitelistMemberAccessPolicy(arrayList1);
      ArrayList<MemberSelectorListMemberAccessPolicy.MemberSelector> arrayList2 = new ArrayList();
      for (Class<?> clazz : hashSet) {
        ClassMemberAccessPolicy classMemberAccessPolicy = this.whitelistMemberAccessPolicy.forClass(clazz);
        for (Method method : clazz.getMethods()) {
          if (!classMemberAccessPolicy.isMethodExposed(method))
            arrayList2.add(new MemberSelectorListMemberAccessPolicy.MemberSelector(clazz, method)); 
        } 
        for (Constructor<?> constructor : clazz.getConstructors()) {
          if (!classMemberAccessPolicy.isConstructorExposed(constructor))
            arrayList2.add(new MemberSelectorListMemberAccessPolicy.MemberSelector(clazz, constructor)); 
        } 
        for (Field field : clazz.getFields()) {
          if (!classMemberAccessPolicy.isFieldExposed(field))
            arrayList2.add(new MemberSelectorListMemberAccessPolicy.MemberSelector(clazz, field)); 
        } 
      } 
      this.blacklistMemberAccessPolicy = new BlacklistMemberAccessPolicy(arrayList2);
      this
        
        .toStringAlwaysExposed = (this.whitelistMemberAccessPolicy.isToStringAlwaysExposed() && this.blacklistMemberAccessPolicy.isToStringAlwaysExposed());
    } catch (Exception exception) {
      throw new IllegalStateException("Couldn't init " + getClass().getName() + " instance", exception);
    } 
  }
  
  public static FusionAuthMemberAccessPolicy getInstance(Version paramVersion) {
    _TemplateAPI.checkVersionNotNullAndSupported(paramVersion);
    return INSTANCE;
  }
  
  private static List<String> loadMemberSelectorFileLines() throws IOException {
    ArrayList<String> arrayList = new ArrayList();
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(FusionAuthMemberAccessPolicy.class.getResourceAsStream("/freemarker/FusionAuthMemberAccessPolicy-rules"), "UTF-8"));
    try {
      String str;
      while ((str = bufferedReader.readLine()) != null)
        arrayList.add(str); 
      bufferedReader.close();
    } catch (Throwable throwable) {
      try {
        bufferedReader.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
    return arrayList;
  }
  
  public ClassMemberAccessPolicy forClass(Class<?> paramClass) {
    if (isTypeWithWhitelistRule(paramClass))
      return this.whitelistMemberAccessPolicy.forClass(paramClass); 
    return this.blacklistMemberAccessPolicy.forClass(paramClass);
  }
  
  public boolean isToStringAlwaysExposed() {
    return this.toStringAlwaysExposed;
  }
  
  private boolean isTypeWithWhitelistRule(Class<?> paramClass) {
    if (this.whitelistRuleFinalClasses.contains(paramClass))
      return true; 
    for (Class<?> clazz : this.whitelistRuleNonFinalClasses) {
      if (clazz.isAssignableFrom(paramClass))
        return true; 
    } 
    return false;
  }
}
