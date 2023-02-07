package net.xdob.http.api;


import java.lang.reflect.Type;

/**
 * @author yangzj
 * @date 2021/7/22
 */
public interface HttpClientFactoryConfig {
  int getConnTimeout();
  int getSoTimeout();
  <T> ResponseHandler<T> getResponseHandler(Type type, String name);
  /**
   * 注册的参数签名方法
   *
   * @param signMethod 参数签名方法名
   * @param paramSign 参数签名方法
   */
  void setParamSign(String signMethod,ParamSign paramSign);

  /**
   * 注销参数签名方法
   * @param signMethod
   */
  void removeParamSign(String signMethod);
  /**
   * 获取注册的参数签名方法
   * @param signMethod 参数签名方法名
   * @return 参数签名方法
   */
  ParamSign getParamSign(String signMethod);
}
