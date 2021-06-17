package com.ls.http.api;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

public class ArgParm {
  private String name;
  private Object arg;
  private final List<Annotation> annotations = Lists.newArrayList();
  
  public boolean hasName(){
    return name!=null&&!name.isEmpty();
  }
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public Object getArg() {
    return arg;
  }
  public void setArg(Object arg) {
    this.arg = arg;
  }
  
  public List<Annotation> getAnnotations() {
    return annotations;
  }
  
  public void setAnnotations(Collection<Annotation> c) {
    annotations.clear();
    if(c!=null&&!c.isEmpty())annotations.addAll(c);
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Annotation> List<T> getAnnotations(Class<T> annotationClass) {
    List<T> anns = Lists.newArrayList();
    if (annotationClass == null)
        throw new NullPointerException();
    for(Annotation ann: this.getAnnotations()){
      if(annotationClass.isInstance(ann))anns.add(((T)ann));
    }
    return anns;
  }

  public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
  {
    List<T> anns = this.getAnnotations(annotationClass);
    return anns.size()>0?anns.get(0):null;
  }
}
