package net.xdob.http.api;

import java.util.List;
import java.util.Map;

/**
 * @author yangzj
 * @date 2021/6/16
 */
public interface ApiClient extends ClientContext {
  String EXCEPTION = "__e__";
  List<RequestHandler> getBaseRequestHandles();
  RequestHandler getParamsHandle(Map<String, Object> params);
  RestClient getRestClient();

  RestAsyncClient getRestAsyncClient();


  /**
   * 获取 ApiProxy
   * @param clazz
   * @param <T>
   * @return ApiProxy
   */
  <T> T getApiProxy(Class<T> clazz);

  /**
   * IPV6 指定使用的网卡
   * @param iface
   */
  void setIface(String iface);

  /**
   * IPV6 获取指定使用的网卡
   * @return
   */
  String getIface();
}
