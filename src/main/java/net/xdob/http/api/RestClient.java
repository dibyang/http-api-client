package net.xdob.http.api;

import com.ls.luava.common.N3Map;
import org.apache.hc.core5.http.ParseException;


import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author yangzj
 * @date 2021/6/17
 */
public interface RestClient {

  <T> T doRequest(String method, String uri, RequestHandler requestHandler, final ResponseHandler<T> responseHandler) throws IOException, ParseException, ExecutionException, InterruptedException;
  <T> T doRequest(String method, String uri, Map<String,Object> params, final ResponseHandler<T> responseHandler) throws IOException, ParseException, ExecutionException, InterruptedException;


  N3Map request(String method, String uri, RequestHandler requestHandler);
  N3Map request(String method, String uri, Map<String,Object> params);
  N3Map request(String method, String uri);


  N3Map get(String uri, Map<String,Object> params);
  N3Map post(String uri, Map<String,Object> params);
  N3Map put(String uri, Map<String,Object> params);
  N3Map delete(String uri, Map<String,Object> params);
  N3Map get(String uri, RequestHandler requestHandler);
  N3Map post(String uri, RequestHandler requestHandler);
  N3Map put(String uri, RequestHandler requestHandler);
  N3Map delete(String uri, RequestHandler requestHandler);
  N3Map get(String uri);
  N3Map post(String uri);
  N3Map put(String uri);
  N3Map delete(String uri);
}
