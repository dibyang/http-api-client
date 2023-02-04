package net.xdob.http.api;

import com.ls.luava.common.N3Map;

/**
 * @author yangzj
 * @date 2021/7/22
 */
@FunctionalInterface
public interface PostChecker {
  /**
   * 是否重发请求
   * @param m
   * @return
   */
  boolean postCheck(ApiClient client, N3Map m);
}
