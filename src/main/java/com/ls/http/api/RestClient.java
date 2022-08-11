package com.ls.http.api;

import com.ls.luava.common.N3Map;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ParseException;


import java.io.IOException;
import java.util.Map;

/**
 * @author yangzj
 * @date 2021/6/17
 */
public interface RestClient {
  CloseableHttpResponse doRequest(String method, String uri, RequestHandle requestHandle) throws IOException;
  <T> T doRequest(String method, String uri, RequestHandle requestHandle, final ResponseHandler<? extends T> responseHandler) throws IOException, ParseException;
  N3Map request(String method, String uri, RequestHandle requestHandle);
  CloseableHttpResponse doRequest(String method, String uri, Map<String,Object> params) throws IOException;
  <T> T doRequest(String method, String uri, Map<String,Object> params, final ResponseHandler<? extends T> responseHandler) throws IOException, ParseException;
  CloseableHttpResponse doRequest(HttpMethod method, String uri, RequestHandle requestHandle) throws IOException;
  <T> T doRequest(HttpMethod method, String uri, RequestHandle requestHandle, final ResponseHandler<? extends T> responseHandler) throws IOException, ParseException;
  N3Map request(HttpMethod method, String uri, RequestHandle requestHandle);
  CloseableHttpResponse doRequest(HttpMethod method, String uri, Map<String,Object> params) throws IOException;
  <T> T doRequest(HttpMethod method, String uri, Map<String,Object> params, final ResponseHandler<? extends T> responseHandler) throws IOException, ParseException;

  N3Map request(String method, String uri, Map<String,Object> params);
  N3Map request(String method, String uri);
  N3Map request(HttpMethod method, String uri, Map<String,Object> params);
  N3Map request(HttpMethod method, String uri);

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
