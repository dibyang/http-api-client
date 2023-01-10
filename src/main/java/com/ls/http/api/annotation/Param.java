package com.ls.http.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * jdk8 设置 javac -parameters  方法参数名和请求参数名一致时可以省略该注解
 *
 * gradle 设置如下
 *
 * tasks.withType(JavaCompile) {
 *   options.compilerArgs << "-parameters"
 * }
 *
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
  String value();
}
