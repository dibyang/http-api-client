package net.xdob.http.api.annotation;

import net.xdob.http.api.ParamSign;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Sign {

  String MD5 = "MD5";
  String SHA256 = "SHA256";

  /**
   * 签名的参数名或head名
   * @return
   */
  String value() default "sign";
  /**
   * 签名计算的key
   * @return
   */
  String key() default "";

  /**
   * 签名是否放入http head
   * @return
   */
  boolean header() default true;

  String signMethod() default MD5;
}
