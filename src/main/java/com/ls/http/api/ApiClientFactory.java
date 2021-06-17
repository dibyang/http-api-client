package com.ls.http.api;

import org.apache.http.Header;

import java.util.List;

/**
 * @author yangzj
 * @date 2021/6/16
 */
public interface ApiClientFactory {
  /**
   * 获取RestClient
   * @param baseUri 基础uri
   * @return ApiClient
   */
  ApiClient getRestClient(String baseUri);

  /**
   *
   * 获取RestClient
   * @param baseUri 基础uri
   * @param headers http头
   * @param handles 请求预处理器
   * @return ApiClient
   */
  ApiClient getRestClient(String baseUri, List<Header> headers, RequestHandle... handles);


}
