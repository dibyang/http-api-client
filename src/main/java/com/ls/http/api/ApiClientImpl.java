package com.ls.http.api;

import com.google.common.collect.Lists;
import com.ls.luava.common.Jsons;
import com.ls.luava.common.N3Map;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * @author yangzj
 * @date 2021/6/16
 */
public class ApiClientImpl implements ApiClient {
  static Logger LOG = LoggerFactory.getLogger(ApiClientImpl.class);

  private final URI baseUri;

  private final List<RequestHandle> baseRequestHandles;
  private int connectTimeout;
  private int socketTimeout;
  private final CloseableHttpClient client;
  private final N3Map params = new N3Map();

  public ApiClientImpl(String baseUri, List<RequestHandle> requestHandles, int connectTimeout, int socketTimeout, CloseableHttpClient client) {
    this.baseUri = URI.create(baseUri);
    this.baseRequestHandles = requestHandles;
    this.connectTimeout = connectTimeout;
    this.socketTimeout = socketTimeout;
    this.client = client;
  }

  public List<RequestHandle> getBaseRequestHandles() {
    return baseRequestHandles;
  }



  @Override
  public CloseableHttpResponse doRequest(String method, String uri, RequestHandle requestHandle) throws IOException {
    List<RequestHandle> requestHandles = Lists.newArrayList(baseRequestHandles);
    RequestConfig requestConfig = RequestConfig.custom()
      .setConnectTimeout(connectTimeout)
      .setSocketTimeout(socketTimeout).build();
    final RequestBuilder requestBuilder = RequestBuilder.create(method)
      .setUri(baseUri.resolve(uri))
      .setConfig(requestConfig);
    if(requestHandle!=null){
      requestHandles.add(requestHandle);
    }
    for (RequestHandle handle : requestHandles) {
      handle.handle(requestBuilder);
    }
    return client.execute(requestBuilder.build());
  }

  @Override
  public <T> T doRequest(String method, String uri, RequestHandle requestHandle, ResponseHandler<? extends T> responseHandler) throws IOException {
    final CloseableHttpResponse response = doRequest(method,uri, requestHandle);
    return responseHandler.handleResponse(response);
  }

  @Override
  public N3Map request(String method, String uri, RequestHandle requestHandle) {
    N3Map n3Map = new N3Map();
    try {
      String s = doRequest(method,uri, requestHandle,new BasicResponseHandler());
      n3Map.putAll(Jsons.i.fromJson(s,N3Map.class));
    } catch (IOException e) {
      n3Map.put("e",e);
      LOG.warn(null,e);
    }
    return n3Map;
  }

  @Override
  public CloseableHttpResponse doRequest(String method, String uri, Map<String, Object> params) throws IOException {
    return doRequest(method,uri,getParamsHandle(params));
  }

  private RequestHandle getParamsHandle(Map<String, Object> params) {
    return builder -> {
      if(params!=null) {
        for (String name : params.keySet()) {
          Object value = handleValue(params.get(name));
          if (value instanceof Iterable) {
            Iterator iter = ((Iterable) value).iterator();
            while (iter.hasNext()) {
              Object o = iter.next();
              if (o != null) {
                builder.addParameter(name, String.valueOf(o));
              }
            }
          } else {
            builder.addParameter(name, String.valueOf(value));
          }
        }
      }
    };
  }

  private Object handleValue(Object value) {
    if (value != null && value.getClass().isArray()) {
      value = Arrays.asList((Object[]) value);
    }
    if (value instanceof Date) {
      value = ((Date) value).getTime();
    }
    return value;
  }

  @Override
  public <T> T doRequest(String method, String uri, Map<String, Object> params, ResponseHandler<? extends T> responseHandler) throws IOException {
    final CloseableHttpResponse response = doRequest(method,uri, params);
    return responseHandler.handleResponse(response);
  }

  @Override
  public N3Map request(String method, String uri, Map<String, Object> params) {
    return request(method,uri,getParamsHandle(params));
  }

  @Override
  public N3Map request(String method, String uri) {
    return request(method,uri,(RequestHandle)null);
  }

  @Override
  public N3Map get(String uri, Map<String, Object> params) {
    return request(HttpGet.METHOD_NAME,uri,getParamsHandle(params));
  }

  @Override
  public N3Map post(String uri, Map<String, Object> params) {
    return request(HttpPost.METHOD_NAME,uri,getParamsHandle(params));
  }

  @Override
  public N3Map put(String uri, Map<String, Object> params) {
    return request(HttpPut.METHOD_NAME,uri,getParamsHandle(params));
  }

  @Override
  public N3Map delete(String uri, Map<String, Object> params) {
    return request(HttpDelete.METHOD_NAME,uri,getParamsHandle(params));
  }

  @Override
  public N3Map get(String uri, RequestHandle requestHandle) {
    return request(HttpGet.METHOD_NAME,uri,requestHandle);
  }

  @Override
  public N3Map post(String uri, RequestHandle requestHandle) {
    return request(HttpPost.METHOD_NAME,uri,requestHandle);
  }

  @Override
  public N3Map put(String uri, RequestHandle requestHandle) {
    return request(HttpPut.METHOD_NAME,uri,requestHandle);
  }

  @Override
  public N3Map delete(String uri, RequestHandle requestHandle) {
    return request(HttpDelete.METHOD_NAME,uri,requestHandle);
  }

  @Override
  public N3Map get(String uri) {
    return request(HttpGet.METHOD_NAME,uri);
  }

  @Override
  public N3Map post(String uri) {
    return request(HttpPost.METHOD_NAME,uri);
  }

  @Override
  public N3Map put(String uri) {
    return request(HttpPut.METHOD_NAME,uri);
  }

  @Override
  public N3Map delete(String uri) {
    return request(HttpDelete.METHOD_NAME,uri);
  }

  @Override
  public <T> T getProxy(Class<T> clazz) {
    ClientProxy<T> proxy = new ClientProxy<T>(this,clazz);

    return proxy.getInstance();
  }


  @Override
  public void close() throws IOException {
    client.close();
  }

  @Override
  public N3Map getParams() {
    return params;
  }
}
