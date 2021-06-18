package com.ls.http.api.annotation;

import com.ls.http.api.HttpMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mapping {
  String value();
  String[] returnKey() default {};
  HttpMethod method() default HttpMethod.POST;
}
