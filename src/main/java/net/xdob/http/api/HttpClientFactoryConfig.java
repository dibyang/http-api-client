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
}
