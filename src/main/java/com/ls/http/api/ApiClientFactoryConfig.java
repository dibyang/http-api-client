package com.ls.http.api;

/**
 * @author yangzj
 * @date 2021/7/22
 */
public interface ApiClientFactoryConfig {
  int getConnTimeout();
  int getSoTimeout();
}
