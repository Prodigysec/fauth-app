package io.fusionauth.app.freemarker;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.util.Set;

public class FilteringTemplateHashModelEx implements TemplateHashModelEx {
  private final TemplateHashModelEx modelsHashModel;
  
  private final Set<String> restrictedKeys;
  
  public FilteringTemplateHashModelEx(TemplateHashModelEx paramTemplateHashModelEx, Set<String> paramSet) {
    this.modelsHashModel = paramTemplateHashModelEx;
    this.restrictedKeys = paramSet;
  }
  
  public TemplateModel get(String paramString) throws TemplateModelException {
    if (this.restrictedKeys.contains(paramString))
      return (TemplateModel)new SimpleHash((ObjectWrapper)(new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_33)).build()); 
    return this.modelsHashModel.get(paramString);
  }
  
  public boolean isEmpty() throws TemplateModelException {
    return this.modelsHashModel.isEmpty();
  }
  
  public TemplateCollectionModel keys() throws TemplateModelException {
    return this.modelsHashModel.keys();
  }
  
  public int size() throws TemplateModelException {
    return this.modelsHashModel.size();
  }
  
  public TemplateCollectionModel values() throws TemplateModelException {
    return this.modelsHashModel.values();
  }
}
