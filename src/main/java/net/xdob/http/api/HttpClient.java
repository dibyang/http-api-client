package net.xdob.http.api;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.http.Header;

import java.util.List;

/**
 * http client
 */
public interface HttpClient extends AutoCloseable{

  HttpClientFactoryConfig getFactoryConfig();

  CloseableHttpAsyncClient getHttpAsyncClient();

  /**
   * 获取RestClient
   * @param baseUri 基础uri
   * @return ApiClient
   */
  ApiClient getApiClient(String baseUri);

  /**
   *
   * 获取RestClient
   * @param baseUri 基础uri
   * @param headers http头
   * @param handles 请求预处理器
   * @return ApiClient
   */
  ApiClient getApiClient(String baseUri, List<Header> headers, RequestHandler... handles);

  /**
   *
   * 获取RestClient
   * @param baseUri 基础uri
   * @param headers http头
   * @param checker PostChecker
   * @param handles 请求预处理器
   * @return ApiClient
   */
  ApiClient getApiClient(String baseUri, List<Header> headers, PostChecker checker, RequestHandler... handles);

}
