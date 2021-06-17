package com.ls.http.api;

import com.ls.luava.common.N3Map;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

/**
 * @author yangzj
 * @date 2021/6/16
 */
public interface ApiClient extends Closeable {
  CloseableHttpResponse doRequest(String method, String uri, RequestHandle requestHandle) throws IOException;
  <T> T doRequest(String method, String uri, RequestHandle requestHandle, final ResponseHandler<? extends T> responseHandler) throws IOException;
  N3Map request(String method, String uri, RequestHandle requestHandle);
  CloseableHttpResponse doRequest(String method, String uri, Map<String,Object> params) throws IOException;
  <T> T doRequest(String method, String uri, Map<String,Object> params, final ResponseHandler<? extends T> responseHandler) throws IOException;
  N3Map request(String method, String uri, Map<String,Object> params);
  N3Map request(String method, String uri);
  N3Map get(String uri, Map<String,Object> params);
  N3Map post(String uri, Map<String,Object> params);
  N3Map put(String uri, Map<String,Object> params);
  N3Map delete(String uri, Map<String,Object> params);
  N3Map get(String uri, RequestHandle requestHandle);
  N3Map post(String uri, RequestHandle requestHandle);
  N3Map put(String uri, RequestHandle requestHandle);
  N3Map delete(String uri, RequestHandle requestHandle);
  N3Map get(String uri);
  N3Map post(String uri);
  N3Map put(String uri);
  N3Map delete(String uri);
}
