package com.ls.http.api;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.RequestBuilder;

import java.io.IOException;

/**
 * @author yangzj
 * @date 2021/6/16
 */
@FunctionalInterface
public interface RequestHandle {
  /**
   * 请求预处理
   * @param builder 请求构建器
   */
  void handle(RequestBuilder builder) throws IOException;
}
