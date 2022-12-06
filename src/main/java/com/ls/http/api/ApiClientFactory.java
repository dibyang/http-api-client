package com.ls.http.api;


import org.apache.hc.core5.http.Header;

import java.util.List;

/**
 * @author yangzj
 * @date 2021/6/16
 */
public interface ApiClientFactory extends ApiClientFactoryConfig, ResponseHandlerSupport {
  void shutdown();
  ApiClientFactory setSoTimeout(int soTimeout);
  ApiClientFactory setConnTimeout(int connTimeout);


  ApiClientFactory disableSslVerification();

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
