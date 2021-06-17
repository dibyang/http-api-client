package com.ls.http.api;

import com.ls.luava.common.N3Map;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author yangzj
 * @date 2021/6/16
 */
public interface ApiClient extends Closeable, ClientContext {
  List<RequestHandle> getBaseRequestHandles();
  RestClient getRestClient();

  /**
   * 获取 ApiProxy
   *
   * 使用方法 getApiProxy 替代
   *
   * @see #getApiProxy(Class)
   * @deprecated
   *
   * @param clazz
   * @param <T>
   * @return ApiProxy
   *
   */
  @Deprecated
  <T> T getProxy(Class<T> clazz);

  /**
   * 获取 ApiProxy
   * @param clazz
   * @param <T>
   * @return ApiProxy
   */
  <T> T getApiProxy(Class<T> clazz);
}
