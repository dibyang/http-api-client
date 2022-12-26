package com.ls.http.api;

import com.google.common.collect.Lists;
import com.ls.luava.common.N3Map;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author yangzj
 * @date 2021/6/16
 */
public class ApiClientImpl implements ApiClient, RestClient {

  public static final String E = "e";
  public static final String ERROR = "error";
  static Logger LOG = LoggerFactory.getLogger(ApiClientImpl.class);

  private final URI baseUri;

  private final List<RequestHandler> baseRequestHandlers;
  private final ApiClientFactoryConfig factoryConfig;
  final HttpClientConnectionManager connManager;
  private final N3Map params = new N3Map();
  private final PostChecker checker;
  private final IFaceRoutePlanner routePlanner = new IFaceRoutePlanner(DefaultSchemePortResolver.INSTANCE);


  public ApiClientImpl(String baseUri, List<RequestHandler> requestHandlers, ApiClientFactoryConfig factoryConfig, HttpClientConnectionManager connManager, PostChecker checker) {
    this.baseUri = URI.create(baseUri);
    this.baseRequestHandlers = requestHandlers;
    this.factoryConfig = factoryConfig;
    this.connManager = connManager;
    this.checker = checker;
  }

  public HttpClientConnectionManager getConnManager() {
    return connManager;
  }

  @Override
  public ApiClientFactoryConfig getFactoryConfig() {
    return factoryConfig;
  }

  @Override
  public RestClient getRestClient() {
    return this;
  }

  public List<RequestHandler> getBaseRequestHandles() {
    return baseRequestHandlers;
  }

  private CloseableHttpClient getHttpClient() {
    RequestConfig requestConfig = RequestConfig.custom()
        .setConnectTimeout(Timeout.of(factoryConfig.getConnTimeout(),TimeUnit.MILLISECONDS))
        .setConnectionRequestTimeout(Timeout.of(factoryConfig.getSoTimeout(),TimeUnit.MILLISECONDS)).build();
    return HttpClients.custom()
      .setConnectionManager(connManager)
        .setRoutePlanner(routePlanner)
        .setDefaultRequestConfig(requestConfig)
      .build();
  }



  protected CloseableHttpResponse doRequest(String method, String uri, RequestHandler requestHandler) throws IOException {
    List<RequestHandler> requestHandlers = Lists.newArrayList(baseRequestHandlers);

    final ClassicRequestBuilder requestBuilder = ClassicRequestBuilder.create(method)
      .setUri(baseUri.resolve(uri));
    if(requestHandler !=null){
      requestHandlers.add(requestHandler);
    }
    for (RequestHandler handle : requestHandlers) {
      handle.handle(requestBuilder);
    }
    ClassicHttpRequest build = requestBuilder.build();
    CloseableHttpClient httpClient = this.getHttpClient();
    return httpClient.execute(build);

  }

  @Override
  public <T> T doRequest(String method, String uri, RequestHandler requestHandler, ResponseHandler<? extends T> responseHandler) throws IOException, ParseException {
    try(CloseableHttpResponse response = doRequest(method,uri, requestHandler)) {
      return responseHandler.handleResponse(response);
    }
  }

  @Override
  public N3Map request(String method, String uri, RequestHandler requestHandler) {
    N3Map n3Map = new N3Map();
    try {
      N3Map data = doRequest(method,uri, requestHandler,new N3MapResponseHandler());
      n3Map.putAll(data);
    } catch (IOException | ParseException e) {
      n3Map.put(EXCEPTION,e);
      n3Map.put(E,e);
      n3Map.put(ERROR,e.getClass().getSimpleName());
      //LOG.warn(null,e);
    }
    if(checker!=null&&checker.postCheck(this,n3Map)){
      n3Map = request(method, uri, requestHandler);
    }
    return n3Map;
  }


  protected CloseableHttpResponse doRequest(String method, String uri, Map<String, Object> params) throws IOException {
    return doRequest(method,uri,getParamsHandle(params));
  }

  public RequestHandler getParamsHandle(Map<String, Object> params) {
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
  public <T> T doRequest(String method, String uri, Map<String, Object> params, ResponseHandler<? extends T> responseHandler) throws IOException, ParseException {
    try(CloseableHttpResponse response = doRequest(method,uri, params)) {
      return responseHandler.handleResponse(response);
    }
  }


  protected CloseableHttpResponse doRequest(HttpMethod method, String uri, RequestHandler requestHandler) throws IOException {
    return doRequest(method.name(),uri, requestHandler);
  }

  @Override
  public <T> T doRequest(HttpMethod method, String uri, RequestHandler requestHandler, ResponseHandler<? extends T> responseHandler) throws IOException, ParseException {
    return doRequest(method.name(),uri, requestHandler,responseHandler);
  }

  @Override
  public N3Map request(HttpMethod method, String uri, RequestHandler requestHandler) {
    return request(method.name(),uri, requestHandler);
  }


  protected CloseableHttpResponse doRequest(HttpMethod method, String uri, Map<String, Object> params) throws IOException {
    return doRequest(method.name(),uri,params);
  }

  @Override
  public <T> T doRequest(HttpMethod method, String uri, Map<String, Object> params, ResponseHandler<? extends T> responseHandler) throws IOException, ParseException {
    return doRequest(method.name(),uri,params,responseHandler);
  }

  @Override
  public N3Map request(String method, String uri, Map<String, Object> params) {
    return request(method,uri,getParamsHandle(params));
  }

  @Override
  public N3Map request(String method, String uri) {
    return request(method,uri,(RequestHandler)null);
  }

  @Override
  public N3Map request(HttpMethod method, String uri, Map<String, Object> params) {
    return request(method.name(),uri,params);
  }

  @Override
  public N3Map request(HttpMethod method, String uri) {
    return request(method.name(),uri);
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
  public N3Map get(String uri, RequestHandler requestHandler) {
    return request(HttpGet.METHOD_NAME,uri, requestHandler);
  }

  @Override
  public N3Map post(String uri, RequestHandler requestHandler) {
    return request(HttpPost.METHOD_NAME,uri, requestHandler);
  }

  @Override
  public N3Map put(String uri, RequestHandler requestHandler) {
    return request(HttpPut.METHOD_NAME,uri, requestHandler);
  }

  @Override
  public N3Map delete(String uri, RequestHandler requestHandler) {
    return request(HttpDelete.METHOD_NAME,uri, requestHandler);
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
  public <T> T getApiProxy(Class<T> clazz) {
    ClientProxy<T> proxy = new ClientProxy<T>(this,clazz);
    return proxy.getInstance();
  }

  @Override
  public void setIface(String iface) {
    routePlanner.setIface(iface);
  }

  @Override
  public String getIface() {
    return routePlanner.getIface();
  }


  @Override
  public N3Map getParams() {
    return params;
  }
}
