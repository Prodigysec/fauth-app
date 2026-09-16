package io.fusionauth.domain.search;

import io.fusionauth.domain.Buildable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UserSearchCriteria extends BaseElasticSearchCriteria implements Buildable<UserSearchCriteria> {
  public static final Set<String> SortableFields = new LinkedHashSet<>(Arrays.asList(new String[] { 
          "birthDate", "email", "fullName", "id", "insertInstant", "lastLoginInstant", "login", "phoneNumber", "tenantId", "username", 
          "registrations.applicationId", "registrations.id", "registrations.insertInstant", "registrations.lastLoginInstant", "registrations.roles" }));
  
  public static final Set<String> DatabaseSortableFields;
  
  static {
    DatabaseSortableFields = (Set<String>)SortableFields.stream().filter(paramString -> !paramString.startsWith("registrations.")).collect(Collectors.toSet());
  }
}
