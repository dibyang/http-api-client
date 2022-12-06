package com.ls.http.api;



import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import java.io.IOException;

/**
 * @author yangzj
 * @date 2021/6/16
 */
@FunctionalInterface
public interface RequestHandler {
  /**
   * 请求预处理
   * @param builder 请求构建器
   */
  void handle(ClassicRequestBuilder builder) throws IOException;
}
