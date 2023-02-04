package net.xdob.http.api;


import org.apache.hc.core5.http.Header;

import java.util.List;

/**
 * @author yangzj
 * @date 2021/6/16
 */
public interface HttpClientFactory extends HttpClientFactoryConfig, ResponseHandlerSupport {
  void shutdown();
  HttpClientFactory setSoTimeout(int soTimeout);
  HttpClientFactory setConnTimeout(int connTimeout);


  HttpClientFactory disableSslVerification();

  HttpClient getHttpClient();
  HttpClient getHttpClient(int soTimeout, int connTimeout);

  <V> V call(HttpCallable<V> callable) throws Exception;

  ApiExecutor getApiExecutor(String baseUri, List<Header> headers, PostChecker checker, RequestHandler... handles);

  ApiExecutor getApiExecutor(String baseUri, List<Header> headers, RequestHandler... handles);

  ApiExecutor getApiExecutor(String baseUri);

}
