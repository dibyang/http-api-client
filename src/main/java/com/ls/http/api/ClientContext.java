package com.ls.http.api;

import com.ls.luava.common.N3Map;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;


/**
 * @author yangzj
 * @date 2021/6/17
 */
public interface ClientContext {
  N3Map getParams();
  HttpClientConnectionManager getConnManager();
  ApiClientFactoryConfig getFactoryConfig();
}
