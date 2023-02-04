package net.xdob.http.api;

import com.ls.luava.common.N3Map;


/**
 * @author yangzj
 * @date 2021/6/17
 */
public interface ClientContext {
  N3Map getParams();
  HttpClient getHttpClient();
}
