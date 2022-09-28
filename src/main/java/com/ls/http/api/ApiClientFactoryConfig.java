package com.ls.http.api;

import org.apache.hc.client5.http.routing.HttpRoutePlanner;

/**
 * @author yangzj
 * @date 2021/7/22
 */
public interface ApiClientFactoryConfig {
  int getConnTimeout();
  int getSoTimeout();
}
