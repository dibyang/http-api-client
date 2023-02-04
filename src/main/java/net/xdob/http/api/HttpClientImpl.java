package net.xdob.http.api;

import com.google.common.collect.Lists;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.http.Header;

import java.util.List;

public class HttpClientImpl implements HttpClient{
  private final CloseableHttpAsyncClient httpClient;
  private final IFaceRoutePlanner routePlanner;
  private final HttpClientFactoryConfig factoryConfig;

  public HttpClientImpl(CloseableHttpAsyncClient httpClient, IFaceRoutePlanner routePlanner, HttpClientFactoryConfig factoryConfig) {
    this.httpClient = httpClient;
    this.routePlanner = routePlanner;
    this.factoryConfig = factoryConfig;
    this.httpClient.start();
  }

  @Override
  public HttpClientFactoryConfig getFactoryConfig() {
    return factoryConfig;
  }

  @Override
  public CloseableHttpAsyncClient getHttpAsyncClient() {
    return httpClient;
  }

  @Override
  public ApiClient getApiClient(String baseUri) {
    return getApiClient(baseUri,null);
  }

  @Override
  public ApiClient getApiClient(String baseUri, List<Header> headers, RequestHandler... handles) {
    return getApiClient(baseUri, headers, null, handles);
  }

  @Override
  public ApiClient getApiClient(String baseUri, List<Header> headers, PostChecker checker, RequestHandler... handles) {
    List<RequestHandler> requestHandlers = Lists.newArrayList(handles);
    if(headers!=null&&headers.size()>0){
      requestHandlers.add(builder-> headers.forEach(header->builder.addHeader(header)));
    }

    return new ApiAsyncClientImpl(this, routePlanner, baseUri, requestHandlers, checker);
  }

  @Override
  public void close() throws Exception {
    this.httpClient.close();
  }
}
