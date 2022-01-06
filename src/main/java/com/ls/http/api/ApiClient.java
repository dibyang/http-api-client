package com.ls.http.api;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

/**
 * @author yangzj
 * @date 2021/6/16
 */
public interface ApiClient extends Closeable, ClientContext {
  String EXCEPTION = "__e__";
  List<RequestHandle> getBaseRequestHandles();
  RequestHandle getParamsHandle(Map<String, Object> params);
  RestClient getRestClient();


  /**
   * 获取 ApiProxy
   * @param clazz
   * @param <T>
   * @return ApiProxy
   */
  <T> T getApiProxy(Class<T> clazz);
}
