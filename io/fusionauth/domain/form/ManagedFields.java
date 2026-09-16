package io.fusionauth.domain.form;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ManagedFields {
  public static final Map<String, FormField> Values;
  
  static {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("registration.preferredLanguages", (new FormField())
        .with(paramFormField -> paramFormField.key = "registration.preferredLanguages")
        .with(paramFormField -> paramFormField.confirm = false)
        .with(paramFormField -> paramFormField.control = FormControl.select)
        .with(paramFormField -> paramFormField.required = false)
        .with(paramFormField -> paramFormField.type = FormDataType.string)
        .with(paramFormField -> paramFormField.data.put("leftAddon", "info"))
        .with(paramFormField -> paramFormField.name = "[Admin Registration] preferred languages"));
    hashMap.put("registration.roles", (new FormField())
        .with(paramFormField -> paramFormField.key = "registration.roles")
        .with(paramFormField -> paramFormField.confirm = false)
        .with(paramFormField -> paramFormField.control = FormControl.checkbox)
        .with(paramFormField -> paramFormField.required = false)
        .with(paramFormField -> paramFormField.type = FormDataType.string)
        .with(paramFormField -> paramFormField.data.put("leftAddon", "info"))
        .with(paramFormField -> paramFormField.name = "[Admin Registration] roles"));
    hashMap.put("registration.timezone", (new FormField())
        .with(paramFormField -> paramFormField.key = "registration.timezone")
        .with(paramFormField -> paramFormField.confirm = false)
        .with(paramFormField -> paramFormField.control = FormControl.select)
        .with(paramFormField -> paramFormField.required = false)
        .with(paramFormField -> paramFormField.type = FormDataType.string)
        .with(paramFormField -> paramFormField.data.put("leftAddon", "info"))
        .with(paramFormField -> paramFormField.name = "[Admin Registration] timezone"));
    hashMap.put("registration.username", (new FormField())
        .with(paramFormField -> paramFormField.key = "registration.username")
        .with(paramFormField -> paramFormField.confirm = false)
        .with(paramFormField -> paramFormField.control = FormControl.text)
        .with(paramFormField -> paramFormField.required = false)
        .with(paramFormField -> paramFormField.type = FormDataType.string)
        .with(paramFormField -> paramFormField.data.put("leftAddon", "user"))
        .with(paramFormField -> paramFormField.name = "[Admin Registration] username"));
    hashMap.put("user.birthDate", (new FormField())
        .with(paramFormField -> paramFormField.key = "user.birthDate")
        .with(paramFormField -> paramFormField.confirm = false)
        .with(paramFormField -> paramFormField.control = FormControl.text)
        .with(paramFormField -> paramFormField.required = false)
        .with(paramFormField -> paramFormField.type = FormDataType.date)
        .with(paramFormField -> paramFormField.data.put("leftAddon", "calendar"))
        .with(paramFormField -> paramFormField.name = "[Admin User] birthdate"));
    hashMap.put("user.email", (new FormField())
        .with(paramFormField -> paramFormField.key = "user.email")
        .with(paramFormField -> paramFormField.confirm = false)
        .with(paramFormField -> paramFormField.control = FormControl.text)
        .with(paramFormField -> paramFormField.required = false)
        .with(paramFormField -> paramFormField.type = FormDataType.email)
        .with(paramFormField -> paramFormField.data.put("leftAddon", "user"))
        .with(paramFormField -> paramFormField.name = "[Admin User] email"));
    hashMap.put("user.phoneNumber", (new FormField())
        .with(paramFormField -> paramFormField.key = "user.phoneNumber")
        .with(paramFormField -> paramFormField.confirm = false)
        .with(paramFormField -> paramFormField.control = FormControl.text)
        .with(paramFormField -> paramFormField.required = false)
        .with(paramFormField -> paramFormField.type = FormDataType.phoneNumber)
        .with(paramFormField -> paramFormField.data.put("leftAddon", "mobile"))
        .with(paramFormField -> paramFormField.name = "[Admin User] phone number"));
    hashMap.put("user.firstName", (new FormField())
        .with(paramFormField -> paramFormField.key = "user.firstName")
        .with(paramFormField -> paramFormField.confirm = false)
        .with(paramFormField -> paramFormField.control = FormControl.text)
        .with(paramFormField -> paramFormField.required = false)
        .with(paramFormField -> paramFormField.type = FormDataType.string)
        .with(paramFormField -> paramFormField.data.put("leftAddon", "info"))
        .with(paramFormField -> paramFormField.name = "[Admin User] first name"));
    hashMap.put("user.fullName", (new FormField())
        .with(paramFormField -> paramFormField.key = "user.fullName")
        .with(paramFormField -> paramFormField.confirm = false)
        .with(paramFormField -> paramFormField.control = FormControl.text)
        .with(paramFormField -> paramFormField.required = false)
        .with(paramFormField -> paramFormField.type = FormDataType.string)
        .with(paramFormField -> paramFormField.data.put("leftAddon", "info"))
        .with(paramFormField -> paramFormField.name = "[Admin User] full name"));
    hashMap.put("user.imageUrl", (new FormField())
        .with(paramFormField -> paramFormField.key = "user.imageUrl")
        .with(paramFormField -> paramFormField.confirm = false)
        .with(paramFormField -> paramFormField.control = FormControl.text)
        .with(paramFormField -> paramFormField.required = false)
        .with(paramFormField -> paramFormField.type = FormDataType.string)
        .with(paramFormField -> paramFormField.data.put("leftAddon", "info"))
        .with(paramFormField -> paramFormField.name = "[Admin User] image URL"));
    hashMap.put("user.lastName", (new FormField())
        .with(paramFormField -> paramFormField.key = "user.lastName")
        .with(paramFormField -> paramFormField.confirm = false)
        .with(paramFormField -> paramFormField.control = FormControl.text)
        .with(paramFormField -> paramFormField.required = false)
        .with(paramFormField -> paramFormField.type = FormDataType.string)
        .with(paramFormField -> paramFormField.data.put("leftAddon", "info"))
        .with(paramFormField -> paramFormField.name = "[Admin User] last name"));
    hashMap.put("user.middleName", (new FormField())
        .with(paramFormField -> paramFormField.key = "user.middleName")
        .with(paramFormField -> paramFormField.confirm = false)
        .with(paramFormField -> paramFormField.control = FormControl.text)
        .with(paramFormField -> paramFormField.required = false)
        .with(paramFormField -> paramFormField.type = FormDataType.string)
        .with(paramFormField -> paramFormField.data.put("leftAddon", "info"))
        .with(paramFormField -> paramFormField.name = "[Admin User] middle name"));
    hashMap.put("user.mobilePhone", (new FormField())
        .with(paramFormField -> paramFormField.key = "user.mobilePhone")
        .with(paramFormField -> paramFormField.confirm = false)
        .with(paramFormField -> paramFormField.control = FormControl.text)
        .with(paramFormField -> paramFormField.required = false)
        .with(paramFormField -> paramFormField.type = FormDataType.string)
        .with(paramFormField -> paramFormField.data.put("leftAddon", "mobile"))
        .with(paramFormField -> paramFormField.name = "[Admin User] mobile phone"));
    hashMap.put("user.password", (new FormField())
        .with(paramFormField -> paramFormField.key = "user.password")
        .with(paramFormField -> paramFormField.confirm = true)
        .with(paramFormField -> paramFormField.control = FormControl.password)
        .with(paramFormField -> paramFormField.required = true)
        .with(paramFormField -> paramFormField.type = FormDataType.string)
        .with(paramFormField -> paramFormField.data.put("leftAddon", "lock"))
        .with(paramFormField -> paramFormField.name = "[Admin User] password"));
    hashMap.put("user.parentEmail", (new FormField())
        .with(paramFormField -> paramFormField.key = "user.parentEmail")
        .with(paramFormField -> paramFormField.confirm = false)
        .with(paramFormField -> paramFormField.control = FormControl.text)
        .with(paramFormField -> paramFormField.required = false)
        .with(paramFormField -> paramFormField.type = FormDataType.email)
        .with(paramFormField -> paramFormField.data.put("leftAddon", "user"))
        .with(paramFormField -> paramFormField.name = "[Registration] parent email"));
    hashMap.put("user.preferredLanguages", (new FormField())
        .with(paramFormField -> paramFormField.key = "user.preferredLanguages")
        .with(paramFormField -> paramFormField.confirm = false)
        .with(paramFormField -> paramFormField.control = FormControl.select)
        .with(paramFormField -> paramFormField.required = false)
        .with(paramFormField -> paramFormField.type = FormDataType.string)
        .with(paramFormField -> paramFormField.data.put("leftAddon", "info"))
        .with(paramFormField -> paramFormField.name = "[Admin User] preferred languages"));
    hashMap.put("user.timezone", (new FormField())
        .with(paramFormField -> paramFormField.key = "user.timezone")
        .with(paramFormField -> paramFormField.confirm = false)
        .with(paramFormField -> paramFormField.control = FormControl.select)
        .with(paramFormField -> paramFormField.required = false)
        .with(paramFormField -> paramFormField.type = FormDataType.string)
        .with(paramFormField -> paramFormField.data.put("leftAddon", "info"))
        .with(paramFormField -> paramFormField.name = "[Admin User] timezone"));
    hashMap.put("user.username", (new FormField())
        .with(paramFormField -> paramFormField.key = "user.username")
        .with(paramFormField -> paramFormField.confirm = false)
        .with(paramFormField -> paramFormField.control = FormControl.text)
        .with(paramFormField -> paramFormField.required = false)
        .with(paramFormField -> paramFormField.type = FormDataType.string)
        .with(paramFormField -> paramFormField.data.put("leftAddon", "user"))
        .with(paramFormField -> paramFormField.name = "[Admin User] username"));
    Values = Collections.unmodifiableMap(hashMap);
  }
}
